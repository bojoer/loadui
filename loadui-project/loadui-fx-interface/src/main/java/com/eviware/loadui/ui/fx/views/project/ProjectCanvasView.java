package com.eviware.loadui.ui.fx.views.project;

import javafx.geometry.Point2D;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.eviware.loadui.ui.fx.views.scenario.CreateScenarioDialog;
import com.eviware.loadui.ui.fx.views.scenario.NewScenarioIcon;

public class ProjectCanvasView extends CanvasView
{
	public ProjectCanvasView( CanvasItem canvas )
	{
		super( canvas );
	}

	@Override
	protected boolean shouldAccept( final Object data )
	{
		return super.shouldAccept( data ) || data instanceof NewScenarioIcon;
	}

	@Override
	protected void handleDrop( final DraggableEvent event )
	{
		if( event.getData() instanceof NewScenarioIcon )
		{
			Point2D position = canvasLayer.sceneToLocal( event.getSceneX(), event.getSceneY() );

			new CreateScenarioDialog( this, getCanvas(), position ).show();
			event.consume();
		}
		else
			super.handleDrop( event );
	}

	@Override
	public ProjectItem getCanvas()
	{
		return ( ProjectItem )super.getCanvas();
	}
}
