package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class CanvasObjectView extends StackPane
{
	//TODO: These should obviously be InputTermialView and OutputTerminalView classes, extending some TerminalView base.
	private static final Function<InputTerminal, Node> INPUT_TERMINAL_TO_VIEW = new Function<InputTerminal, Node>()
	{
		@Override
		public Node apply( final InputTerminal terminal )
		{
			final Circle node = CircleBuilder.create().radius( 10 ).fill( Color.GREEN ).build();
			DragNode dragNode = DragNode.install( node, CircleBuilder.create().radius( 10 ).fill( Color.GREEN ).build() );
			dragNode.setData( terminal );

			node.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
			{
				@Override
				public void handle( DraggableEvent event )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED
							&& event.getData() instanceof OutputTerminal )
					{
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						OutputTerminal outputTerminal = ( OutputTerminal )event.getData();
						outputTerminal.connectTo( terminal );
						event.consume();
					}
				}
			} );

			final Tooltip tooltip = new Tooltip();
			Tooltip.install( node, tooltip );
			node.addEventHandler( MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
			{
				@Override
				public void handle( MouseEvent event )
				{
					StringBuilder sb = new StringBuilder( terminal.getLabel() + ", connected to: " );
					for( Connection connection : terminal.getConnections() )
					{
						sb.append( connection.getOutputTerminal().getLabel() ).append( ", " );
					}
					tooltip.setText( sb.toString() );
				}
			} );

			return node;
		}
	};

	private static final Function<OutputTerminal, Node> OUTPUT_TERMINAL_TO_VIEW = new Function<OutputTerminal, Node>()
	{
		@Override
		public Node apply( final OutputTerminal terminal )
		{
			final Circle node = CircleBuilder.create().radius( 10 ).fill( Color.RED ).build();
			DragNode dragNode = DragNode.install( node, CircleBuilder.create().radius( 10 ).fill( Color.RED ).build() );
			dragNode.setData( terminal );

			node.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
			{
				@Override
				public void handle( DraggableEvent event )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof InputTerminal )
					{
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						InputTerminal inputTerminal = ( InputTerminal )event.getData();
						terminal.connectTo( inputTerminal );
						event.consume();
					}
				}
			} );

			final Tooltip tooltip = new Tooltip();
			Tooltip.install( node, tooltip );
			node.addEventHandler( MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
			{
				@Override
				public void handle( MouseEvent event )
				{
					StringBuilder sb = new StringBuilder( terminal.getLabel() + ", connected to: " );
					for( Connection connection : terminal.getConnections() )
					{
						sb.append( connection.getInputTerminal().getLabel() ).append( ", " );
					}
					tooltip.setText( sb.toString() );
				}
			} );

			return node;
		}
	};

	private final CanvasObjectItem canvasObject;
	private final ObservableList<Node> inputTerminals;
	private final ObservableList<Node> outputTerminals;

	public CanvasObjectView( CanvasObjectItem canvasObject )
	{
		this.canvasObject = canvasObject;
		inputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, InputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), InputTerminal.class ) ) ), INPUT_TERMINAL_TO_VIEW );

		outputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, OutputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), OutputTerminal.class ) ) ), OUTPUT_TERMINAL_TO_VIEW );

		HBox inputTerminalHBox = HBoxBuilder.create().alignment( Pos.TOP_CENTER ).build();
		Bindings.bindContent( inputTerminalHBox.getChildren(), inputTerminals );

		HBox outputTerminalHBox = HBoxBuilder.create().alignment( Pos.BOTTOM_CENTER ).build();
		Bindings.bindContent( outputTerminalHBox.getChildren(), outputTerminals );

		getChildren().addAll(
				VBoxBuilder
						.create()
						.children( inputTerminalHBox,
								RectangleBuilder.create().width( 200 ).height( 100 ).fill( Color.BLUEVIOLET ).build(),
								outputTerminalHBox ).build() );
	}

	public CanvasObjectItem getCanvasObject()
	{
		return canvasObject;
	}
}
