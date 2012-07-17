package com.eviware.loadui.ui.fx.control;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.util.concurrent.SettableFuture;

public class ToolBoxTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static ControllerApi controller;

	public static class ToolboxTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			ToolBox<Rectangle> toolbox = new ToolBox<>();
			toolbox.getItems().setAll( RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.GREEN ).build(),
					RectangleBuilder.create().width( 75 ).height( 50 ).fill( Color.YELLOW ).build() );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 200 ).root( toolbox ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = ControllerApi.wrap( new FXScreenController() );
		FXTestUtils.launchApp( ToolboxTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.use( stage );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void shouldTest() throws Exception
	{
		//TODO: Real test here.
		controller.click( ".tool-box" );
	}
}
