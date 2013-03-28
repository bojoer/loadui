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
package com.eviware.loadui.ui.fx.views.window;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.testevents.TestEventManager;
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
	private final FxExecutionsInfo executionsInfo;
	private final TestEventManager tem;

	private static final Logger log = LoggerFactory.getLogger( MainWindowView.class );

	public MainWindowView( WorkspaceProvider workspaceProvider, FxExecutionsInfo executionsInfo, TestEventManager tem )
	{
		this.workspaceProvider = Preconditions.checkNotNull( workspaceProvider );
		this.executionsInfo = Preconditions.checkNotNull( executionsInfo );
		this.tem = tem;
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		notificationPanel.setVisible( false );
		notificationPanel.setMainWindowView( this );
		notificationPanel.listenOnDetachedTabs();
		tem.registerObserver( notificationPanel );
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
			mainButton.effectProperty().bind(Bindings.when( Bindings.or( mainButton.hoverProperty(), mainButton.showingProperty() ) ).then( new Glow(0.4d) ).otherwise( new Glow( 0d ) ) );
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
				else if( event.getEventType() == IntentEvent.INTENT_RUN_BLOCKING_ABORTABLE )
				{
					//Handled by AbortableBlockingTask.
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

	@SuppressWarnings( "unchecked" )
	public <T extends Parent> T getChildView( Class<T> expectedClass )
	{
		if( container != null && container.getChildren().isEmpty() == false )
		{
			log.debug( "contains: " + container.getChildren().size() + ": " + container.getChildren() );
			for( Node childView : container.getChildren() )
			{
				if( expectedClass.isInstance( childView ) )
					return ( T )childView;
			}
		}
		throw new IllegalStateException( MainWindowView.class.getName() + " does not hold a view of class "
				+ expectedClass );
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

}
