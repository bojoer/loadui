package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.bindContentUnordered;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofServices;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Movable;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.google.common.base.Function;

public class CanvasView extends StackPane
{
	private static final Function<ComponentItem, ComponentView> COMPONENT_TO_VIEW = new Function<ComponentItem, ComponentView>()
	{
		@Override
		public ComponentView apply( final ComponentItem input )
		{
			final ComponentView componentView = new ComponentView( input );
			componentView.setLayoutX( Integer.parseInt( input.getAttribute( "gui.layoutX", "0" ) ) );
			componentView.setLayoutY( Integer.parseInt( input.getAttribute( "gui.layoutY", "0" ) ) );
			Movable movable = Movable.install( componentView, componentView.lookup( "#label" ) );
			movable.draggingProperty().addListener( new ChangeListener<Boolean>()
			{
				@Override
				public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue )
				{
					if( !newValue )
					{
						input.setAttribute( "gui.layoutX", String.valueOf( ( int )componentView.getLayoutX() ) );
						input.setAttribute( "gui.layoutY", String.valueOf( ( int )componentView.getLayoutY() ) );
					}
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
			ToolBox.setCategory( view, input.getCategory() );

			return view;
		}
	};

	private final CanvasItem canvas;
	private final ObservableList<ComponentView> components;

	public CanvasView( CanvasItem canvas )
	{
		this.canvas = canvas;

		components = transform(
				fx( ofCollection( canvas, CanvasItem.COMPONENTS, ComponentItem.class, canvas.getComponents() ) ),
				COMPONENT_TO_VIEW );

		final Group componentLayer = new Group();
		bindContentUnordered( componentLayer.getChildren(), components );

		ToolBox<ComponentDescriptorView> descriptors = new ToolBox<>( "Components" );
		descriptors.setMaxWidth( 90 );
		StackPane.setAlignment( descriptors, Pos.CENTER_LEFT );
		StackPane.setMargin( descriptors, new Insets( 10, 0, 10, 0 ) );

		Bindings.bindContent( descriptors.getItems(),
				fx( transform( ofServices( ComponentDescriptor.class ), DESCRIPTOR_TO_VIEW ) ) );

		Slider posSlider = SliderBuilder.create().min( -1000 ).max( 1000 ).value( 0 ).maxWidth( 100 ).build();
		StackPane.setAlignment( posSlider, Pos.BOTTOM_CENTER );

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

		componentLayer.layoutXProperty().bind( posSlider.valueProperty().multiply( -1 ) );

		setAlignment( Pos.TOP_LEFT );
		getChildren().addAll( RegionBuilder.create().style( "-fx-background-color: lightgray;" ).build(),
				componentWrapper, zoomSlider, posSlider, descriptors );
	}

	public CanvasItem getCanvas()
	{
		return canvas;
	}
}
