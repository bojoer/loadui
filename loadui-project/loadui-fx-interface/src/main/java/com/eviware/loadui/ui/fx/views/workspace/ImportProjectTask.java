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
package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;

import javafx.concurrent.Task;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;

final class ImportProjectTask extends Task<ProjectRef>
{
	private final WorkspaceItem workspace;
	private final File sampleFile;

	ImportProjectTask( WorkspaceItem workspace, File projectFile )
	{
		updateMessage( "Importing project: " + projectFile.getName() );
		this.workspace = workspace;
		this.sampleFile = projectFile;
	}

	@Override
	protected ProjectRef call() throws Exception
	{
		ProjectRef projectRef = workspace.importProject( sampleFile, true );
		projectRef.setEnabled( false );

		return projectRef;
	}
}
