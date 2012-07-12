package com.eviware.loadui.fx;

import java.net.URL;

import javafx.application.Platform;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;

public class TestWindow
{
	private final Stage stage;
	private final WorkspaceProvider workspaceProvider;

	public TestWindow( final Stage stage, final WorkspaceProvider workspaceProvider )
	{
		this.stage = stage;
		this.workspaceProvider = workspaceProvider;
	}

	public void show()
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				if( !workspaceProvider.isWorkspaceLoaded() )
				{
					workspaceProvider.loadDefaultWorkspace();
				}

				WorkspaceItem workspace = workspaceProvider.getWorkspace();

				stage.setWidth( 1280 );
				stage.setHeight( 700 );
				//stage.setScene( SceneBuilder.create().root( new WorkspaceView( workspace ) ).build() );
				final URL resource = TestWindow.class.getResource( "/com/eviware/loadui/ui/fx/loadui-style.bss" );
				stage.setScene( SceneBuilder.create().stylesheets( resource.toString() )
						.root( new MainWindowView( workspaceProvider ) ).build() );
				stage.setTitle( System.getProperty( LoadUI.NAME, "loadUI" ) + " " + LoadUI.VERSION );
				stage.show();

				//				if( System.getProperty( "scenicView" ) != null )
				//				{
				//					ScenicView.show( stage.getScene() );
				//				}
			}
		} );
	}
}
