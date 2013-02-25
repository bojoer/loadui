package com.eviware.loadui.ui.fx.views.window;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;
import com.eviware.loadui.ui.fx.control.NotificationPanel;
import com.eviware.loadui.ui.fx.input.SelectableImpl;
import com.eviware.loadui.ui.fx.util.ErrorHandler;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.about.AboutDialog;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.ui.fx.views.inspector.InspectorView;
import com.eviware.loadui.ui.fx.views.project.ProjectView;
import com.eviware.loadui.ui.fx.views.project.SaveProjectDialog;
import com.eviware.loadui.ui.fx.views.rename.RenameDialog;
import com.eviware.loadui.ui.fx.views.workspace.GlobalSettingsDialog;
import com.eviware.loadui.ui.fx.views.workspace.SystemPropertiesDialog;
import com.eviware.loadui.ui.fx.views.workspace.WorkspaceView;
import com.google.common.base.Preconditions;

public class MainWindowView extends StackPane
{
	@FXML
	private MenuButton mainButton;

	@FXML
	private StackPane container;

	@FXML
	private InspectorView inspectorView;

	@FXML
	private NotificationPanel notificationPanel;

	private final WorkspaceProvider workspaceProvider;
	private final Property<WorkspaceItem> workspaceProperty = new SimpleObjectProperty<>();
	private final WorkspaceListener workspaceListener = new WorkspaceListener();
	private final SimpleBooleanProperty isInitialized = new SimpleBooleanProperty( false );
	private final FxExecutionsInfo executionsInfo;

	public MainWindowView( WorkspaceProvider workspaceProvider, FxExecutionsInfo executionsInfo )
	{
		this.workspaceProvider = Preconditions.checkNotNull( workspaceProvider );
		this.executionsInfo = Preconditions.checkNotNull( executionsInfo );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		notificationPanel.setVisible( false );
		notificationPanel.setMainWindowView( this );
		notificationPanel.setOnShowLog( new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				inspectorView.ensureShowing( InspectorView.EVENT_LOG_TAB );
			}
		} );

		workspaceProvider.addEventListener( BaseEvent.class, workspaceListener );

		if( workspaceProvider.isWorkspaceLoaded() )
		{
			workspaceProperty.setValue( workspaceProvider.getWorkspace() );
		}
		else
		{
			workspaceProperty.setValue( workspaceProvider.loadDefaultWorkspace() );
		}

		try
		{
			mainButton.setGraphic( new ImageView( LoadUI.relativeFile( "res/logo-button.png" ).toURI().toURL()
					.toExternalForm() ) );
			SelectableImpl.installDeleteKeyHandler( this );

			initIntentEventHanding();
			initInspectorView();
			showWorkspace();
		}
		catch( Exception e1 )
		{
			e1.printStackTrace();
			ErrorHandler.promptRestart();
		}

		synchronized( isInitialized )
		{
			isInitialized.set( true );
		}

	}

	/**
	 * The given runnable will be run immediately if this has already been
	 * initialized, or at some time after this is initialized.
	 * 
	 * @param runnable
	 */
	public void runAfterInit( final Runnable runnable )
	{
		boolean runNow = false;
		synchronized( isInitialized )
		{
			if( isInitialized.get() )
			{
				runNow = true; // do not run the runnable inside the synchronized block, do it outside
			}
			else
			{
				isInitialized.addListener( new ChangeListener<Boolean>()
				{
					@Override
					public void changed( ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal )
					{
						if( newVal )
							runnable.run();
					}
				} );
			}
		}
		if( runNow )
		{
			runnable.run();
		}
	}

	public MenuButton getMainButton()
	{
		return mainButton;
	}

	private void initIntentEventHanding()
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
											ProjectView projectView = new ProjectView( project, executionsInfo );
											container.getChildren().setAll( projectView );
											PerspectiveEvent.fireEvent( PerspectiveEvent.PERSPECTIVE_PROJECT, projectView );
										}
									} );
								}
								catch( Exception e )
								{
									e.printStackTrace();
								}

								return null;
							}
						};

						fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, openProject ) );
					}
					else if( event.getArg() instanceof SceneItem )
					{
						if( container.getChildren().size() == 1 )
						{
							container.getChildren().get( 0 ).fireEvent( event );
						}
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
						final ProjectItem project = ( ProjectItem )event.getArg();
						if( project.isDirty() )
						{
							SaveProjectDialog saveDialog = new SaveProjectDialog( MainWindowView.this, project );
							saveDialog.show();
						}
						else
						{
							showWorkspace();
							project.release();
						}
						//TODO: Need to have the ProjectRef close the project.
					}
					else
					{
						System.out.println( "Unhandled intent: " + event );
						return;
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_RENAME )
				{
					final Object arg = event.getArg();
					Preconditions.checkArgument( arg instanceof Labeled.Mutable );
					new RenameDialog( ( Labeled.Mutable )arg, MainWindowView.this ).show();
				}
				else if( event.getEventType() == IntentEvent.INTENT_RUN_BLOCKING )
				{
					//Handled by BlockingTask.
					return;
				}
				else if( event.getEventType() == IntentEvent.INTENT_DELETE )
				{
					//Handled by DeleteTask.
					return;
				}
				else
				{
					System.out.println( "Unhandled intent: " + event );
					return;
				}
				event.consume();
			}
		} );
	}

	public void showWorkspace()
	{
		WorkspaceView workspaceView = new WorkspaceView( workspaceProvider.getWorkspace() );
		container.getChildren().setAll( workspaceView );
		PerspectiveEvent.fireEvent( PerspectiveEvent.PERSPECTIVE_WORKSPACE, workspaceView );
	}

	public WorkspaceView getWorkspaceView()
	{
		if( container != null && container.getChildren().isEmpty() == false )
			return ( WorkspaceView )container.getChildren().get( 0 );
		else
			throw new RuntimeException( "WorkspaceView has not been created yet" );
	}

	public void settings()
	{
		GlobalSettingsDialog.newInstance( mainButton, workspaceProperty.getValue() ).show();
	}

	public void systemProperties()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new Runnable()
		{
			@Override
			public void run()
			{
				SystemPropertiesDialog.initialize();

				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						new SystemPropertiesDialog( mainButton ).show();
					}
				} );
			}
		} ) );
	}

	public void feedback()
	{
		UIUtils.openInExternalBrowser( "http://www.soapui.org/forum/viewforum.php?f=9" );
	}

	public void about()
	{
		new AboutDialog( mainButton ).show( getScene().getWindow() );
	}

	private ObservableList<Inspector> inspectors;

	private void initInspectorView()
	{
		inspectorView.setPerspective( PerspectiveEvent.PERSPECTIVE_WORKSPACE );

		PerspectiveEvent.addEventHandler( PerspectiveEvent.ANY, new EventHandler<PerspectiveEvent>()
		{
			@Override
			public void handle( PerspectiveEvent event )
			{
				inspectorView.setPerspective( event.getEventType() );
			}
		} );

		inspectors = ObservableLists.fx( ObservableLists.ofServices( Inspector.class ) );
		Bindings.bindContent( inspectorView.getInspectors(), inspectors );
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

	/**
	 * @return notification panel. May return null if this is not initialized
	 *         yet.
	 * @see {@link MainWindowView#runAfterInit(Runnable)}
	 */
	public TestEventObserver getNotificationPanel()
	{
		return notificationPanel;
	}

}
