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
package com.eviware.loadui.ui.fx.views.assertions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.ScrollableList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.statistics.StatisticHolderToolBox;
import com.google.common.base.Function;

@SuppressWarnings( "rawtypes" )
public class AssertionInspectorView extends HBox
{

	private static int currentIdInt = 1;

	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( AssertionInspectorView.class );

	private final ObjectProperty<ProjectItem> projectProperty = new SimpleObjectProperty<>();

	@FXML
	private StatisticHolderToolBox componentToolBox;

	@FXML
	private ScrollableList<AssertionView> assertionList;

	private final ObservableList<AssertionView> assertions = FXCollections.observableArrayList();

	public AssertionInspectorView()
	{
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		final Function<AssertionItem, AssertionView> ASSERTION_TO_VIEW = new Function<AssertionItem, AssertionView>()
		{
			@Override
			public AssertionView apply( AssertionItem assertion )
			{
				return new AssertionView( assertion, assertions );
			}
		};

		projectProperty.addListener( new ChangeListener<ProjectItem>()
		{
			@Override
			public void changed( ObservableValue<? extends ProjectItem> arg0, ProjectItem oldValue, ProjectItem newValue )
			{
				if( oldValue != null )
				{
					Bindings.unbindContent( assertionList.getItems(), assertions );
					assertionList.getItems().clear();
				}
				if( newValue != null )
				{
					assertions.setAll( ObservableLists.transform(
							ObservableLists.fx( AssertionUtils.assertions( newValue ) ), ASSERTION_TO_VIEW ) );
					Bindings.bindContent( assertionList.getItems(), assertions );
				}
			}
		} );

		assertionList.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( final DraggableEvent event )
			{
				if( event.getData() instanceof StatisticHolder )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						handleDrop( event );
						event.consume();
					}
				}
			}
		} );
	}

	private void handleDrop( DraggableEvent event )
	{
		final StatisticHolder holder = ( StatisticHolder )event.getData();
		final CreateAssertionDialog dialog = new CreateAssertionDialog( this, holder );

		dialog.setOnConfirm( new EventHandler<ActionEvent>()
		{

			@Override
			public void handle( ActionEvent actionEvent )
			{
				AssertableWrapper<ListenableValue<Number>> selectedWrapper = dialog.getSelectedAssertable();

				Resolver<? extends ListenableValue<Number>> resolver = selectedWrapper.getResolver();

				AssertionAddon assertionAddon = holder.getCanvas().getAddon( AssertionAddon.class );
				AssertionItem.Mutable<Number> assertion = assertionAddon.createAssertion( holder, resolver );
				assertion.setLabel( getUniqueAssertionLabel( assertionAddon.getAssertions() ) );

				assertion.setConstraint( dialog.getConstraint() );
				Pair<Integer, Integer> tolerance = dialog.getTolerance();
				assertion.setTolerance( tolerance.getValue(), tolerance.getKey() );

				dialog.close();
			}

		} );
		dialog.show();
	}

	public ObjectProperty<ProjectItem> projectProperty()
	{
		return projectProperty;
	}

	public static String getUniqueAssertionLabel( Collection<? extends AssertionItem<?>> assertions )
	{
		Set<String> otherLabels = new HashSet<>();
		for( AssertionItem<?> other : assertions )
		{
			otherLabels.add( other.getLabel() );
		}

		String label = null;
		while( ( label = "Assertion " + currentIdInt++ ) != null && otherLabels.contains( label ) )
		{
		} // repeat until unique ID is found
		return label;
	}

}
