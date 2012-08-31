package com.eviware.loadui.ui.fx.views.canvas;

import com.eviware.loadui.api.model.ComponentItem;

public class ComponentView extends CanvasObjectView
{
	public ComponentView( ComponentItem component )
	{
		super( component );
	}

	public ComponentItem getComponent()
	{
		return ( ComponentItem )getCanvasObject();
	}
}
