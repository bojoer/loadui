package com.eviware.loadui.ui.fx.views.project;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooserBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class ProjectSettingsDialog extends ConfirmationDialog
{
	private static final Logger log = LoggerFactory.getLogger( ProjectSettingsDialog.class );

	@FXML
	private DetachableTab executionTab;

	@FXML
	private DetachableTab reportsTab;

	@FXML
	private DetachableTab miscTab;

	@FXML
	private CheckBox enableBrowseForReportFile;

	@FXML
	private Button browseForReportFileButton;

	@FXML
	private TextField browseForReportFileField;

	private final ProjectItem project;

	public ProjectSettingsDialog( Node owner, ProjectItem project )
	{
		super( owner, "Settings", "Ok" );
		this.project = project;

		Pane pane = new Pane();
		pane.setMinHeight( 320 );
		pane.setMinWidth( 500 );

		FXMLLoader loader = new FXMLLoader( getClass().getResource( getClass().getSimpleName() + ".fxml" ) );
		loader.setClassLoader( FXMLUtils.classLoader );
		loader.setRoot( pane );
		loader.setController( this );

		try
		{
			loader.load();
		}
		catch( IOException exception )
		{
			throw new RuntimeException( "Unable to load fxml view: " + getClass().getSimpleName() + ".fxml", exception );
		}

		getItems().add( pane );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				log.info( "CONFIRM!" );
				close();
			}
		} );
	}

	@FXML
	public void enableBrowseForReportFile()
	{
		browseForReportFileButton.setDisable( !enableBrowseForReportFile.isSelected() );
		browseForReportFileField.setDisable( !enableBrowseForReportFile.isSelected() );
	}

	@FXML
	public void browseForReportFile()
	{
		final Property<?> settingsFileProperty = project.getProperty( ProjectItem.REPORT_FOLDER_PROPERTY );

		File initialDirectory = new File( Objects.firstNonNull(
				Strings.emptyToNull( ( String )settingsFileProperty.getValue() ),
				project.getWorkspace().getAttribute( UIUtils.LATEST_DIRECTORY, System.getProperty( LoadUI.LOADUI_HOME ) ) ) );

		initialDirectory = initialDirectory.isDirectory() ? initialDirectory : initialDirectory.getParentFile();

		log.info( "Browsing for report file in initial directory: '" + initialDirectory + "'" );

		FileChooser fileChooser = FileChooserBuilder.create().initialDirectory( initialDirectory )
				.extensionFilters( UIUtils.XML_EXTENSION_FILTER ).build();
		String path = fileChooser.showSaveDialog( getScene().getWindow() ).getPath();
		browseForReportFileField.setText( path );
		settingsFileProperty.setValue( path );
	}
}
