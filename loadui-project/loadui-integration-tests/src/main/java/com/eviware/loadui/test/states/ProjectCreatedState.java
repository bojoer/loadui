package com.eviware.loadui.test.states;

import java.io.File;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.TestState;

public class ProjectCreatedState extends TestState
{
	public static final ProjectCreatedState STATE = new ProjectCreatedState();

	private ProjectItem project;

	private ProjectCreatedState()
	{
		super( "Project Created", WorkspaceLoadedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		File projectFile = File.createTempFile( "project", ".xml", new File( System.getProperty( LoadUI.LOADUI_HOME ) ) );
		project = WorkspaceLoadedState.STATE.getWorkspace().createProject( projectFile, "Project", true ).getProject();
	}

	@Override
	protected void exitToParent() throws Exception
	{
		project.delete();
		project = null;
	}

	public ProjectItem getProject()
	{
		return project;
	}
}
