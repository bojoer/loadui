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
						mainWindow.fireEvent( IntentEvent.create( IntentEvent.INTENT_SAVE, project ) );
						//project.save();
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
