package com.eviware.loadui.test.states;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.util.ReleasableUtils;

public class WorkspaceLoadedState extends TestState
{
	public static final WorkspaceLoadedState STATE = new WorkspaceLoadedState();

	private WorkspaceItem workspace;

	private WorkspaceLoadedState()
	{
		super( "Workspace Loaded", ControllerStartedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		WorkspaceProvider workspaceProvider = ControllerStartedState.STATE.getWorkspaceProviderByForce();
		if( !workspaceProvider.isWorkspaceLoaded() )
		{
			workspace = workspaceProvider.loadDefaultWorkspace();
		}
		else
		{
			workspace = workspaceProvider.getWorkspace();
		}
	}

	@Override
	protected void exitToParent() throws Exception
	{
		ReleasableUtils.release( workspace );
		workspace = null;
	}

	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}
}
