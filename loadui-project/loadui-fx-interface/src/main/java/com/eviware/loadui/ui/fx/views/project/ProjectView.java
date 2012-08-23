package com.eviware.loadui.ui.fx.views.project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.eviware.loadui.ui.fx.views.statistics.StatisticsView;
import com.google.common.base.Preconditions;

public class ProjectView extends StackPane
{
	@FXML
	private Label workspaceLabel;

	@FXML
	private Label projectLabel;

	@FXML
	private DetachableTab designTab;

	@FXML
	private DetachableTab resultTab;

	private final ProjectItem project;

	public ProjectView( ProjectItem project )
	{
		this.project = Preconditions.checkNotNull( project );

		FXMLUtils.loadNew( this, this );

		workspaceLabel.textProperty().bind( Properties.forLabel( project.getWorkspace() ) );
		projectLabel.textProperty().bind( Properties.forLabel( project ) );

		designTab.setDetachableContent( new CanvasView( project ) );
		resultTab.setDetachableContent( new StatisticsView( project ) );
	}

	public void close()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, project ) );
	}
}
