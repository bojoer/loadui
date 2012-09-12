package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.GroupBuilder;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
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
	private static final Property<String> property = new TestingProperty<String>( String.class,
			SettingsDialogTest.class.getSimpleName() + ".prop1", "Old value" );

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
	public void addTab_oneStringPropertyGiven_propertySavedSucessfully() throws Exception
	{
		controller.press( KeyCode.TAB ).type( "New value" ).press( KeyCode.ENTER );
		assertEquals( property.getValue(), "New value" );
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

			SettingsTab generalTab = SettingsTabBuilder.create( "General" ).textField( "Foo", property ).build();

			settingsDialog = new SettingsDialog( openDialogButton, "Hej", Lists.newArrayList( generalTab ) );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}

	}
}