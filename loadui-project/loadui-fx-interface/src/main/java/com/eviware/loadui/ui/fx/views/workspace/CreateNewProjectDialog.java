package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;

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
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateNewProjectDialog extends ConfirmationDialog
{
	private static final String LATEST_DIRECTORY = "gui.latestDirectory";
	private static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );

	public CreateNewProjectDialog( final WorkspaceItem workspace, final Node owner )
	{
		super( owner, "Create new project", "Create" );

		Label projectName = new Label( "Project name" );
		final TextField projectNameField = new TextField();
		Label fileName = new Label( "File name" );
		final TextField fileNameField = new TextField();
		HBox.setHgrow( fileNameField, Priority.ALWAYS );
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
						LATEST_DIRECTORY, System.getProperty( LoadUI.LOADUI_HOME ) ), path );
				ProjectRef projectRef = workspace.createProject( projectFile, projectNameField.getText(), false );

				if( openNewProject.isSelected() )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
				}
			}
		} );
	}
}
