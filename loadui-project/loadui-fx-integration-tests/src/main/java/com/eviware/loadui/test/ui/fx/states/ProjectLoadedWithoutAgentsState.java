package com.eviware.loadui.test.ui.fx.states;

public class ProjectLoadedWithoutAgentsState extends ProjectLoadedState
{
	
	public static final ProjectLoadedWithoutAgentsState STATE = new ProjectLoadedWithoutAgentsState();
	
	public ProjectLoadedWithoutAgentsState()
	{
		super( "Project loaded without agents", ProjectCreatedWithoutAgentsState.STATE );
	}

}
