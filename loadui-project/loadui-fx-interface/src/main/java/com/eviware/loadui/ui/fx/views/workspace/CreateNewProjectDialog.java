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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;
import com.eviware.loadui.ui.fx.util.UIUtils;

public class CreateNewProjectDialog extends ConfirmationDialog
{
	public CreateNewProjectDialog( final WorkspaceItem workspace, final Node owner )
	{
		super( owner, "Create new project", "Create" );

		int projectNumber = getNextProjectNumber();

		Label projectName = new Label( "Project name" );
		final ValidatableStringField projectNameField = ValidatableStringField.Builder.create()
				.stringConstraint( ValidatableStringField.NOT_EMPTY ).text( "Project " + projectNumber ).build();
		Label fileName = new Label( "File name" );
		final ValidatableStringField fileNameField = ValidatableStringField.Builder.create()
				.stringConstraint( ValidatableStringField.NOT_EMPTY ).text( "project-" + projectNumber + ".xml" ).build();
		HBox.setHgrow( fileNameField, Priority.ALWAYS );
		Button browseButton = ButtonBuilder.create().text( "Browse..." ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				FileChooser fileChooser = FileChooserBuilder
						.create()
						.initialDirectory(
								new File( workspace.getAttribute( UIUtils.LATEST_DIRECTORY,
										System.getProperty( LoadUI.LOADUI_HOME ) ) ) )
						.extensionFilters( UIUtils.XML_EXTENSION_FILTER ).build();
				fileNameField.setText( fileChooser.showSaveDialog( getScene().getWindow() ).getPath() );
			}
		} ).build();
		final CheckBox openNewProject = new CheckBox( "Open project after creation" );
		openNewProject.setSelected( true );

		getItems().setAll( projectName, projectNameField, fileName,
				HBoxBuilder.create().spacing( 4 ).children( fileNameField, browseButton ).build(), openNewProject );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();

				String path = fileNameField.getText();
				File projectFile = path.contains( File.separator ) ? new File( path ) : new File( workspace.getAttribute(
						UIUtils.LATEST_DIRECTORY, System.getProperty( LoadUI.LOADUI_HOME ) ), path );
				ProjectRef projectRef = workspace.createProject( projectFile, projectNameField.getText(), false );

				if( openNewProject.isSelected() )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
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
