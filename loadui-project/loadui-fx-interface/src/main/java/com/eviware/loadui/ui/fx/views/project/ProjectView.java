package com.eviware.loadui.ui.fx.views.project;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.eviware.loadui.ui.fx.views.rename.RenameDialog;
import com.eviware.loadui.ui.fx.views.scenario.ScenarioToolbar;
import com.eviware.loadui.ui.fx.views.statistics.StatisticsView;
import com.eviware.loadui.ui.fx.views.workspace.CloneProjectDialog;
import com.eviware.loadui.ui.fx.views.workspace.CreateNewProjectDialog;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ProjectView extends AnchorPane
{
	private static final String HELP_PAGE = "http://www.loadui.org/interface/project-view.html";

	private static final Logger log = LoggerFactory.getLogger( ProjectView.class );

	@FXML
	private DetachableTab designTab;

	@FXML
	private DetachableTab resultTab;

	@FXML
	private MenuButton menuButton;

	@FXML
	private Button summaryButton;

	private CanvasItem lastRunCanvas;

	private final ProjectItem project;

	private final Observable projectReleased;

	public ProjectView( ProjectItem projectIn )
	{
		this.project = Preconditions.checkNotNull( projectIn );
		projectReleased = Properties.observeEvent( project, ProjectItem.RELEASED );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		ProjectPlaybackPanel playbackPanel = new ProjectPlaybackPanel( project );
		AnchorPane.setTopAnchor( playbackPanel, 14.0 );
		AnchorPane.setLeftAnchor( playbackPanel, 440.0 );
		getChildren().add( playbackPanel );

		menuButton.textProperty().bind( Properties.forLabel( project ) );
		designTab.setDetachableContent( new ProjectCanvasView( project ) );
		resultTab.setDetachableContent( new StatisticsView( project ) );
		summaryButton.setDisable( true );

		addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<? extends Object>>()
		{
			@Override
			public void handle( IntentEvent<? extends Object> event )
			{
				if( event.getEventType() == IntentEvent.INTENT_CREATE )
				{
					if( event.getArg() instanceof WorkspaceItem )
					{
						WorkspaceItem workspaceItem = ( WorkspaceItem )event.getArg();
						new CreateNewProjectDialog( workspaceItem, ProjectView.this ).show();
						event.consume();
					}
					else
					{
						log.debug( "Unhandled intent: %s", event );
						return;
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_SAVE )
				{
					if( event.getArg() instanceof ProjectItem )
					{
						project.save();
					}
					else
					{
						log.debug( "Unhandled intent: %s", event );
						return;
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_RENAME )
				{
					final Object arg = event.getArg();
					Preconditions.checkArgument( arg instanceof Labeled.Mutable );
					new RenameDialog( ( Labeled.Mutable )arg, ProjectView.this ).show();
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_OPEN )
				{
					final Object arg = event.getArg();
					Preconditions.checkArgument( arg instanceof SceneItem );
					SceneItem scenario = ( SceneItem )arg;
					ScenarioToolbar toolbar = new ScenarioToolbar( scenario );
					StackPane.setAlignment( toolbar, Pos.TOP_CENTER );
					CanvasView canvas = new CanvasView( scenario );
					StackPane.setMargin( canvas, new Insets( 60, 0, 0, 0 ) );
					StackPane pane = StackPaneBuilder.create().children( canvas, toolbar ).build();
					designTab.setDetachableContent( pane );
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CLOSE && event.getArg() instanceof SceneItem )
				{
					designTab.setDetachableContent( new ProjectCanvasView( project ) );
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CLONE )
				{
					ProjectRef projectRef = getProjectRef( project.getId() );
					new CloneProjectDialog( project.getWorkspace(), projectRef, ProjectView.this ).show();
					event.consume();
					return;
				}
			}
		} );

		final TestExecutionTask testExecutionTask = new TestExecutionTask()
		{
			@Override
			public void invoke( final TestExecution execution, Phase phase )
			{
				if( phase == Phase.PRE_START )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							summaryButton.setDisable( true );
						}
					} );
				}
				else
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							lastRunCanvas = execution.getCanvas();
							summaryButton.setDisable( false );
						}
					} );
				}
			}
		};
		BeanInjector.getBean( TestRunner.class ).registerTask( testExecutionTask, Phase.PRE_START, Phase.POST_STOP );

		projectReleased.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				BeanInjector.getBean( TestRunner.class ).unregisterTask( testExecutionTask, Phase.values() );
			}
		} );
	}

	@FXML
	public void newScenario()
	{
		log.info( "New Scenario requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, project ) );
	}

	@FXML
	public void newProjectWizard()
	{
		log.info( "New Project Wizard requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, project.getWorkspace() ) );
	}

	@FXML
	public void cloneProject()
	{
		log.info( "Clone Project requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CLONE, project ) );
	}

	@FXML
	public void saveProject()
	{
		log.info( "Save Project requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_SAVE, project ) );
	}

	@FXML
	public void renameProject()
	{
		log.info( "Rename Project requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, project ) );
	}

	@FXML
	public void saveProjectAndClose()
	{
		log.info( "Saving and closing project requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new SaveAndCloseTask() ) );
	}

	@FXML
	public void closeProject()
	{
		log.info( "Close project requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, project ) );
	}

	@FXML
	public void openHelpPage()
	{
		log.info( "Open help page requested" );
		UIUtils.openInExternalBrowser( HELP_PAGE );
	}

	@FXML
	public void openSettings()
	{
		log.info( "Open settings page" );
		ProjectSettingsDialog.newInstance( this, project ).show();
	}

	@FXML
	public void openSummaryPage()
	{
		log.info( "Open summary requested" );
		ReportingManager reportingManager = BeanInjector.getBean( ReportingManager.class );
		reportingManager.createReport( lastRunCanvas.getSummary() );
	}

	private ProjectRef getProjectRef( String id )
	{
		return Iterables.find( project.getWorkspace().getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return Objects.equal( input.getProjectId(), project.getId() );
			}
		} );
	}

	private class SaveAndCloseTask extends Task<ProjectRef>
	{
		public SaveAndCloseTask()
		{
			updateMessage( "Saving and closing project: " + project.getLabel() + "..." );
		}

		@Override
		protected ProjectRef call() throws Exception
		{
			project.save();
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					ProjectView.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, project ) );
				}
			} );
			return getProjectRef( project.getId() );
		}
	}

}
