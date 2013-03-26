/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
