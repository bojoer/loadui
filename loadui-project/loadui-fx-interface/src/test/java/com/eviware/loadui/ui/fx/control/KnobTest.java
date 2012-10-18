package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static com.eviware.loadui.ui.fx.util.test.TestFX.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.GroupBuilder;
import javafx.scene.SceneBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBoxBuilder;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class KnobTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static TestFX controller;

	public static class KnobTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Knob boundedKnob = new Knob( "Bounded", 0, 10, 0 );
			boundedKnob.setId( "bounded" );
			Knob minKnob = new Knob( "Min" );
			minKnob.setMin( 0 );
			minKnob.setId( "min" );
			Knob maxKnob = new Knob( "Max" );
			maxKnob.setMax( 0 );
			maxKnob.setId( "max" );

			primaryStage.setScene( SceneBuilder
					.create()
					.stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 200 )
					.height( 100 )
					.root(
							GroupBuilder.create()
									.children( HBoxBuilder.create().children( boundedKnob, minKnob, maxKnob ).build() ).build() )
					.build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( KnobTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup()
	{
		( ( Knob )find( "#bounded" ) ).setValue( 0.0 );
		( ( Knob )find( "#min" ) ).setValue( 0.0 );
		( ( Knob )find( "#max" ) ).setValue( 0.0 );
	}

	@Test
	public void onlyBoundedShouldHaveBoundedStyleClass()
	{
		assertThat( find( "#bounded" ).getStyleClass().contains( "bounded" ), is( true ) );
		assertThat( find( "#min" ).getStyleClass().contains( "bounded" ), is( false ) );
		assertThat( find( "#max" ).getStyleClass().contains( "bounded" ), is( false ) );
	}

	@Test
	public void shouldBeModifiableByDragging()
	{
		Knob bounded = find( "#bounded" );
		//Drag down first to initiate dragging.
		controller.drag( bounded ).by( 0, 10 ).by( 0, -5 ).drop();
		assertThat( bounded.getValue(), closeTo( 5.0, 0.01 ) );
	}

	@Test
	public void shouldBeModifiableByManualEntry()
	{
		Knob bounded = find( "#bounded" );
		controller.click( bounded ).click( bounded ).sleep( 100 ).type( "5" ).press( KeyCode.ENTER );
		assertThat( bounded.getValue(), closeTo( 5.0, 0.01 ) );
	}

	@Test
	public void shouldBeModifiableByScrolling()
	{
		Knob bounded = find( "#bounded" );
		controller.move( bounded );
		for( int x = 0; x < 10; x++ )
		{
			controller.scroll( 1 );
		}
		assertThat( bounded.getValue(), closeTo( 0.0, 0.01 ) );

		for( int x = 0; x < 10; x++ )
		{
			controller.scroll( -1 );
		}
		assertThat( bounded.getValue(), closeTo( 10.0, 0.01 ) );

		Knob minKnob = find( "#min" );
		controller.move( minKnob );
		for( int x = 0; x < 10; x++ )
		{
			controller.scroll( 1 );
		}
		assertThat( minKnob.getValue(), closeTo( 0.0, 0.01 ) );

		for( int x = 0; x < 11; x++ )
		{
			controller.scroll( -1 );
		}
		assertThat( minKnob.getValue(), greaterThan( 10.0 ) );

		Knob maxKnob = find( "#max" );
		controller.move( maxKnob );
		for( int x = 0; x < 10; x++ )
		{
			controller.scroll( -1 );
		}
		assertThat( maxKnob.getValue(), closeTo( 0.0, 0.01 ) );

		for( int x = 0; x < 11; x++ )
		{
			controller.scroll( 1 );
		}
		assertThat( maxKnob.getValue(), lessThan( -10.0 ) );
	}
}
