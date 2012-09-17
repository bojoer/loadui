package com.eviware.loadui.ui.fx.views.canvas;

import com.eviware.loadui.api.model.SceneItem;

public class ScenarioView extends CanvasObjectView
{
	public ScenarioView( SceneItem component )
	{
		super( component );
	}

	public SceneItem getComponent()
	{
		return ( SceneItem )getCanvasObject();
	}
}
