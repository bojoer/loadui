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