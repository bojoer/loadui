package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
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

	public TerminalView( Terminal terminal )
	{
		this.terminal = terminal;
		FXMLUtils.load( this, this, TerminalView.class.getResource( TerminalView.class.getSimpleName() + ".fxml" ) );
	}

	@FXML
	protected void initialize()
	{
		DragNode dragNode = DragNode.install( this, CircleBuilder.create().radius( 10 ).fill( Color.GREEN ).build() );
		dragNode.setData( terminal );

		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind( Properties.forDescription( terminal ) );
		Tooltip.install( this, tooltip );
	}

	public Terminal getTerminal()
	{
		return terminal;
	}
}