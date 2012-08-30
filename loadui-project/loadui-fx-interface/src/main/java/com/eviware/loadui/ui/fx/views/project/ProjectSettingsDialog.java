package com.eviware.loadui.ui.fx.views.project;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.DirectoryChooserBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class ProjectSettingsDialog extends ConfirmationDialog
{
	private static final Logger log = LoggerFactory.getLogger( ProjectSettingsDialog.class );

	public static String IGNORE_INVALID_CANVAS = "gui.ignore_invalid_canvas";

	@FXML
	private DetachableTab executionTab;

	@FXML
	private CheckBox abortOngoingOnFinish;

	@FXML
	private DetachableTab miscTab;

	@FXML
	private CheckBox ignoreInvalidCanvas;

	@FXML
	private DetachableTab reportsTab;

	@FXML
	private CheckBox enableBrowseForReportFile;

	@FXML
	private Button browseForReportFileButton;

	@FXML
	private TextField browseForReportFileField;

	private final ProjectItem project;

	public ProjectSettingsDialog( Node owner, ProjectItem projectIn )
	{
		super( owner, "Settings", "Ok" );
		this.project = projectIn;

		Pane pane = new Pane();
		pane.setMinHeight( 320 );
		pane.setMinWidth( 500 );

		FXMLUtils.load( pane, this, getClass().getResource( getClass().getSimpleName() + ".fxml" ) );

		getItems().add( pane );

		browseForReportFileField.setText( project.getReportFolder() );
		enableBrowseForReportFile.setSelected( project.isSaveReport() );
		enableBrowseForReportFile();
		ignoreInvalidCanvas.setSelected( Boolean.valueOf( project.getAttribute( IGNORE_INVALID_CANVAS, "false" ) ) );
		abortOngoingOnFinish.setSelected( project.isAbortOnFinish() );

		log.debug( "ignore: " + ignoreInvalidCanvas.isSelected() );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				project.setSaveReport( enableBrowseForReportFile.isSelected() );
				project.setReportFolder( browseForReportFileField.getText() );
				project.setAbortOnFinish( abortOngoingOnFinish.isSelected() );
				project.setAttribute( IGNORE_INVALID_CANVAS, String.valueOf( ignoreInvalidCanvas.isSelected() ) );
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
		String settingsFile = project.getReportFolder();

		File initialDirectory = new File( Objects.firstNonNull( Strings.emptyToNull( settingsFile ), project
				.getWorkspace().getAttribute( UIUtils.LATEST_DIRECTORY, System.getProperty( "user.home" ) ) ) );

		initialDirectory = initialDirectory.isDirectory() ? initialDirectory : initialDirectory.getParentFile();

		log.debug( "Browsing for report file in initial directory: '%s'", initialDirectory );

		DirectoryChooser directoryChooser = DirectoryChooserBuilder.create().initialDirectory( initialDirectory ).build();
		String path = directoryChooser.showDialog( getScene().getWindow() ).getPath();

		browseForReportFileField.setText( path );
	}
}
