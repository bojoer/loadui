package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.google.common.io.Files;

public class CloneProjectDialog extends ConfirmationDialog
{
	private static final String LATEST_DIRECTORY = "gui.latestDirectory";
	private static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );

	private final WorkspaceItem workspace;
	private final ProjectRef projectRef;
	private final TextField fileNameField;
	private final TextField projectNameField;
	private final CheckBox openNewProject;

	public CloneProjectDialog( final WorkspaceItem workspace, final ProjectRef projectRef, final Node owner )
	{
		super( owner, "Clone Project: " + projectRef.getLabel(), "Clone" );

		this.workspace = workspace;
		this.projectRef = projectRef;

		Label projectName = new Label( "Cloned project name" );
		projectNameField = new TextField();
		Label fileName = new Label( "File name" );
		fileNameField = new TextField();
		HBox.setHgrow( fileNameField, Priority.ALWAYS );
		openNewProject = new CheckBox( "Open cloned project after creation" );
		openNewProject.setSelected( true );

		int count = 1;
		File availableFile;
		while( ( availableFile = new File( projectRef.getProjectFile().getParentFile(), String.format( "copy-%d-of-%s",
				count, projectRef.getProjectFile().getName() ) ) ).exists() )
		{
			count++ ;
		}
		projectNameField.setText( String.format( "Copy %d of %s", count, projectRef.getLabel() ) );
		fileNameField.setText( availableFile.getName() );
		Button browseButton = ButtonBuilder.create().text( "Browse..." ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				FileChooser fileChooser = FileChooserBuilder
						.create()
						.initialDirectory(
								new File( workspace.getAttribute( LATEST_DIRECTORY, System.getProperty( LoadUI.LOADUI_HOME ) ) ) )
						.extensionFilters( XML_EXTENSION_FILTER ).build();
				fileNameField.setText( fileChooser.showSaveDialog( getScene().getWindow() ).getPath() );
			}
		} ).build();

		getItems().setAll( projectName, projectNameField, fileName,
				HBoxBuilder.create().spacing( 4 ).children( fileNameField, browseButton ).build(), openNewProject );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
				fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new CloneProjectTask() ) );
			}
		} );
	}

	private class CloneProjectTask extends Task<ProjectRef>
	{
		{
			updateMessage( "Cloning project: " + projectRef.getLabel() );
		}

		@Override
		protected ProjectRef call() throws Exception
		{
			String path = fileNameField.getText();
			File cloneFile = path.contains( File.separator ) ? new File( path ) : new File( projectRef.getProjectFile()
					.getParentFile(), path );

			Files.copy( projectRef.getProjectFile(), cloneFile );
			ProjectRef cloneRef = workspace.importProject( cloneFile, true );

			ProjectItem cloneProject = cloneRef.getProject();
			cloneProject.setLabel( projectNameField.getText() );
			cloneProject.save();

			//TODO: Remote if miniatures aren't generated in the same way.
			cloneRef.setAttribute( "miniature", projectRef.getAttribute( "miniature", "" ) );

			workspace.save();

			if( openNewProject.isSelected() )
			{
				fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, cloneRef ) );
			}
			else
			{
				cloneRef.setEnabled( false );
			}

			return cloneRef;
		}
	}
}
