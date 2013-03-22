package com.eviware.loadui.ui.fx.api.control;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBoxBuilder;

public class TaskProgressIndicator extends StackPane
{
	private final Label label;
	private final ProgressIndicator progress;

	public TaskProgressIndicator()
	{
		getChildren().setAll(
				VBoxBuilder
						.create()
						.children( label = LabelBuilder.create().text( "Please wait..." ).build(),
								progress = ProgressBarBuilder.create().build() ).build() );
	}
	
	public TaskProgressIndicator( Task<?> task ) {
		this();
		label.textProperty().bind( task.messageProperty() );
		progress.progressProperty().bind( task.progressProperty() );
	}
	
	public ProgressIndicator getProgressIndicator() {
		return progress;
	}

	public void dispose()
	{
		label.textProperty().unbind();
		progress.progressProperty().unbind();
	}
}
