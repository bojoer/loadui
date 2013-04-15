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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
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
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.google.common.base.Objects;

final public class TestCasePropertiesNode extends VBox
{
	protected static final Logger log = LoggerFactory.getLogger( TestCasePropertiesNode.class );

	ComponentContext context;
	ArrayList<Property<?>> customProperties = new ArrayList<>();
	PropertiesTableView table;

	//	public static TestCaseProperties newInstance( ComponentContext context )
	//	{
	//		TestCaseProperties t = new TestCaseProperties();
	//		t.addTableModelListener( t.new PropertiesTableListener( context ) );
	//		return t;
	//	}

	public TestCasePropertiesNode( String label, ComponentContext context )
	{
		this.context = context;
		table = new PropertiesTableView();

		table.setList( context.getProperties() );

		this.getChildren().addAll( new Label( label ), table );

		//data = new ArrayList( context.getProperties() );
	}

	//	public Collection<Property<?>> getViewProperties()
	//	{
	//		List<Property<?>> loadUiProperties = new LinkedList<>();
	//		for( TestProperty soapUiProperty : soapUiTestCaseProperties )
	//		{
	//			loadUiProperties.add( new ViewProperty( soapUiProperty ) );
	//		}
	//		return loadUiProperties;
	//	}

	public void putTestCaseProperties( List<TestProperty> soapUiTestCaseProperties )
	{

		customProperties.clear();

		for( Property<?> p : context.getProperties() )
		{
			log.debug( "context has before:" + p.getKey() + ", " + p.getValue() );
		}

		log.debug( "properties should be cleared: " + customProperties.size() );
		for( TestProperty tp : soapUiTestCaseProperties )
		{
			log.debug( "adding testcaseproperty: " + tp.getName() + ", " + tp.getValue() );
			customProperties.add( setOrCreateContextProperty( tp.getName(), tp.getValue() ) );
		}

		for( Property<?> p : context.getProperties() )
		{
			log.debug( "context has after:" + p.getKey() + ", " + p.getValue() );
		}

		table.setList( context.getProperties() );

	}

	/**
	 * Applies properties to a TestCase from an incoming message.
	 * 
	 * @param testCase
	 * @param triggerMessage
	 */

	public void overrideTestCaseProperties( WsdlTestCase testCase, TerminalMessage triggerMessage )
	{
		log.debug( "overidingTestCaseProperties({})", customProperties.size() );
		for( Property<?> p : customProperties )
		{
			log.debug( "setting property:" + p.getKey() + ", " + p.getValue() );
			testCase.setPropertyValue( p.getKey().replaceFirst( "_valueToOverride_", "" ), p.getValue() + "" );
		}

		for( String name : testCase.getPropertyNames() )
		{
			if( triggerMessage.containsKey( name ) )
				testCase.setPropertyValue( name, String.valueOf( triggerMessage.get( name ) ) );
		}
	}

	/**
	 * Updates the table, replacing the default TestCase Property values with the
	 * overriding (user-defined) values.
	 * 
	 * @param properties
	 */

	public void loadOverridingProperties( Collection<Property<?>> properties )
	{
		System.out.println( "loadOverridingProperties: " + properties.size() );
		for( Property<?> p : context.getProperties() )
		{
			System.out.println( "ovveriding properties:" + p.getKey() + ", " + p.getValue() );
		}

		// fetch overriding values for testcase properties
		for( Property<?> p : customProperties ) // ["ny", 400]
		{
			// check if property for overridden value already exists
			for( Property<?> inP : properties )
			{
				if( inP.getKey().equals( p.getKey() ) ) // _valueToOverride_ny
				{
					p.setValue( inP.getValue() != null ? inP.getValue().toString() : "" );
					setOrCreateContextProperty( p.getKey(), p.getStringValue() );

				}
			}
		}
		table.setList( context.getProperties() );
	}

