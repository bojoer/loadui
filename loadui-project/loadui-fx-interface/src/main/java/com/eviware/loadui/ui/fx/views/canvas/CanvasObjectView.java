package com.eviware.loadui.ui.fx.views.canvas;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.canvas.component.ComponentView;
import com.eviware.loadui.ui.fx.views.canvas.scenario.ScenarioView;
import com.eviware.loadui.ui.fx.views.canvas.terminal.InputTerminalView;
import com.eviware.loadui.ui.fx.views.canvas.terminal.OutputTerminalView;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class CanvasObjectView extends StackPane implements Deletable
{
	protected static final Logger log = LoggerFactory.getLogger( CanvasObjectView.class );

	private static final Function<InputTerminal, InputTerminalView> INPUT_TERMINAL_TO_VIEW = new Function<InputTerminal, InputTerminalView>()
	{
		@Override
		public InputTerminalView apply( final InputTerminal terminal )
		{
			InputTerminalView terminalView = new InputTerminalView( terminal );
			HBox.setHgrow( terminalView, Priority.ALWAYS );
			return terminalView;
		}
	};

	private static final Function<OutputTerminal, OutputTerminalView> OUTPUT_TERMINAL_TO_VIEW = new Function<OutputTerminal, OutputTerminalView>()
	{
		@Override
		public OutputTerminalView apply( final OutputTerminal terminal )
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
		{
			return ( T )ComponentView.newInstance( ( ComponentItem )item );
		}
		if( item instanceof SceneItem )
		{
			return ( T )new ScenarioView( ( SceneItem )item );
		}

		throw new IllegalArgumentException();
	}

	private final CanvasObjectItem canvasObject;
	private final ObservableList<InputTerminalView> inputTerminals;
	private final ObservableList<OutputTerminalView> outputTerminals;

	@FXML
	protected Label canvasObjectLabel;
	@FXML
	protected Pane inputTerminalPane;
	@FXML
	protected Pane outputTerminalPane;
	@FXML
	protected MenuButton menuButton;
	@FXML
	protected BorderPane topBar;
	@FXML
	protected HBox buttonBar;
	@FXML
	protected StackPane content;

	public CanvasObjectView( CanvasObjectItem canvasObject )
	{
		this.canvasObject = canvasObject;
		inputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, InputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), InputTerminal.class ) ) ), INPUT_TERMINAL_TO_VIEW );

		outputTerminals = transform(
				fx( ofCollection( canvasObject, TerminalHolder.TERMINALS, OutputTerminal.class,
						Iterables.filter( canvasObject.getTerminals(), OutputTerminal.class ) ) ), OUTPUT_TERMINAL_TO_VIEW );

		FXMLUtils
				.load( this, this, CanvasObjectView.class.getResource( CanvasObjectView.class.getSimpleName() + ".fxml" ) );
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
	@Override
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

	public ObservableList<OutputTerminalView> getOutputTerminalViews()
	{
		return outputTerminals;
	}

	public ObservableList<InputTerminalView> getInputTerminalViews()
	{
		return inputTerminals;
	}
}