package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.canvas.terminal.InputTerminalView;
import com.eviware.loadui.ui.fx.views.canvas.terminal.OutputTerminalView;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class CanvasObjectView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( CanvasObjectView.class );

	private static final Function<InputTerminal, Node> INPUT_TERMINAL_TO_VIEW = new Function<InputTerminal, Node>()
	{
		@Override
		public Node apply( final InputTerminal terminal )
		{
			InputTerminalView terminalView = new InputTerminalView( terminal );
			HBox.setHgrow( terminalView, Priority.ALWAYS );
			return terminalView;
		}
	};

	private static final Function<OutputTerminal, Node> OUTPUT_TERMINAL_TO_VIEW = new Function<OutputTerminal, Node>()
	{
		@Override
		public Node apply( final OutputTerminal terminal )
		{
			OutputTerminalView terminalView = new OutputTerminalView( terminal );
			HBox.setHgrow( terminalView, Priority.ALWAYS );
			return terminalView;
		}
	};

	@SuppressWarnings( "unchecked" )
	public static final <T extends CanvasObjectView> T newInstanceUnchecked( Class<T> type, CanvasObjectItem item )
	{
		if( item instanceof ComponentItem )
			return ( T )new ComponentView( ( ComponentItem )item );
		return ( T )new ScenarioView( ( SceneItem )item );
	}

	protected final CanvasObjectItem canvasObject;
	protected final ObservableList<Node> inputTerminals;
	protected final ObservableList<Node> outputTerminals;

	@FXML
	protected Label canvasObjectLabel;
	@FXML
	protected Pane inputTerminalPane;
	@FXML
	protected Pane outputTerminalPane;

	public CanvasObjectView( CanvasObjectItem canvasObject )
	{
		this.canvasObject = canvasObject;
		inputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, InputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), InputTerminal.class ) ) ), INPUT_TERMINAL_TO_VIEW );

		outputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, OutputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), OutputTerminal.class ) ) ), OUTPUT_TERMINAL_TO_VIEW );

		//		FXMLUtils
		//				.load( this, this, CanvasObjectView.class.getResource( CanvasObjectView.class.getSimpleName() + ".fxml" ) );

		//		getChildren().addAll(
		//				VBoxBuilder
		//						.create()
		//						.children( inputTerminalHBox,
		//								RectangleBuilder.create().width( 200 ).height( 100 ).fill( Color.BLUEVIOLET ).build(),
		//								outputTerminalHBox ).build() );
	}

	@FXML
	protected void initialize()
	{
		canvasObjectLabel.textProperty().bind( Properties.forLabel( canvasObject ) );

		Bindings.bindContent( inputTerminalPane.getChildren(), inputTerminals );
		Bindings.bindContent( outputTerminalPane.getChildren(), outputTerminals );

		addEventHandler( MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.isPrimaryButtonDown() )
				{
					toFront();
				}
			}
		} );

		addEventHandler( MouseEvent.ANY, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				event.consume();
			}
		} );
	}

	@FXML
	public void delete()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_DELETE, canvasObject ) );
	}

	@FXML
	public void rename()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, canvasObject ) );
	}

	public CanvasObjectItem getCanvasObject()
	{
		return canvasObject;
	}
}