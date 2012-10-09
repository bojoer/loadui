package com.eviware.loadui.ui.fx.views.scenario;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;

public class NewScenarioIcon extends Label
{
	public NewScenarioIcon()
	{
		getStyleClass().add( "icon" );

		setGraphic( createIcon() );
		setText( "VU Scenario" );

		addEventFilter( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
				}
			}
		} );

		DragNode.install( this, createIcon() ).setData( this );
	}

	private static Node createIcon()
	{
		return RectangleBuilder.create().width( 65 ).height( 40 ).fill( Color.DEEPPINK ).build();
	}

	@Override
	public String toString()
	{
		return "VU Scenario";
	}
}
