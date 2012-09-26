package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.bindContentUnordered;
import static com.eviware.loadui.ui.fx.util.ObservableLists.filter;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofServices;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;

import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.input.Movable;
import com.eviware.loadui.ui.fx.api.input.MultiMovable;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.canvas.terminal.ConnectionView;
import com.eviware.loadui.ui.fx.views.canvas.terminal.TerminalView;
import com.eviware.loadui.ui.fx.views.canvas.terminal.Wire;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CanvasView extends StackPane
{
	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( CanvasView.class );
	private static final Effect selectedEffect = new Glow( 0.5 );
	private static final int GRID_SIZE = 36;
	private static final double PADDING = 100;
	private final UninstallCanvasObjectView uninstallCanvasObject = new UninstallCanvasObjectView();

	private final Function<ComponentItem, ComponentView> COMPONENT_TO_VIEW = Functions.compose(
			new InitializeCanvasObjectView<ComponentView>(), new Function<ComponentItem, ComponentView>()
			{
				@Override
				public ComponentView apply( ComponentItem input )
				{
					return new ComponentView( input );
				}
			} );
	private final Function<SceneItem, ScenarioView> SCENARIO_TO_VIEW = Functions.compose(
			new InitializeCanvasObjectView<ScenarioView>(), new Function<SceneItem, ScenarioView>()
			{
				@Override
				public ScenarioView apply( SceneItem input )
				{
					return new ScenarioView( input );
				}
			} );

	private final Function<Connection, ConnectionView> CONNECTION_TO_VIEW = new Function<Connection, ConnectionView>()
	{
		@Override
		public ConnectionView apply( Connection connection )
		{
			final OutputTerminal outputTerminal = connection.getOutputTerminal();
			ComponentView outputComponentView = Iterables.find( components, new Predicate<ComponentView>()
			{
				@Override
				public boolean apply( ComponentView input )
				{
					return input.getCanvasObject().equals( outputTerminal.getTerminalHolder() );
				}
			} );

			final InputTerminal inputTerminal = connection.getInputTerminal();
			ComponentView inputComponentView = Iterables.find( components, new Predicate<ComponentView>()
			{
				@Override
				public boolean apply( ComponentView input )
				{
					return input.getCanvasObject().equals( inputTerminal.getTerminalHolder() );
				}
			} );

			return new ConnectionView( connection, outputComponentView, inputComponentView );
		}
	};

	private static final Function<ComponentDescriptor, ComponentDescriptorView> DESCRIPTOR_TO_VIEW = new Function<ComponentDescriptor, ComponentDescriptorView>()
	{
		@Override
		public ComponentDescriptorView apply( ComponentDescriptor input )
		{
			ComponentDescriptorView view = new ComponentDescriptorView( input );
			String category = input.getCategory();
			ToolBox.setCategory( view, category.substring( 0, 1 ).toUpperCase() + category.substring( 1 ).toLowerCase() );

			return view;
		}
	};

	private static final Predicate<ComponentDescriptor> NOT_DEPRECATED = new Predicate<ComponentDescriptor>()
	{
		@Override
		public boolean apply( ComponentDescriptor input )
		{
			return !input.isDeprecated();
		}
	};

	private final CanvasItem canvas;
	private final ObservableList<ComponentView> components;
	private final ObservableList<ScenarioView> scenarios;
	private final ObservableList<ConnectionView> connections;

	private final Group canvasLayer = new Group();
	private final Group componentLayer = new Group();
	private final Group connectionLayer = new Group();

	public CanvasView( CanvasItem canvas )
	{
		this.canvas = canvas;
		components = transform(
				fx( ofCollection( canvas, CanvasItem.COMPONENTS, ComponentItem.class, canvas.getComponents() ) ),
				COMPONENT_TO_VIEW );
		connections = transform(
				fx( ofCollection( canvas, CanvasItem.CONNECTIONS, Connection.class, canvas.getConnections() ) ),
				CONNECTION_TO_VIEW );

		scenarios = transform( fx( ofCollection( canvas, ProjectItem.SCENES, SceneItem.class, canvas.getChildren() ) ),
				SCENARIO_TO_VIEW );

		components.addListener( uninstallCanvasObject );
		scenarios.addListener( uninstallCanvasObject );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		bindContentUnordered( componentLayer.getChildren(), ObservableLists.concat( components, scenarios ) );
		bindContentUnordered( connectionLayer.getChildren(), connections );

		ToolBox<ComponentDescriptorView> descriptors = new ToolBox<>( "Components" );
		descriptors.setMaxWidth( 100 );
		StackPane.setAlignment( descriptors, Pos.CENTER_LEFT );
		StackPane.setMargin( descriptors, new Insets( 10, 0, 10, 0 ) );

		Bindings.bindContent( descriptors.getItems(),
				fx( transform( filter( ofServices( ComponentDescriptor.class ), NOT_DEPRECATED ), DESCRIPTOR_TO_VIEW ) ) );

		Pane componentWrapper = new Pane();
		Rectangle clipRect = new Rectangle();
		clipRect.widthProperty().bind( componentWrapper.widthProperty() );
		clipRect.heightProperty().bind( componentWrapper.heightProperty() );
		componentWrapper.setClip( clipRect );
		canvasLayer.getChildren().addAll( connectionLayer, componentLayer );
		componentWrapper.getChildren().add( canvasLayer );

		componentWrapper.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( final DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED
						&& event.getData() instanceof ComponentDescriptor )
				{
					event.accept();
					event.consume();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					final ComponentDescriptor descriptor = ( ComponentDescriptor )event.getData();

					final Task<ComponentItem> createComponent = new Task<ComponentItem>()
					{
						@Override
						protected ComponentItem call() throws Exception
						{
							updateMessage( "Creating component: " + descriptor.getLabel() );

							ComponentItem component = CanvasView.this.canvas.createComponent( descriptor.getLabel(),
									descriptor );
							Point2D position = canvasLayer.sceneToLocal( event.getSceneX(), event.getSceneY() );
							component.setAttribute( "gui.layoutX", String.valueOf( ( int )position.getX() ) );
							component.setAttribute( "gui.layoutY", String.valueOf( ( int )position.getY() ) );

							return component;
						}
					};

					createComponent.setOnFailed( new EventHandler<WorkerStateEvent>()
					{
						@Override
						public void handle( WorkerStateEvent stateEvent )
						{
							createComponent.getException().printStackTrace();
						}
					} );

					fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, createComponent ) );

					event.consume();
				}
			}
		} );

		componentLayer.addEventFilter( DraggableEvent.ANY, new ConnectionDraggingFilter() );

		Slider zoomSlider = SliderBuilder.create().min( 0.1 ).max( 1.0 ).value( 1.0 ).maxWidth( 100 ).build();
		canvasLayer.scaleXProperty().bind( zoomSlider.valueProperty() );
		canvasLayer.scaleYProperty().bind( zoomSlider.valueProperty() );
		StackPane.setAlignment( zoomSlider, Pos.BOTTOM_RIGHT );

		setAlignment( Pos.TOP_LEFT );

		getChildren().addAll( createGrid(), componentWrapper, zoomSlider, descriptors );

		Selectable.install( this );
		initScrolling();
	}

	private Node createGrid()
	{
		Region gridRegion = RegionBuilder.create().styleClass( "canvas-view" ).build();
		//Hack for setting CSS resources within an OSGi framework
		String gridUrl = CanvasView.class.getResource( "grid-box.png" ).toExternalForm();
		gridRegion.setStyle( "-fx-background-image: url('" + gridUrl + "');" );

		gridRegion.translateXProperty().bind( Bindings.createDoubleBinding( new Callable<Double>()
		{
			@Override
			public Double call() throws Exception
			{
				return canvasLayer.getLayoutX() % GRID_SIZE;
			}
		}, canvasLayer.layoutXProperty() ) );
		gridRegion.translateYProperty().bind( Bindings.createDoubleBinding( new Callable<Double>()
		{
			@Override
			public Double call() throws Exception
			{
				return canvasLayer.getLayoutY() % GRID_SIZE;
			}
		}, canvasLayer.layoutYProperty() ) );

		return StackPaneBuilder.create().padding( new Insets( -GRID_SIZE ) ).children( gridRegion ).build();
	}

	double startX = 0;
	double startY = 0;
	boolean dragging = false;

	private void initScrolling()
	{
		addEventHandler( MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.isShortcutDown() )
				{
					dragging = true;
					startX = canvasLayer.getLayoutX() - event.getX();
					startY = canvasLayer.getLayoutY() - event.getY();
				}
			}
		} );
		addEventHandler( MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.isShortcutDown() && dragging )
				{
					canvasLayer.setLayoutX( startX + event.getX() );
					canvasLayer.setLayoutY( startY + event.getY() );
					enforceCanvasBounds();
				}
			}
		} );
		addEventHandler( MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				dragging = false;
				enforceCanvasBounds();
			}
		} );

		setOnScroll( new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle( ScrollEvent event )
			{
				canvasLayer.setLayoutX( canvasLayer.getLayoutX() + event.getDeltaX() );
				canvasLayer.setLayoutY( canvasLayer.getLayoutY() + event.getDeltaY() );
				enforceCanvasBounds();
			}
		} );
	}

	private void enforceCanvasBounds()
	{
		Bounds bounds = canvasLayer.getBoundsInLocal();
		if( bounds.getWidth() == -1 )
		{
			return;
		}

		double minX = -bounds.getMinX() + PADDING;
		double maxX = getWidth() - bounds.getMaxX() - PADDING;
		double minY = -bounds.getMinY() + PADDING;
		double maxY = getHeight() - bounds.getMaxY() - PADDING;

		double layoutX = canvasLayer.getLayoutX();
		double layoutY = canvasLayer.getLayoutY();

		if( minX > maxX )
		{
			if( layoutX > minX )
			{
				canvasLayer.setLayoutX( minX );
			}
			else if( layoutX < maxX )
			{
				canvasLayer.setLayoutX( maxX );
			}
		}
		else
		{
			if( layoutX < minX )
			{
				canvasLayer.setLayoutX( minX );
			}
			else if( layoutX > maxX )
			{
				canvasLayer.setLayoutX( maxX );
			}
		}

		if( minY > maxY )
		{
			if( layoutY > minY )
			{
				canvasLayer.setLayoutY( minY );
			}
			else if( layoutY < maxY )
			{
				canvasLayer.setLayoutY( maxY );
			}
		}
		else
		{
			if( layoutY < minY )
			{
				canvasLayer.setLayoutY( minY );
			}
			else if( layoutY > maxY )
			{
				canvasLayer.setLayoutY( maxY );
			}
		}
	}

	public CanvasItem getCanvas()
	{
		return canvas;
	}

	private final class UninstallCanvasObjectView implements ListChangeListener<CanvasObjectView>
	{
		@Override
		public void onChanged( ListChangeListener.Change<? extends CanvasObjectView> change )
		{
			while( change.next() )
			{
				if( change.wasRemoved() )
				{
					for( CanvasObjectView component : change.getRemoved() )
					{
						Movable.uninstall( component );
						Selectable.uninstall( CanvasView.this, component );
						MultiMovable.uninstall( CanvasView.this, component );
					}
				}
			}
		}
	}

	private final class InitializeCanvasObjectView<T extends CanvasObjectView> implements Function<T, T>
	{
		@Override
		public T apply( final T view )
		{
			final CanvasObjectItem item = view.getCanvasObject();
			view.setLayoutX( Integer.parseInt( item.getAttribute( "gui.layoutX", "0" ) ) );
			view.setLayoutY( Integer.parseInt( item.getAttribute( "gui.layoutY", "0" ) ) );

			final Node handle = view.lookup( "#base" );
			final Movable movable = Movable.install( view, handle );
			movable.draggingProperty().addListener( new ChangeListener<Boolean>()
			{
				@Override
				public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue )
				{
					if( !newValue )
					{
						item.setAttribute( "gui.layoutX", String.valueOf( ( int )view.getLayoutX() ) );
						item.setAttribute( "gui.layoutY", String.valueOf( ( int )view.getLayoutY() ) );
						enforceCanvasBounds();
					}
				}
			} );
			Selectable selectable = Selectable.install( CanvasView.this, view );
			view.effectProperty().bind(
					Bindings.when( selectable.selectedProperty() ).then( selectedEffect ).otherwise( ( Effect )null ) );

			MultiMovable.install( CanvasView.this, view );

			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					enforceCanvasBounds();
				}
			} );
			return view;
		}
	}

	private class ConnectionDraggingFilter implements EventHandler<DraggableEvent>
	{
		private final Wire wire = new Wire();
		private Point2D startPoint = new Point2D( 0, 0 );
		private Terminal originalData = null;
		private ConnectionView connectionView = null;

		public ConnectionDraggingFilter()
		{
			wire.setFill( Color.LIGHTGRAY );
			wire.setVisible( false );

			canvasLayer.getChildren().add( 0, wire );
		}

		@Override
		public void handle( DraggableEvent event )
		{
			if( event.getData() instanceof Terminal )
			{
				final Terminal draggedTerminal = ( Terminal )event.getData();
				DragNode dragNode = ( DragNode )event.getDraggable();

				if( event.getEventType() == DraggableEvent.DRAGGABLE_STARTED )
				{
					connectionView = Iterables.find( connections, new Predicate<ConnectionView>()
					{
						@Override
						public boolean apply( ConnectionView input )
						{
							//Dragging the OutputTerminal (only connection) of a Connection, OR dragging the InputTerminal of a selected Connection:
							return draggedTerminal.equals( input.getConnection().getOutputTerminal() )
									|| draggedTerminal.equals( input.getConnection().getInputTerminal() ) && input.isSelected();
						}
					}, null );

					if( connectionView != null )
					{
						connectionView.setVisible( false );
						TerminalView otherTerminalView = draggedTerminal instanceof InputTerminal ? connectionView
								.getOutputTerminalView() : connectionView.getInputTerminalView();

						Bounds startBounds = canvasLayer.sceneToLocal( otherTerminalView.localToScene( otherTerminalView
								.getBoundsInLocal() ) );

						startPoint = new Point2D( ( startBounds.getMinX() + startBounds.getMaxX() ) / 2,
								( startBounds.getMinY() + startBounds.getMaxY() ) / 2 );

						originalData = draggedTerminal;
						dragNode.setData( otherTerminalView.getTerminal() );
					}
					else
					{
						Node source = ( ( DragNode )event.getDraggable() ).getDragSource();
						Bounds startBounds = canvasLayer.sceneToLocal( source.localToScene( source.getBoundsInLocal() ) );

						startPoint = new Point2D( ( startBounds.getMinX() + startBounds.getMaxX() ) / 2,
								( startBounds.getMinY() + startBounds.getMaxY() ) / 2 );

						originalData = null;
						connectionView = null;
					}

					Point2D endPoint = canvasLayer.sceneToLocal( event.getSceneX(), event.getSceneY() );

					wire.updatePosition( startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY() );
					wire.setReversed( event.getData() instanceof InputTerminal );
					wire.setVisible( true );
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DRAGGED )
				{
					Point2D endPoint = canvasLayer.sceneToLocal( event.getSceneX(), event.getSceneY() );
					wire.updatePosition( startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY() );
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_STOPPED )
				{
					wire.setVisible( false );
					if( originalData != null )
					{
						dragNode.setData( originalData );
					}
					if( connectionView != null )
					{
						//If dropped on (or near enough) the original TerminalView, do not delete the Connection:
						Node source = ( ( DragNode )event.getDraggable() ).getDragSource();
						Bounds startBounds = source.localToScene( source.getBoundsInLocal() );
						Point2D endPoint = new Point2D( event.getSceneX(), event.getSceneY() );
						double distance = endPoint.distance( new Point2D(
								( startBounds.getMinX() + startBounds.getMaxX() ) / 2, ( startBounds.getMinY() + startBounds
										.getMaxY() ) / 2 ) );

						if( distance > 10 )
						{
							connectionView.delete();
						}
						else
						{
							connectionView.setVisible( true );
						}
					}
				}
			}
		}
	}
}
