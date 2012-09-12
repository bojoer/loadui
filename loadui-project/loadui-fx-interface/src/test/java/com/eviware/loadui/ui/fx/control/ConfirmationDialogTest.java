package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.GroupBuilder;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class ConfirmationDialogTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static ControllerApi controller;
	private static Dialog dialog;
	private static Button openDialogButton;
	protected static final Logger log = LoggerFactory.getLogger( ConfirmationDialogTest.class );

	public static class DialogTestApp extends Application
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
					dialog.show();
				}
			} );

			primaryStage.setScene( SceneBuilder.create().width( 800 ).height( 600 )
					.root( GroupBuilder.create().children( openDialogButton ).build() ).build() );

			dialog = new ConfirmationDialog( openDialogButton, "My dialog", "I got it!" );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}

	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = ControllerApi.wrap( new FXScreenController() );
		FXTestUtils.launchApp( DialogTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void shouldOpen()
	{
		assertFalse( dialog.isShowing() );
		controller.click( openDialogButton );
		assertTrue( dialog.isShowing() );
	}

	@Test
	public void shouldCloseOnCancel()
	{
		controller.click( openDialogButton ).target( dialog ).click( "#cancel" );
		assertFalse( dialog.isShowing() );
	}
}