	/*
	 * Synchronizes with Context because the context´s properties are saved.
	 */
	private Property<?> setOrCreateContextProperty( String key, String value )
	{

		log.info( "setting property to context: {}, {}", key, value );

		Property<?> contextProperty = setContextProperty( key, value );

		if( contextProperty != null )
		{
			return contextProperty;
		}
		else
		{
			contextProperty = context.createProperty( "_valueToOverride_" + key, String.class );
			Object valueToSet = value;
			contextProperty.setValue( Objects.firstNonNull( valueToSet.toString(), "" ) );
			return contextProperty;
		}

	}

	private Property<?> setContextProperty( String key, String value )
	{

		for( Property<?> p : context.getProperties() )
		{
			if( p.getKey().equals( "_valueToOverride_" + key ) )
			{
				p.setValue( value );
				return p;
			}
		}
		return null;
	}

	public int size()
	{
		return customProperties.size();
	}

	public Collection<Property<?>> getData()
	{
		return customProperties;
	}

	private class PropertiesTableView extends TableView<javafx.beans.property.Property<String>>
	{
		public PropertiesTableView()
		{
			this.setEditable( true );

			TableColumn<javafx.beans.property.Property<String>, String> keyColumn = new TableColumn<>( "Property" );
			TableColumn<javafx.beans.property.Property<String>, String> valueColumn = new TableColumn<>( "Value" );

			keyColumn
					.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<javafx.beans.property.Property<String>, String>, ObservableValue<String>>()
					{

						@Override
						public ObservableValue<String> call(
								CellDataFeatures<javafx.beans.property.Property<String>, String> data )
						{
							return new ReadOnlyStringWrapper( data.getValue().getName() );
						}
					} );

			valueColumn
					.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<javafx.beans.property.Property<String>, String>, ObservableValue<String>>()
					{

						@Override
						public ObservableValue<String> call(
								CellDataFeatures<javafx.beans.property.Property<String>, String> data )
						{

							return data.getValue();
						}
					} );

			valueColumn
					.setCellFactory( new Callback<TableColumn<javafx.beans.property.Property<String>, String>, TableCell<javafx.beans.property.Property<String>, String>>()
					{

						@Override
						public TableCell<javafx.beans.property.Property<String>, String> call(
								TableColumn<javafx.beans.property.Property<String>, String> table )
						{
							TextFieldTableCell<javafx.beans.property.Property<String>, String> cell = new TextFieldTableCell<>(
									new DefaultStringConverter() );

							cell.setEditable( true );

							return cell;
						}
					} );

			valueColumn
					.setOnEditCommit( new EventHandler<TableColumn.CellEditEvent<javafx.beans.property.Property<String>, String>>()
					{

						@Override
						public void handle( CellEditEvent<javafx.beans.property.Property<String>, String> event )
						{
							log.info( "Setting property {} to {}", event.getRowValue().getName(), event.getNewValue() );
							setOrCreateContextProperty( event.getRowValue().getName(), event.getNewValue() );

							setList( context.getProperties() );
						}

					} );

			valueColumn.setEditable( true );

			this.getColumns().setAll( keyColumn, valueColumn );
			System.out.println( "CREATED TABLE?)?=?=???=?=?=?=?=?=?=??==?=??=?=?=?=??=?!       " + this.toString() );
		}

		public void setList( Collection<Property<?>> list )
		{
			log.info( "Setting items(" + list.size() + ")" );

			Collection<StringProperty> stringList = new ArrayList<>();

			for( Property<?> p : list )
			{
				log.debug( "item: {}, {}", p.getKey(), p.getValue() );
				if( p.getKey().startsWith( "_valueToOverride_" ) )
				{
					stringList.add( new SimpleStringProperty( null, p.getKey().replaceFirst( "_valueToOverride_", "" ), p
							.getValue() + "" ) );
				}
			}
			;

			getItems().clear();

			getItems().setAll( stringList );
		}
	}

}
