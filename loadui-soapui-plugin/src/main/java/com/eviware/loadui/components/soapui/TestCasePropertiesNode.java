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
package com.eviware.loadui.components.soapui;

import static com.eviware.loadui.util.property.PropertyUtils.keyEquals;
import static com.google.common.collect.Iterables.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.property.PropertyUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

final public class TestCasePropertiesNode extends VBox
{
	public static final String OVERRIDING_VALUE_PREFIX = "_valueToOverride_";

	protected static final Logger log = LoggerFactory.getLogger( TestCasePropertiesNode.class );

	//	public static TestCaseProperties newInstance( ComponentContext context )
	//	{
	//		TestCaseProperties t = new TestCaseProperties();
	//		t.addTableModelListener( t.new PropertiesTableListener( context ) );
	//		return t;
	//	}

	//	public Collection<Property<?>> getViewProperties()
	//	{
	//		List<Property<?>> loadUiProperties = new LinkedList<>();
	//		for( TestProperty soapUiProperty : soapUiTestCaseProperties )
	//		{
	//			loadUiProperties.add( new ViewProperty( soapUiProperty ) );
	//		}
	//		return loadUiProperties;
	//	}

	//	public void putTestCaseProperties( List<TestProperty> soapUiTestCaseProperties )
	//	{
	//		transformedProperties.clear();
	//
	//		for( Property<?> p : context.getProperties() )
	//		{
	//			System.out.println( "context has before:" + p.getKey() + ", " + p.getValue() );
	//		}
	//
	//		System.out.println( "properties should be cleared: " + transformedProperties.size() );
	//		for( TestProperty tp : soapUiTestCaseProperties )
	//		{
	//			System.out.println( "adding testcaseproperty: " + tp.getName() + ", " + tp.getValue() );
	//			Property<?> property = setOrCreateContextProperty( tp.getName(), tp.getValue() );
	//			System.out.println( "Created property: " + property.getKey() );
	//			transformedProperties.add( property );
	//		}
	//
	//		for( Property<?> p : context.getProperties() )
	//		{
	//			System.out.println( "context has after:" + p.getKey() + ", " + p.getValue() );
	//		}
	//
	//		table.setList( context.getProperties() );
	//
	//	}

	/**
	 * Applies properties to a TestCase from an incoming message.
	 * 
	 * @param testCase
	 * @param triggerMessage
	 */

	public void overrideTestCaseProperties( WsdlTestCase testCase, TerminalMessage triggerMessage )
	{
		//		log.debug( "overidingTestCaseProperties({})", transformedProperties.size() );
		//		for( Property<?> p : transformedProperties )
		//		{
		//			log.debug( "setting property:" + p.getKey() + ", " + p.getValue() );
		//			testCase.setPropertyValue( p.getKey().replaceFirst( OVERRIDING_VALUE_PREFIX, "" ), p.getValue() + "" );
		//		}
		//
		//		for( String name : testCase.getPropertyNames() )
		//		{
		//			if( triggerMessage.containsKey( name ) )
		//				testCase.setPropertyValue( name, String.valueOf( triggerMessage.get( name ) ) );
		//		}
	}

	/**
	 * Updates the table, replacing the default TestCase Property values with the
	 * overriding (user-defined) values.
	 * 
	 * @param overridingValues
	 */

	public void loadOverridingProperties( WsdlTestCase targetTestCase, Collection<Property<?>> overridingValues )
	{
		for( TestProperty suiProperty : targetTestCase.getPropertyList() )
		{
			for( Property<?> overridingValue : overridingValues )
			{
				if( isOverriding( suiProperty, overridingValue ) )
				{
					suiProperty.setValue( overridingValue.getValue().toString() );
				}
			}
		}
	}

	private static boolean isOverriding( TestProperty suiProperty, Property<?> overridingValue )
	{
		System.out.println( overridingValue.getKey() + " = " + OVERRIDING_VALUE_PREFIX + suiProperty.getName() );
		return overridingValue.getKey().equals( OVERRIDING_VALUE_PREFIX + suiProperty.getName() );
	}

	//	@Nonnull
	//	private Property<?> setOrCreateContextProperty( String key, String value )
	//	{
	//		System.out.println( "setting property to context: " + key + " = " + value );
	//
	//		if( hasContextProperty( key ) )
	//		{
	//			return setContextProperty( key, value );
	//		}
	//		else
	//		{
	//			return context.createProperty( OVERRIDING_VALUE_PREFIX + key, String.class, value );
	//		}
	//	}
	//
	//	private boolean hasContextProperty( String key )
	//	{
	//		return any( context.getProperties(), keyEquals( OVERRIDING_VALUE_PREFIX + key ) );
	//	}
	//
	//	private Property<?> setContextProperty( String key, String value )
	//	{
	//		for( Property<?> p : context.getProperties() )
	//		{
	//			if( p.getKey().equals( OVERRIDING_VALUE_PREFIX + key ) )
	//			{
	//				p.setValue( value );
	//				return p;
	//			}
	//		}
	//		throw new NoSuchElementException();
	//	}

	public static Callable<Node> createTableView( final SoapUISamplerComponent component )
	{
		return new Callable<Node>()
		{
			@Override
			public Node call() throws Exception
			{
				PropertiesTableView table = new PropertiesTableView();
				TestCase testCase = component.getTestCase();

				if( testCase != null )
				{
					table.getItems().setAll( testCase.getPropertyList() );
				}

				TestCasePropertiesNode properties = new TestCasePropertiesNode();
				properties.getChildren().addAll( new Label( "TestCase Properties" ), table );
				return properties;
			}
		};
	}

	@Immutable
	private static class PropertiesTableView extends TableView<TestProperty>
	{
		public PropertiesTableView()
		{
			this.setEditable( true );

			TableColumn<TestProperty, String> keyColumn = new TableColumn<>( "Property" );
			TableColumn<TestProperty, String> valueColumn = new TableColumn<>( "Value" );

			keyColumn
					.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<TestProperty, String>, ObservableValue<String>>()
					{

						@Override
						public ObservableValue<String> call( CellDataFeatures<TestProperty, String> data )
						{
							return new ReadOnlyStringWrapper( data.getValue().getName() );
						}
					} );

			valueColumn
					.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<TestProperty, String>, ObservableValue<String>>()
					{

						@Override
						public ObservableValue<String> call( CellDataFeatures<TestProperty, String> data )
						{

							return new ReadOnlyStringWrapper( data.getValue().getValue() );
						}
					} );

			valueColumn.setCellFactory( new Callback<TableColumn<TestProperty, String>, TableCell<TestProperty, String>>()
			{

				@Override
				public TableCell<TestProperty, String> call( TableColumn<TestProperty, String> table )
				{
					TextFieldTableCell<TestProperty, String> cell = new TextFieldTableCell<>( new DefaultStringConverter() );

					cell.setEditable( true );

					return cell;
				}
			} );

			valueColumn.setOnEditCommit( new EventHandler<TableColumn.CellEditEvent<TestProperty, String>>()
			{

				@Override
				public void handle( CellEditEvent<TestProperty, String> event )
				{
					log.info( "Setting property {} to {}", event.getRowValue().getName(), event.getNewValue() );
					//					setOrCreateContextProperty( event.getRowValue().getName(), event.getNewValue() );

					//					setList( context.getProperties() );
				}

			} );

			valueColumn.setEditable( true );

			this.getColumns().setAll( keyColumn, valueColumn );
			System.out.println( "CREATED TABLE?)?=?=???=?=?=?=?=?=?=??==?=??=?=?=?=??=?!       " + this.toString() );
		}

	}

}
