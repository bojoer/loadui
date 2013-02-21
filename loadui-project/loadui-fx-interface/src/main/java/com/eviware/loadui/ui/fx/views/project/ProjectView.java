package com.eviware.loadui.ui.fx.views.project;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.ui.fx.views.analysis.reporting.LineChartUtils;
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
	private DetachableTab statsTab;

	@FXML
	private MenuButton menuButton;

	@FXML
	private Button summaryButton;

	private ProjectPlaybackPanel playbackPanel;

	private final FxExecutionsInfo executionsInfo;

	private final ProjectItem project;

	private final Observable projectReleased;

	private final TestExecutionTask blockWindowTask = new TestExecutionTask()
	{
		@Override
		public void invoke( final TestExecution execution, Phase phase )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								execution.complete().get(); // Wait until execution is complete.
							}
							catch( InterruptedException | ExecutionException e )
							{
								e.printStackTrace();
							}
						}
					} ) );
				}
			} );
		}
	};

	public ProjectPlaybackPanel getPlaybackPanel()
	{
		return playbackPanel;
	}

	public ProjectView( ProjectItem projectIn, FxExecutionsInfo executionsInfo )
	{
		this.project = Preconditions.checkNotNull( projectIn );
		this.executionsInfo = executionsInfo;
		projectReleased = Properties.observeEvent( project, ProjectItem.RELEASED );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		playbackPanel = new ProjectPlaybackPanel( project );
		AnchorPane.setTopAnchor( playbackPanel, 7d );
		AnchorPane.setLeftAnchor( playbackPanel, 440.0 );
		getChildren().add( playbackPanel );

		menuButton.textProperty().bind( Properties.forLabel( project ) );
		designTab.setDetachableContent( new ProjectCanvasView( project ) );
		statsTab.setDetachableContent( new StatisticsView( project, executionsInfo ) );
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

						Node canvas = lookup( "#snapshotArea" );
						Node grid = lookup( ".tool-box" );
						grid.setVisible( false );

						javafx.scene.SnapshotParameters parameters = new javafx.scene.SnapshotParameters();
						parameters.setViewport( new javafx.geometry.Rectangle2D( 100, 60, 620, 324 ) );
						WritableImage fxImage = canvas.snapshot( parameters, null );
						BufferedImage bimg = SwingFXUtils.fromFXImage( fxImage, null );
						bimg = UIUtils.scaleImage( bimg, 120, 64 );
						String base64 = NodeUtils.toBase64Image( bimg );

						for( ProjectRef pRef : project.getWorkspace().getProjectRefs() )
						{
							if( pRef.isEnabled() && pRef.getProject() == project )
							{
								pRef.setAttribute( "miniature_fx2", base64 );
								break;
							}
						}

						grid.setVisible( true );
						event.consume();
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
					Group canvas = ( Group )lookup( ".canvas-layer" );
					StackPane grid = ( StackPane )lookup( ".grid-pane" );
					StackPane parent = ( StackPane )grid.getParent();
					parent.getChildren().remove( grid );
					parent.getChildren().remove( canvas );

					StackPane completeCanvas = StackPaneBuilder.create().children( grid, canvas ).build();
					SceneBuilder.create().root( completeCanvas ).width( 996 ).height( 525 ).build();

					WritableImage fxImage = completeCanvas.snapshot( null, null );
					BufferedImage bimg = SwingFXUtils.fromFXImage( fxImage, null );
					bimg = UIUtils.scaleImage( bimg, 332, 175 );
					String base64 = NodeUtils.toBase64Image( bimg );

					SceneItem scenario = ( SceneItem )event.getArg();
					scenario.setAttribute( "miniature_fx2", base64 );

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
							summaryButton.setDisable( false );
						}
					} );
				}
			}
		};
		BeanInjector.getBean( TestRunner.class ).registerTask( testExecutionTask, Phase.PRE_START, Phase.POST_STOP );
		BeanInjector.getBean( TestRunner.class ).registerTask( blockWindowTask, Phase.PRE_STOP );

		projectReleased.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				BeanInjector.getBean( TestRunner.class ).unregisterTask( testExecutionTask, Phase.values() );
			}
		} );
	}

	public ProjectItem getProject()
	{
		return project;
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

		StatisticsView statisticsView = ( StatisticsView )statsTab.getDetachableContent();
		ReadOnlyProperty<Execution> execution = statisticsView.currentExecutionProperty();
		Collection<StatisticPage> pages = project.getStatisticPages().getChildren();

		Map<ChartView, Image> images = LineChartUtils.createImages( pages, execution, null );

		reportingManager.createReport( project.getLabel(), execution.getValue(), pages, images, execution.getValue()
				.getSummaryReport() );
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
