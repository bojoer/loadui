package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.bindContentUnordered;
import static com.eviware.loadui.ui.fx.util.ObservableLists.filter;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofServices;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
import javafx.scene.shape.Rectangle;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.input.Movable;
import com.eviware.loadui.ui.fx.api.input.MultiMovable;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class CanvasView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( CanvasView.class );
	private final Effect selectedEffect = new Glow( 0.5 );
	private static final int GRID_SIZE = 36;
	private static final double PADDING = 100;

	private final Function<ComponentItem, ComponentView> COMPONENT_TO_VIEW = new Function<ComponentItem, ComponentView>()
	{
		@Override
		public ComponentView apply( final ComponentItem input )
		{
			final ComponentView componentView = new ComponentView( input );
			componentView.setLayoutX( Integer.parseInt( input.getAttribute( "gui.layoutX", "0" ) ) );
			componentView.setLayoutY( Integer.parseInt( input.getAttribute( "gui.layoutY", "0" ) ) );

			final Movable movable = Movable.install( componentView, componentView.lookup( "#label" ) );
			movable.draggingProperty().addListener( new ChangeListener<Boolean>()
			{
				@Override
				public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue )
				{
					if( !newValue )
					{
						input.setAttribute( "gui.layoutX", String.valueOf( ( int )componentView.getLayoutX() ) );
						input.setAttribute( "gui.layoutY", String.valueOf( ( int )componentView.getLayoutY() ) );
						enforceCanvasBounds();
					}
				}
			} );
			Selectable selectable = Selectable.installSelectable( componentView );
			componentView.effectProperty().bind(
					Bindings.when( selectable.selectedProperty() ).then( selectedEffect ).otherwise( ( Effect )null ) );

			MultiMovable.install( CanvasView.this, componentView );

			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					enforceCanvasBounds();
				}
			} );

			return componentView;
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

	private final Group componentLayer = new Group();

	public CanvasView( CanvasItem canvas )
	{
		this.canvas = canvas;

		FXMLUtils.load( this );

		components = transform(
				fx( ofCollection( canvas, CanvasItem.COMPONENTS, ComponentItem.class, canvas.getComponents() ) ),
				COMPONENT_TO_VIEW );

		Selectable.installDragToSelectArea( this );

		bindContentUnordered( componentLayer.getChildren(), components );

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
		componentWrapper.getChildren().add( componentLayer );

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
							Point2D position = componentLayer.sceneToLocal( event.getSceneX(), event.getSceneY() );
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

		Slider zoomSlider = SliderBuilder.create().min( 0.1 ).max( 1.0 ).value( 1.0 ).maxWidth( 100 ).build();
		componentLayer.scaleXProperty().bind( zoomSlider.valueProperty() );
		componentLayer.scaleYProperty().bind( zoomSlider.valueProperty() );
		StackPane.setAlignment( zoomSlider, Pos.BOTTOM_RIGHT );

		setAlignment( Pos.TOP_LEFT );

		getChildren().addAll( createGrid(), componentWrapper, zoomSlider, descriptors );

		initScrolling();
	}

	private Node createGrid()
	{
		Region gridRegion = RegionBuilder.create().styleClass( "canvas-view" ).build();
		gridRegion.translateXProperty().bind( Bindings.createDoubleBinding( new Callable<Double>()
		{
			@Override
			public Double call() throws Exception
			{
				return componentLayer.getLayoutX() % GRID_SIZE;
			}
		}, componentLayer.layoutXProperty() ) );
		gridRegion.translateYProperty().bind( Bindings.createDoubleBinding( new Callable<Double>()
		{
			@Override
			public Double call() throws Exception
			{
				return componentLayer.getLayoutY() % GRID_SIZE;
			}
		}, componentLayer.layoutYProperty() ) );

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
					startX = componentLayer.getLayoutX() - event.getX();
					startY = componentLayer.getLayoutY() - event.getY();
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
					componentLayer.setLayoutX( startX + event.getX() );
					componentLayer.setLayoutY( startY + event.getY() );
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
				componentLayer.setLayoutX( componentLayer.getLayoutX() + event.getDeltaX() );
				componentLayer.setLayoutY( componentLayer.getLayoutY() + event.getDeltaY() );
				enforceCanvasBounds();
			}
		} );
	}

	private void enforceCanvasBounds()
	{
		Bounds bounds = componentLayer.getBoundsInLocal();
		if( bounds.getWidth() == -1 )
		{
			return;
		}

		double minX = -bounds.getMinX() + PADDING;
		double maxX = getWidth() - bounds.getMaxX() - PADDING;
		double minY = -bounds.getMinY() + PADDING;
		double maxY = getHeight() - bounds.getMaxY() - PADDING;

		double layoutX = componentLayer.getLayoutX();
		double layoutY = componentLayer.getLayoutY();

		if( minX > maxX )
		{
			if( layoutX > minX )
			{
				componentLayer.setLayoutX( minX );
			}
			else if( layoutX < maxX )
			{
				componentLayer.setLayoutX( maxX );
			}
		}
		else
		{
			if( layoutX < minX )
			{
				componentLayer.setLayoutX( minX );
			}
			else if( layoutX > maxX )
			{
				componentLayer.setLayoutX( maxX );
			}
		}

		if( minY > maxY )
		{
			if( layoutY > minY )
			{
				componentLayer.setLayoutY( minY );
			}
			else if( layoutY < maxY )
			{
				componentLayer.setLayoutY( maxY );
			}
		}
		else
		{
			if( layoutY < minY )
			{
				componentLayer.setLayoutY( minY );
			}
			else if( layoutY > maxY )
			{
				componentLayer.setLayoutY( maxY );
			}
		}
	}

	public CanvasItem getCanvas()
	{
		return canvas;
	}
}
