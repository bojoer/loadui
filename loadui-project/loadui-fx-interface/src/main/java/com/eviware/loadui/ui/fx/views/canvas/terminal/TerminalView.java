package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CircleBuilder;

import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class TerminalView extends StackPane
{
	private final Terminal terminal;
	private Runnable onLayout;

	public TerminalView( Terminal terminal )
	{
		this.terminal = terminal;
		FXMLUtils.load( this, this, TerminalView.class.getResource( TerminalView.class.getSimpleName() + ".fxml" ) );
	}

	@FXML
	Node terminalNode;

	@FXML
	protected void initialize()
	{
		final DragNode dragNode = DragNode.install( terminalNode, CircleBuilder.create().radius( 10 ).fill( Color.GREEN )
				.build() );
		dragNode.setRevert( false );
		dragNode.setData( terminal );

		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind( Properties.forDescription( terminal ) );
		Tooltip.install( terminalNode, tooltip );

		terminalNode.addEventHandler( MouseEvent.ANY, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				event.consume();
			}
		} );
	}

	public Terminal getTerminal()
	{
		return terminal;
	}

	public Runnable getOnLayout()
	{
		return onLayout;
	}

	public void setOnLayout( Runnable onLayout )
	{
		this.onLayout = onLayout;
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		if( onLayout != null )
		{
			onLayout.run();
		}
	}
}