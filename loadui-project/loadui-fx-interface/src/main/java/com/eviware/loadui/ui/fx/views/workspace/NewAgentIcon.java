package com.eviware.loadui.ui.fx.views.workspace;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.ui.fx.control.DragNode;

public class NewAgentIcon extends Label
{
	public NewAgentIcon()
	{
		getStyleClass().add( "icon" );

		setGraphic( createIcon() );
		setText( "Create Agent" );

		addEventFilter( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					//TODO: Open NewAgentDialog.
					System.out.println( "Create Agent" );
				}
			}
		} );

		DragNode.install( this, createIcon() ).setData( this );
	}

	private static Node createIcon()
	{
		return RectangleBuilder.create().width( 75 ).height( 50 ).fill( Color.BLUE ).build();
	}
}
