package com.eviware.loadui.api.ui;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;

public interface ApplicationState
{
	public WorkspaceItem getLoadedWorkspace();

	public void displayWorkspace();

	public CanvasItem getActiveCanvas();

	public void setActiveCanvas( CanvasItem canvas );
}
