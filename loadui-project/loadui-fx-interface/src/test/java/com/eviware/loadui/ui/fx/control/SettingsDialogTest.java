package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.GroupBuilder;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTabBuilder;
import com.eviware.loadui.ui.fx.util.TestingProperty;
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class SettingsDialogTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static ControllerApi controller;
	private static SettingsDialog settingsDialog;
	private static Button openDialogButton;
	private static final Property<String> stringProperty = new TestingProperty<>( String.class,
			SettingsDialogTest.class.getSimpleName() + ".stringProperty", "Old value" );
	private static final Property<Boolean> booleanProperty = new TestingProperty<>( Boolean.class,
			SettingsDialogTest.class.getSimpleName() + ".booleanProperty", false );
	private static final Property<Long> longProperty = new TestingProperty<>( Long.class,
			SettingsDialogTest.class.getSimpleName() + ".longProperty", 123L );
	private static SettingsTab otherTab;
	private static SettingsTab generalTab;

	protected static final Logger log = LoggerFactory.getLogger( SettingsDialogTest.class );

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = ControllerApi.wrap( new FXScreenController() );
		FXTestUtils.launchApp( SettingsDialogTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setUp()
	{
		controller.click( openDialogButton );
	}

	@Test
	public void changedFieldsInAllTabs_should_updateProperties_onSave() throws Exception
	{
		controller.click( "#my-string" ).press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A );
		Thread.sleep( 100 );
		controller.type( "New value" ).click( "#my-boolean" );
		generalTab.getTabPane().getSelectionModel().select( otherTab );
		Thread.sleep( 100 );
		controller.click( "#my-long" ).press( KeyCode.CONTROL, KeyCode.A ).release( KeyCode.CONTROL, KeyCode.A );
		Thread.sleep( 100 );
		controller.type( "4711" ).click( "#default" );

		assertEquals( "New value", stringProperty.getValue() );
		assertEquals( true, booleanProperty.getValue() );
		assertEquals( new Long( 4711 ), longProperty.getValue() );
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

			generalTab = SettingsTabBuilder.create( "General" ).field( "My string", stringProperty )
					.field( "My boolean", booleanProperty ).build();

			otherTab = SettingsTabBuilder.create( "Other" ).field( "My long", longProperty ).build();

			settingsDialog = new SettingsDialog( openDialogButton, "Hej", Lists.newArrayList( generalTab, otherTab ) );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}
}