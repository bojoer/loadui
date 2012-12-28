package com.eviware.loadui.test.ui.fx.states;


public class ProjectCreatedWithoutAgentsState extends ProjectCreatedState
{
	
	public static final ProjectCreatedWithoutAgentsState STATE = new ProjectCreatedWithoutAgentsState();

	private ProjectCreatedWithoutAgentsState()
	{
		super( "Project without agents created", FXAppLoadedState.STATE );
	}

}
