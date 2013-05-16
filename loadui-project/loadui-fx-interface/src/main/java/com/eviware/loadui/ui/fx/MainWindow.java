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
package com.eviware.loadui.ui.fx;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.api.LoaduiFXConstants;
import com.eviware.loadui.ui.fx.api.intent.AbortableBlockingTask;
import com.eviware.loadui.ui.fx.api.intent.BlockingTask;
import com.eviware.loadui.ui.fx.api.intent.DeleteTask;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;

public class MainWindow
{

	// Fullscreen doesn't seem to work, the property never changes.
	private static final String FULLSCREEN = MainWindow.class.getName() + "@fullscreen";
	private static final String WINDOW_WIDTH = MainWindow.class.getName() + "@width";
	private static final String WINDOW_HEIGHT = MainWindow.class.getName() + "@height";

	private Stage stage;
	private TestEventManager tem;
	private final WorkspaceProvider workspaceProvider;
	private FxExecutionsInfo executionsInfo;

	public MainWindow( final WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}

	public MainWindow withStage( Stage stage )
	{
		if( this.stage != null )
			throw new IllegalStateException( "Stage has already been set" );
		this.stage = stage;
		stage.addEventHandler( WindowEvent.WINDOW_HIDING, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent event )
			{
				saveDimensions();
			}
		} );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				loadDimensions();
			}
		} );
		return this;
	}

	public MainWindow withTestEventManager( TestEventManager tem )
	{
		if( this.tem != null )
			throw new IllegalStateException( "TestEventManager has already been set" );
		this.tem = tem;
		return this;
	}

	public MainWindow provideInfoFor( FxExecutionsInfo executionsInfo )
	{
		this.executionsInfo = executionsInfo;
		return this;
	}

	public void show()
	{
		if( stage == null || tem == null )
			throw new IllegalStateException( "Stage or TestEventManager have not been set" );

		stage.setTitle( System.getProperty( LoadUI.NAME, "LoadUI" ) + " " + LoadUI.VERSION );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				final MainWindowView mainView = new MainWindowView( workspaceProvider, executionsInfo, tem );

				stage.setScene( SceneBuilder.create().stylesheets( LoaduiFXConstants.getLoaduiStylesheets() )
						.root( mainView ).build() );
				BlockingTask.install( stage.getScene() );
				AbortableBlockingTask.install( stage.getScene() );
				DeleteTask.install( stage.getScene() );

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
			stage.setHeight( Double.parseDouble( workspace.getAttribute( WINDOW_HEIGHT, "800" ) ) );
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
