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
package com.eviware.loadui.ui.fx.views.project;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ButtonDialog;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;

public class SaveProjectDialog extends ButtonDialog
{

	public SaveProjectDialog( final MainWindowView mainWindow, final ProjectItem project )
	{
		super( mainWindow, "Save changes?" );

		Button yesButton = ButtonBuilder.create().text( "Save" ).id( "yes" ).alignment( Pos.BOTTOM_RIGHT )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						mainWindow.getChildView( ProjectView.class ).fireEvent(
								IntentEvent.create( IntentEvent.INTENT_SAVE, project ) );
						close();
						mainWindow.showWorkspace();
						project.release();

					}
				} ).build();

		Button noButton = ButtonBuilder.create().text( "Don't Save" ).id( "no" ).alignment( Pos.BOTTOM_RIGHT )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						close();
						mainWindow.showWorkspace();
						project.release();
					}
				} ).build();

		Button cancelButton = ButtonBuilder.create().text( "Cancel" ).cancelButton( true ).alignment( Pos.BOTTOM_RIGHT )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						close();
					}
				} ).build();

		getButtons().setAll( yesButton, noButton, cancelButton );
	}

}
