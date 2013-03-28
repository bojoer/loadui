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
package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.GroupBuilder;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.impl.layout.ActionLayoutComponentImpl;
import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.control.SettingsTab.Builder;
import com.eviware.loadui.ui.fx.util.StylingUtils;
import com.eviware.loadui.ui.fx.util.TestingProperty;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class SettingsDialogTest
{
	private static final long INIT_LONG = 123L;
	private static final boolean INIT_BOOLEAN = false;
	private static final String INIT_STRING = "Old value";
	private static final String CONNECTED = "Successfully connected to SkyNet!";

	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static TestFX controller;
	private static SettingsDialog settingsDialog;
	private static Button openDialogButton;
	private static final Property<String> stringProperty = new TestingProperty<>( String.class,
			SettingsDialogTest.class.getSimpleName() + ".stringProperty", INIT_STRING );
	private static final Property<Boolean> booleanProperty = new TestingProperty<>( Boolean.class,
			SettingsDialogTest.class.getSimpleName() + ".booleanProperty", INIT_BOOLEAN );
	private static final Property<Long> longProperty = new TestingProperty<>( Long.class,
			SettingsDialogTest.class.getSimpleName() + ".longProperty", INIT_LONG );
	private static final Property<Long> longProperty2 = new TestingProperty<>( Long.class,
			SettingsDialogTest.class.getSimpleName() + ".longProperty2", INIT_LONG );
	private static SettingsTab otherTab;
	private static SettingsTab generalTab;

	protected static final Logger log = LoggerFactory.getLogger( SettingsDialogTest.class );

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( SettingsDialogTestApp.class );

		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		TestFX.targetWindow( stage );
		FXTestUtils.bringToFront( stage );

	}

	@Before
	public void setUp() throws Exception
	{
		stringProperty.setValue( INIT_STRING );
		booleanProperty.setValue( INIT_BOOLEAN );
		longProperty.setValue( INIT_LONG );
		longProperty2.setValue( INIT_LONG );
		generalTab = Builder.create( "General" ).id( "general-tab" ).field( "My string", stringProperty )
				.field( "My boolean", booleanProperty ).build();

		final Callable<String> statusCallback = new Callable<String>()
		{
			private boolean tested = false;

			@Override
			public String call() throws Exception
			{
				if( !tested )
				{
					tested = true;
					return "Untested...";
				}
				else
				{
					tested = false;
					return CONNECTED;
				}
			}
		};

		otherTab = Builder
				.create( "Other" )
				.id( "other-tab" )
				.field( "My long", longProperty )
				.field( "My other long", longProperty2 )
				.button(
						new ActionLayoutComponentImpl( ImmutableMap.<String, Object> builder()
								.put( ActionLayoutComponentImpl.LABEL, "Test Connection" )
								.put( ActionLayoutComponentImpl.ASYNC, false )
								.put( ActionLayoutComponentImpl.ACTION, new Runnable()
								{
									@Override
									public void run()
									{
									}
								} ).put( "status", statusCallback ).build() ) ).build();

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				settingsDialog = new SettingsDialog( openDialogButton, "Hej", Lists.newArrayList( generalTab, otherTab ) );
			}
		}, 1000 );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				generalTab.getTabPane().getSelectionModel().select( generalTab );
			}
		}, 1000 );

		StylingUtils.applyLoaduiStyling( settingsDialog.getScene() );

		controller.click( openDialogButton );

		generalTab.getTabPane().getSelectionModel().select( generalTab );
		controller.click( "#general-tab" );
	}

	@Test
	public void changedFieldsInAllTabs_should_updateProperties_onSave() throws Exception
	{
		System.out.println( "generalTab.getTabPane().getSelectionModel().getSelectedItem().getText(): "
				+ generalTab.getTabPane().getSelectionModel().getSelectedItem().getText() );

		controller.click( "#my-string" ).press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A )
				.sleep( 100 );
		controller.type( "New value" ).click( "#my-boolean" );

		generalTab.getTabPane().getSelectionModel().select( otherTab );
		controller.sleep( 100 ).click( "#my-long" ).press( KeyCode.CONTROL, KeyCode.A )
				.release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 );
		controller.type( "4711" ).click( "#default" );

		assertEquals( "New value", stringProperty.getValue() );
		assertEquals( true, booleanProperty.getValue() );
		assertEquals( Long.valueOf( 4711 ), longProperty.getValue() );
	}

	@Test
	public void actionComponentShouldBePlacedCorrectlyAndCallbackUsedWhenClicked()
	{
		try
		{
			generalTab.getTabPane().getSelectionModel().select( otherTab );
			Button testConnection = ( ( Button )otherTab.getContent().lookup( "#test-connection" ) );
			assertEquals( "Untested...", ( ( Text )otherTab.getContent().lookup( "#status" ) ).getText() );
			testConnection.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					( ( Text )otherTab.getContent().lookup( "#status" ) ).setText( CONNECTED );
				}
			} );

			controller.click( "#other-tab" ).sleep( 500 );
			controller.click( "#test-connection" ).sleep( 1000 );
			assertEquals( CONNECTED, ( ( Text )otherTab.getContent().lookup( "#status" ) ).getText() );
			controller.click( "#default" );
			assertEquals( false, settingsDialog.isShowing() );

		}
		catch( Exception e )
		{
			org.junit.Assert.fail( "Label or Button of ActionLayoutComponent missplaced or missing." );
		}
	}

	@Test
	public void invalidNumbers_should_promptError_onSave() throws Exception
	{
		generalTab.getTabPane().getSelectionModel().select( otherTab );
		controller.click( "#my-long" ).press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A )
				.sleep( 100 );
		controller.type( "not a number" ).click( "#default" );

		assertEquals( true, settingsDialog.isShowing() );
		assertFalse( Long.valueOf( 4711 ).equals( longProperty.getValue() ) );

		controller.sleep( 100 ).click( "#my-long" ).press( KeyCode.CONTROL, KeyCode.A )
				.release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 ).type( "4711" ).sleep( 100 ).click( "#my-other-long" )
				.press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 )
				.type( "not a number" ).click( "#default" );
		assertEquals( true, settingsDialog.isShowing() );

		controller.click( "#my-other-long" ).press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A )
				.sleep( 100 ).type( "7" ).click( "#default" );
		assertEquals( false, settingsDialog.isShowing() );
	}

	@Ignore
	@Test
	public void singleTab_should_notDisplayTabPane() throws Exception
	{
		final Tab t = generalTab.getTabPane().getTabs().remove( 1 );
		controller.sleep( 2500 );
		final Region tabHeader = ( Region )generalTab.getTabPane().lookup( ".tab-header-area" );
		assertEquals( tabHeader.getHeight(), 0.0, 0.005 );
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				generalTab.getTabPane().getTabs().add( t );
			}
		} );
		controller.sleep( 500 );
	}

	public static class SettingsDialogTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			openDialogButton = new Button( "Open dialog" );
			openDialogButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					settingsDialog.show();
				}
			} );

			primaryStage.setScene( SceneBuilder.create().width( 800 ).height( 600 )
					.root( GroupBuilder.create().children( openDialogButton ).build() ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}
}
