package com.eviware.loadui.ui.fx.control;

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

	public TaskProgressIndicator( Runnable runnable )
	{
		getChildren().setAll(
				VBoxBuilder
						.create()
						.children( label = LabelBuilder.create().text( "Please wait..." ).build(),
								progress = ProgressBarBuilder.create().build() ).build() );

		if( runnable instanceof Task )
		{
			Task<?> task = ( Task<?> )runnable;
			label.textProperty().bind( task.messageProperty() );
			progress.progressProperty().bind( task.progressProperty() );
		}
	}
}
