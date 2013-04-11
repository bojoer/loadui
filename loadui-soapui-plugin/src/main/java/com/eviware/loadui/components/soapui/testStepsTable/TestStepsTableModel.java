/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.components.soapui.testStepsTable;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.layout.SoapUiProjectSelector;
import com.eviware.loadui.components.soapui.utils.SwingFXUtils2;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.util.ScheduledExecutor;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

public class TestStepsTableModel
{
	private static final ImageView DISABLED_ICON = new ImageView( TestStepsTableModel.class.getResource(
			"/images/disabledTestStep.png" ).toExternalForm() );
	private final static long UPDATE_INTERVAL = 500;

	protected static final Logger log = LoggerFactory.getLogger( SoapUiProjectSelector.class );

	private final TableView<TestStep> table;
	private final LoadingCache<TestStep, IntegerProperty> invocationCounts = CacheBuilder.newBuilder().weakKeys()
			.build( new CacheLoader<TestStep, IntegerProperty>()
			{
				@Override
				public IntegerProperty load( TestStep step ) throws Exception
				{
					try
					{
						return new SimpleIntegerProperty( component.getTestStepInvocationCount( step ) );
					}
					catch( NullPointerException e )
					{
						return new SimpleIntegerProperty( 0 );
					}
				}
			} );
	private final SoapUISamplerComponent component;
	private final ScheduledFuture<?> future;

	public TestStepsTableModel( @Nonnull final SoapUISamplerComponent component )
	{
		this.component = component;

		TableColumn<TestStep, Label> testStepColumn = TableColumnBuilder.<TestStep, Label> create().resizable( false )
				.prefWidth( 190 ).text( "TestStep" ).build();
		TableColumn<TestStep, Label> disableColumn = TableColumnBuilder.<TestStep, Label> create().resizable( false )
				.prefWidth( 47 ).text( "Disable" ).build();
		TableColumn<TestStep, Number> countColumn = TableColumnBuilder.<TestStep, Number> create().resizable( false )
				.prefWidth( 58 ).text( "Count" ).build();

		table = TableViewBuilder.<TestStep> create().editable( false ).minWidth( 310 ).build();

		table.getColumns().add( testStepColumn );
		table.getColumns().add( disableColumn );
		table.getColumns().add( countColumn );

		testStepColumn.setCellValueFactory( new LabelCellFactory() );
		disableColumn.setCellValueFactory( new DisabledCellFactory() );
		countColumn.setCellValueFactory( new CountCellFactory() );

		future = ScheduledExecutor.instance.scheduleAtFixedRate( new Runnable()
		{
			@Override
			public void run()
			{
				updateCounts();
			}
		}, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MILLISECONDS );
	}

	private void updateCounts()
	{
		for( Map.Entry<TestStep, IntegerProperty> entry : invocationCounts.asMap().entrySet() )
		{
			int count = component.getTestStepInvocationCount( entry.getKey() );
			entry.getValue().set( count );
		}
	}

	public void release()
	{
		future.cancel( true );
	}

	public LayoutComponentImpl buildLayout()
	{
		return new LayoutComponentImpl( ImmutableMap.<String, Object> builder().put( "component", table )
				.put( "componentHeight", 125 ) //
				.put( "componentWidth", 324 ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "spanx 3, h 125!" ) //
				.build() );
	}

	public void updateTestCase( WsdlTestCase testCase )
	{
		invocationCounts.invalidateAll();
		table.setItems( FXCollections.observableArrayList( testCase.getTestStepList() ) );
	}

	public void clearTestCase()
	{
		invocationCounts.invalidateAll();
		table.getItems().clear();
	}

	private final static class LabelCellFactory implements
			Callback<CellDataFeatures<TestStep, Label>, ObservableValue<Label>>
	{
		@Override
		public ObservableValue<Label> call( CellDataFeatures<TestStep, Label> p )
		{
			TestStep step = p.getValue();
			java.awt.Image awtImage = step.getIcon().getImage();
			BufferedImage bufferedImage = SwingFXUtils2.toBufferedImageUnchecked( awtImage );
			WritableImage fxImage = new WritableImage( bufferedImage.getWidth(), bufferedImage.getHeight() );
			SwingFXUtils.toFXImage( bufferedImage, fxImage );
			ImageView icon = ImageViewBuilder.create().image( fxImage ).opacity( step.isDisabled() ? 0.4 : 1.0 ).build();
			return new ReadOnlyObjectWrapper<Label>( new Label( step.getLabel(), icon ) );
		}
	}

	private final class DisabledCellFactory implements
			Callback<CellDataFeatures<TestStep, Label>, ObservableValue<Label>>
	{
		@Override
		public ObservableValue<Label> call( CellDataFeatures<TestStep, Label> p )
		{
			final TestStep step = p.getValue();
			ImageView image = null;
			if( step.isDisabled() )
				image = DISABLED_ICON;
			Label label = LabelBuilder.create().graphic( image ).minWidth( 45 ).minHeight( 20 )
					.onMouseClicked( new EventHandler<MouseEvent>()
					{
						@Override
						public void handle( MouseEvent e )
						{
							component.setTestStepIsDisabled( step, !step.isDisabled() );
						}
					} ).build();
			return new ReadOnlyObjectWrapper<Label>( label );
		}
	}

	private final class CountCellFactory implements
			Callback<CellDataFeatures<TestStep, Number>, ObservableValue<Number>>
	{
		@Override
		public ObservableValue<Number> call( CellDataFeatures<TestStep, Number> p )
		{
			TestStep step = p.getValue();
			return invocationCounts.getUnchecked( step );
		}
	}
}
