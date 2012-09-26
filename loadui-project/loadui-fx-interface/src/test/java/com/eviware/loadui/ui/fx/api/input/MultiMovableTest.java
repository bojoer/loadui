package com.eviware.loadui.ui.fx.api.input;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.offset;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.ControllerApi.MouseMotion;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class MultiMovableTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Selectable selectable1;
	private static Selectable selectable2;
	private static Selectable selectable3;
	private static Stage stage;
	private static ControllerApi controller;
	private static Pane background;

	public static class SelectableTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle rect1 = RectangleBuilder.create().id( "rect1" ).width( 25 ).height( 25 ).fill( Color.BLUE ).build();
			Movable.install( rect1 );

			Rectangle rect2 = RectangleBuilder.create().id( "rect2" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();
			Movable.install( rect2 );

			StackPane stack = StackPaneBuilder.create().id( "stack" ).minHeight( 25 ).minWidth( 25 ).layoutY( 180 )
					.build();
			Rectangle rect3 = RectangleBuilder.create().width( 25 ).height( 25 ).fill( Color.DARKSLATEBLUE ).build();
			VBox handle = VBoxBuilder.create().children( rect3 ).build();
			stack.getChildren().add( handle );
			Movable.install( stack, handle );

			background = PaneBuilder.create().children( stack, rect2, rect1 ).build();

			Selectable.installDragToSelectArea( background );
			selectable1 = Selectable.installSelectable( rect1 );
			selectable2 = Selectable.installSelectable( rect2 );
			selectable3 = Selectable.installSelectable( stack );

			rect1.fillProperty().bind(
					Bindings.when( selectable1.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			rect2.fillProperty().bind(
					Bindings.when( selectable2.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 300 ).root( background ).build() );

			MultiMovable.install( background, rect1 );
			MultiMovable.install( background, rect2 );
			MultiMovable.install( background, stack );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = ControllerApi.wrap( new FXScreenController() );
		FXTestUtils.launchApp( SelectableTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@After
	public void restorePosition()
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();
		selectable1.deselect();
		selectable2.deselect();
		selectable3.deselect();
		rectangle1.setLayoutX( 0 );
		rectangle1.setLayoutY( 0 );
		rectangle2.setLayoutX( 100 );
		rectangle2.setLayoutY( 100 );
		FXTestUtils.awaitEvents();
	}

	@Test
	public void movingSelectedNode_should_moveAlongAllOtherSelectedNodes() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.drag( offset( background, 220, 170 ) ).to( offset( background, 0, 0 ) );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );

		MouseMotion motion = controller.drag( rectangle1 ).by( 100, 20 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );

		motion.drop().click( background );
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( false ) );
		assertThat( rectangle1.getLayoutX(), equalTo( 100.0 ) );
		assertThat( rectangle1.getLayoutY(), equalTo( 20.0 ) );
		assertThat( rectangle2.getLayoutX(), equalTo( 200.0 ) );
		assertThat( rectangle2.getLayoutY(), equalTo( 120.0 ) );
	}

	@Test
	public void movingUnselectedNode_shouldNot_moveAlongAllSelectedNodes() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.click( rectangle1 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( false ) );

		MouseMotion motion = controller.drag( rectangle2 ).by( 100, 20 );
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( true ) );

		motion.drop();
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( true ) );
		assertThat( rectangle1.getLayoutX(), equalTo( 0.0 ) );
		assertThat( rectangle1.getLayoutY(), equalTo( 0.0 ) );
		assertThat( rectangle2.getLayoutX(), equalTo( 200.0 ) );
		assertThat( rectangle2.getLayoutY(), equalTo( 120.0 ) );
	}

	@Test
	public void clickingOnAMovablesHandle_should_selectThatNode() throws Throwable
	{
		final Node stack = selectable3.getNode();

		controller.click( stack );
		assertThat( selectable3.isSelected(), is( true ) );

	}

}
