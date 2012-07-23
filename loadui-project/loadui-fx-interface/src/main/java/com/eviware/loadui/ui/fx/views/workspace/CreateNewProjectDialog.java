package com.eviware.loadui.ui.fx.views.workspace;

import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateNewProjectDialog extends ConfirmationDialog
{

	public CreateNewProjectDialog( Scene parentScene )
	{
		super( parentScene, "Create new project", "Create" );

		Label projectName = new Label( "Project name" );
		TextField projectNameField = new TextField();
		Label fileName = new Label( "File name" );
		TextField fileNameField = new TextField();
		CheckBox openNewProject = new CheckBox( "Open project after creation" );
		openNewProject.setSelected( true );

		getItems().setAll( projectName, projectNameField, fileName, fileNameField, openNewProject );
	}
}
