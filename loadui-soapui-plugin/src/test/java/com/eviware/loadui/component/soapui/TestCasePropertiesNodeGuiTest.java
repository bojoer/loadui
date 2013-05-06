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
package com.eviware.loadui.component.soapui;

import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static com.eviware.loadui.ui.fx.util.test.TestFX.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.GroupBuilder;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.TestCasePropertiesNode;
import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.TestingProperty;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.javafx.scene.control.skin.LabeledText;
import com.sun.javafx.scene.control.skin.TableRowSkin;

@Category( GUITest.class )
public class TestCasePropertiesNodeGuiTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	//private static Stage stage;
	private static TestFX controller;

	private static List<TestProperty> inputProperties = new ArrayList<>();
	private static List<Property<?>> outputProperties = new ArrayList<>();

	private static HBox box = new HBox();

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( TestCasePropertiesNodeApp.class );
		Stage stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	public static class TestCasePropertiesNodeApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			primaryStage.setScene( SceneBuilder.create().height( 600 ).width( 300 )
					.root( GroupBuilder.create().children( box ).build() ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@SuppressWarnings( "unchecked" )
	public ComponentContext createContextMock()
	{
		ComponentContext context = mock( ComponentContext.class );
		when( context.getProperties() ).thenReturn( outputProperties );
		when( context.createProperty( Mockito.anyString(), ( Class<String> )Mockito.any( Class.class ), Mockito.any() ) )
				.thenAnswer( new Answer<Property<?>>()
				{
					@Override
					public Property<?> answer( InvocationOnMock invocation ) throws Throwable
					{
						Object[] args = invocation.getArguments();

						String key = ( String )args[0];
						String value = ( String )args[2];

						Property<String> createdProperty = new TestingProperty<String>( String.class, key, value );

						outputProperties.add( createdProperty );
						return createdProperty;
					}
				} );
		when( context.getProperty( Mockito.anyString() ) ).thenAnswer( new Answer<Property<?>>()
		{
			@Override
			public Property<?> answer( InvocationOnMock invocation ) throws Throwable
			{
				Object[] args = invocation.getArguments();

				for( Property<?> p : outputProperties )
				{
					if( p.getKey().equals( args[0] ) )
					{
						return p;
					}
				}
				return null;
			}
		} );
		return context;
	}

	public WsdlTestCase createTestCaseMock()
	{

		WsdlTestCase testCase = mock( WsdlTestCase.class );
		when( testCase.getPropertyList() ).thenReturn( inputProperties );

		Mockito.doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{

				for( TestProperty p : inputProperties )
				{
					if( p.getName().equals( invocation.getArguments()[0] ) )
					{
						p.setValue( ( String )invocation.getArguments()[1] );
						return null;
					}
				}

				return null;
			}
		} ).when( testCase ).setPropertyValue( Mockito.anyString(), Mockito.anyString() );

		return testCase;
	}

	public SoapUISamplerComponent createComponentMock()
	{
		WsdlTestCase testcase = createTestCaseMock();

		SoapUISamplerComponent component = mock( SoapUISamplerComponent.class );
		when( component.getTestCase() ).thenReturn( testcase );

		return component;
	}

	public void resetTable()
	{

		Platform.runLater( new Runnable()
		{

			@Override
			public void run()
			{
				resetProperties();
				Callable<Node> propertiesNode = TestCasePropertiesNode.createTableView( createComponentMock(),
						createContextMock() );

				try
				{
					box.getChildren().setAll( propertiesNode.call() );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
		FXTestUtils.awaitEvents();
	}

	public static void resetProperties()
	{
		inputProperties.clear();
		inputProperties.add( new TestTestProperty( "p1", "valuep-1" ) );
		inputProperties.add( new TestTestProperty( "p2", "valuep-2" ) );
		inputProperties.add( new TestTestProperty( "p3", "valuep-3" ) );
		inputProperties.add( new TestTestProperty( "p4", "valuep-4" ) );
		inputProperties.add( new TestTestProperty( "p5", "valuep-5" ) );
	}

	public static void setIndexStyleclassToCells()
	{
		Set<Node> rows = TestFX.findAll( ".table-row-cell" );
		;
		int n = 0;
		for( Node row : rows )
		{
			if( row instanceof TableRow )
			{
				TableRow tRow = ( TableRow )row;

				( ( TableRowSkin )tRow.getChildrenUnmodifiable().get( 0 ) ).getChildrenUnmodifiable().get( 0 )
						.getStyleClass().add( "column0-" + "row" + n );
				( ( TableRowSkin )tRow.getChildrenUnmodifiable().get( 0 ) ).getChildrenUnmodifiable().get( 1 )
						.getStyleClass().add( "column1-" + "row" + n );
				n++ ;
			}
		}
	}

	@Before
	public void setup()
	{
		outputProperties.clear();
		resetTable();
		setIndexStyleclassToCells();
	}

	@Test
	public void shouldDisplayAValue()
	{
		LabeledText text = ( LabeledText )TestFX.find( ".text", TestFX.find( ".column0-row0" ) );
		assertEquals( "p1", text.getText() );
	}

	@Test
	public void shouldDisplayAllValues()
	{
		assertEquals( "p1", getText( ".column0-row0" ) );
		assertEquals( "p2", getText( ".column0-row1" ) );
		assertEquals( "p3", getText( ".column0-row2" ) );
		assertEquals( "p4", getText( ".column0-row3" ) );
		assertEquals( "p5", getText( ".column0-row4" ) );

		assertEquals( "valuep-1", getText( ".column1-row0" ) );
		assertEquals( "valuep-2", getText( ".column1-row1" ) );
		assertEquals( "valuep-3", getText( ".column1-row2" ) );
		assertEquals( "valuep-4", getText( ".column1-row3" ) );
		assertEquals( "valuep-5", getText( ".column1-row4" ) );
	}

	@Test
	public void onlySecoundColoumnShouldBeEditable()
	{
		editCell( ".column0-row0", "new" );
		assertThat( getText( ".column0-row0" ), not( "new" ) );

		controller.click( ".column1-row1", MouseButton.PRIMARY );

		editCell( ".column1-row0", "new" );
		assertThat( getText( ".column1-row0" ), is( "new" ) );
	}

	@Test
	public void editCommitShouldChangeProperty()
	{
		editCell( ".column1-row3", "edit1" );
		assertThat( ( String )outputProperties.get( 0 ).getValue(), is( "edit1" ) );

		editCell( ".column1-row2", "edit2" );
		assertThat( ( String )outputProperties.get( 1 ).getValue(), is( "edit2" ) );
	}

	@Test
	public void outputPropertiesShouldHaveOvveriddebPrefix()
	{
		editCell( ".column1-row3", "edit1" );
		assertTrue( outputProperties.get( 0 ).getKey().startsWith( TestCasePropertiesNode.OVERRIDING_VALUE_PREFIX ) );
	}

	@Test
	public void editShouldBeStoredAfterUnFocus()
	{
		typeInNotYetSelectedCell( ".column1-row4", "new" );
		controller.click( ".column1-row3" );

		assertThat( getText( ".column1-row4" ), is( "new" ) );
		assertThat( ( String )outputProperties.get( 0 ).getValue(), is( "new" ) );
	}

	@Test
	public void savedPropertiesShouldBeUsed()
	{
		outputProperties.clear();
		outputProperties.add( new TestingProperty<>( String.class, TestCasePropertiesNode.OVERRIDING_VALUE_PREFIX + "p2",
				"newValue" ) );
		resetTable();
		setIndexStyleclassToCells();
		assertEquals( "newValue", getText( ".column1-row1" ) );
	}

	private void editCell( String selector, String newValue )
	{
		typeInNotYetSelectedCell( selector, newValue );
		controller.press( KeyCode.ENTER );
		controller.release( KeyCode.ENTER );
	}

	private void typeInNotYetSelectedCell( String selector, String newValue )
	{
		controller.move( selector ).doubleClick( MouseButton.PRIMARY ).click( MouseButton.PRIMARY );
		controller.type( newValue );
	}

	private String getText( String selector )
	{
		return ( ( Text )TestFX.find( ".text", TestFX.find( selector ) ) ).getText();
	}

}
