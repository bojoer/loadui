package com.eviware.loadui.ui.fx;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.api.intent.BlockingTask;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;

public class MainWindow
{
	//Fullscreen doesn't seem to work, the property never changes.
	private static final String FULLSCREEN = MainWindow.class.getName() + "@fullscreen";
	private static final String WINDOW_WIDTH = MainWindow.class.getName() + "@width";
	private static final String WINDOW_HEIGHT = MainWindow.class.getName() + "@height";

	private final Stage stage;
	private final WorkspaceProvider workspaceProvider;

	public MainWindow( final Stage stage, final WorkspaceProvider workspaceProvider )
	{
		this.stage = stage;
		this.workspaceProvider = workspaceProvider;

		loadDimensions();
		stage.addEventHandler( WindowEvent.WINDOW_HIDING, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent event )
			{
				saveDimensions();
			}
		} );
	}

	public void show()
	{
		stage.setTitle( System.getProperty( LoadUI.NAME, "loadUI" ) + " " + LoadUI.VERSION );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				stage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
						.root( new MainWindowView( workspaceProvider ) ).build() );
				BlockingTask.install( stage.getScene() );

				stage.show();
			}
		} );
	}

	private void loadDimensions()
	{
		if( !workspaceProvider.isWorkspaceLoaded() )
		{
			workspaceProvider.loadDefaultWorkspace();
		}

		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		if( Boolean.valueOf( workspace.getAttribute( FULLSCREEN, "false" ) ) )
		{
			stage.setFullScreen( true );
		}
		else
		{
			stage.setWidth( Double.parseDouble( workspace.getAttribute( WINDOW_WIDTH, "1200" ) ) );
			stage.setHeight( Double.parseDouble( workspace.getAttribute( WINDOW_HEIGHT, "700" ) ) );
		}
	}

	private void saveDimensions()
	{
		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		workspace.setAttribute( FULLSCREEN, String.valueOf( stage.isFullScreen() ) );
		workspace.setAttribute( WINDOW_WIDTH, String.valueOf( stage.getWidth() ) );
		workspace.setAttribute( WINDOW_HEIGHT, String.valueOf( stage.getHeight() ) );
		workspace.save();
	}
}
