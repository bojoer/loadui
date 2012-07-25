package com.eviware.loadui.ui.fx.views.window;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.views.project.ProjectView;
import com.eviware.loadui.ui.fx.views.workspace.WorkspaceView;

public class MainWindowView extends StackPane
{
	private final WorkspaceProvider workspaceProvider;
	private final Property<WorkspaceItem> workspaceProperty = new SimpleObjectProperty<>();
	private final WorkspaceListener workspaceListener = new WorkspaceListener();

	public MainWindowView( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
		workspaceProvider.addEventListener( BaseEvent.class, workspaceListener );

		if( workspaceProvider.isWorkspaceLoaded() )
		{
			workspaceProperty.setValue( workspaceProvider.getWorkspace() );
		}
		else
		{
			workspaceProperty.setValue( workspaceProvider.loadDefaultWorkspace() );
		}

		getChildren().add( FXMLUtils.load( MainWindowView.class, new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				return new Controller();
			}
		} ) );
	}

	public class Controller implements Initializable
	{
		@FXML
		private StackPane container;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<? extends Object>>()
			{
				@Override
				public void handle( IntentEvent<? extends Object> event )
				{
					if( event.isConsumed() )
					{
						return;
					}

					if( event.getEventType() == IntentEvent.INTENT_OPEN )
					{
						if( event.getArg() instanceof ProjectRef )
						{
							final ProjectRef projectRef = ( ProjectRef )event.getArg();
							Task<Void> openProject = new Task<Void>()
							{
								@Override
								protected Void call() throws Exception
								{
									updateMessage( "Loading project: " + projectRef.getLabel() );
									try
									{
										projectRef.setEnabled( true );
										final ProjectItem project = projectRef.getProject();
										Platform.runLater( new Runnable()
										{
											@Override
											public void run()
											{
												container.getChildren().setAll( new ProjectView( project ) );
											}
										} );
									}
									catch( Exception e )
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									return null;
								}
							};

							fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, openProject ) );

							//new Thread( openProject ).start();
						}
						else
						{
							System.out.println( "Unhandled intent: " + event );
							return;
						}
					}
					else if( event.getEventType() == IntentEvent.INTENT_CLOSE )
					{
						if( event.getArg() instanceof ProjectItem )
						{
							ProjectItem project = ( ProjectItem )event.getArg();
							project.release();
							//TODO: Need to have the ProjectRef close the project.
							container.getChildren().setAll( new WorkspaceView( workspaceProvider.getWorkspace() ) );
						}
						else
						{
							System.out.println( "Unhandled intent: " + event );
							return;
						}
					}
					else
					{
						System.out.println( "Unhandled intent: " + event );
						return;
					}
					event.consume();
				}
			} );
			container.getChildren().setAll( new WorkspaceView( workspaceProvider.getWorkspace() ) );
			//			designTab.detachableContentProperty().bind( Bindings.createObjectBinding( new Callable<Node>()
			//			{
			//				@Override
			//				public Node call() throws Exception
			//				{
			//					return new WorkspaceView( workspaceProperty.getValue() );
			//				}
			//			}, workspaceProperty ) );
		}
	}

	private class WorkspaceListener implements WeakEventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( WorkspaceProvider.WORKSPACE_LOADED.equals( event.getKey() ) )
			{
				workspaceProperty.setValue( workspaceProvider.getWorkspace() );
			}
		}
	}
}
