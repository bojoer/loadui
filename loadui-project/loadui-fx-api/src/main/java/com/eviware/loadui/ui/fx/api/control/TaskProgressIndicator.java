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
