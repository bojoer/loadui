package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBoxBuilder;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.ui.fx.views.canvas.component.ComponentLayoutUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class OptionsSliderTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

	private static OptionsSlider optionsSlider;
	private static OptionsSlider imageOptionsSlider;
	private static Stage stage;
	private static TestFX controller;
	private static Label label;

	public static class OptionsSliderTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			optionsSlider = new OptionsSlider( ImmutableList.of( "one", "two", "three" ) );

			imageOptionsSlider = new OptionsSlider( ImmutableList.of( "gauss", "sine" ), ImmutableList.of(
					createImage( "gauss_shape.png" ), createImage( "variance2_shape.png" ) ) );

			primaryStage.titleProperty().bind( optionsSlider.selectedProperty() );

			label = new Label( "not set" );
			label.textProperty().bind( imageOptionsSlider.selectedProperty() );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 200 )
					.root( HBoxBuilder.create().spacing( 25 ).children( optionsSlider, imageOptionsSlider, label ).build() )
					.build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( OptionsSliderTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void property_should_updateOnClick()
	{
		controller.click( "#two" );
		assertTrue( "two".equals( stage.getTitle() ) );
		controller.click( "#three" );
		assertTrue( "three".equals( stage.getTitle() ) );
		controller.click( "#two" );
		assertTrue( "two".equals( stage.getTitle() ) );
		controller.click( "#one" );
		assertTrue( "one".equals( stage.getTitle() ) );
	}

	@Test
	public void images_should_work()
	{
		controller.click( "#gauss" );
		assertTrue( "gauss".equals( label.getText() ) );
		controller.click( "#sine" );
		assertTrue( "sine".equals( label.getText() ) );
	}

	private static ImageView createImage( String imageName )
	{
		return new ImageView( new Image( ComponentLayoutUtils.class.getClassLoader()
				.getResource( "images/options/" + imageName ).toExternalForm() ) );
	}
}
