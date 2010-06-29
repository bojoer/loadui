/*
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.cmd;

import groovy.ui.Console;

import java.io.File;
import java.util.Map;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;

public class CommandRunner
{
	private final WorkspaceProvider workspaceProvider;
	private final Console console;

	public CommandRunner( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;

		console = new Console();
	}

	public void execute( Object command, Map<String, String> properties )
	{
		WorkspaceItem workspace = workspaceProvider.isWorkspaceLoaded() ? workspaceProvider.getWorkspace()
				: workspaceProvider.loadWorkspace( new File( System.getProperty( "loadui.home" ) + File.separator
						+ "workspace.xml" ) );
		console.setVariable( "workspace", workspace );

		console.getShell().evaluate( command.toString() );
	}
}
