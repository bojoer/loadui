package com.eviware.loadui.ui.fx.views.workspace;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.UIUtils;

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
					fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, AgentItem.class ) );
				}
			}
		} );

		DragNode.install( this, createIcon() ).setData( this );
	}

	private static Node createIcon()
	{
		return new ImageView( UIUtils.getImageFor( AgentItem.class ) );
	}
}
