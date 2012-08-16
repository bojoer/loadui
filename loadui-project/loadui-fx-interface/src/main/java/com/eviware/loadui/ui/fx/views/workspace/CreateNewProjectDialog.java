package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateNewProjectDialog extends ConfirmationDialog
{
	public CreateNewProjectDialog( final WorkspaceItem workspace, final Node owner )
	{
		super( owner, "Create new project", "Create" );

		int projectNumber = getNextProjectNumber();

		Label projectName = new Label( "Project name" );
		final TextField projectNameField = new TextField( "Project " + projectNumber );
		Label fileName = new Label( "File name" );
		final TextField fileNameField = new TextField( "project-" + projectNumber + ".xml" );
		final CheckBox openNewProject = new CheckBox( "Open project after creation" );
		openNewProject.setSelected( true );

		getItems().setAll( projectName, projectNameField, fileName, fileNameField, openNewProject );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();

				ProjectRef projectRef = workspace.createProject( new File( System.getProperty( LoadUI.LOADUI_HOME ),
						fileNameField.getText() ), projectNameField.getText(), false );

				if( openNewProject.isSelected() )
				{
					owner.fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
				}
			}
		} );
	}

	private static int getNextProjectNumber()
	{
		int projectNumber = 1;
		while( !isValidFileName( "project-" + projectNumber + ".xml" ) )
		{
			projectNumber++ ;
		}
		return projectNumber;
	}

	private static boolean isValidFileName( String fileName )
	{
		return !new File( System.getProperty( "user.home" ) + "/.loadui", fileName ).exists();
	}
}
