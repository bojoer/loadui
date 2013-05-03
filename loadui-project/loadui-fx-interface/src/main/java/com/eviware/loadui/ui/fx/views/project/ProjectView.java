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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
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
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.api.intent.AbortableTask;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DetachableTab;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.ui.fx.views.analysis.reporting.LineChartUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.eviware.loadui.ui.fx.views.scenario.ScenarioToolbar;
import com.eviware.loadui.ui.fx.views.statistics.StatisticsView;
import com.eviware.loadui.ui.fx.views.workspace.CloneProjectDialog;
import com.eviware.loadui.ui.fx.views.workspace.CreateNewProjectDialog;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.projects.ProjectUtils;
import com.google.common.base.Preconditions;
import com.sun.javafx.PlatformUtil;

public class ProjectView extends AnchorPane
{
	private static final String HELP_PAGE = "http://www.loadui.org/interface/project-view.html";

	private static final Logger log = LoggerFactory.getLogger( ProjectView.class );

	private static final String TOOLBAR_STYLE_WITHOUT_SCENARIO = "-fx-background-color: linear-gradient(to bottom, -base-color-mid 0%, -fx-header-color 75%, #000000 76%, #272727 81%);";
	private static final String TOOLBAR_STYLE_WITH_SCENARIO = "-fx-background-color: linear-gradient(-base-color-mid 0%, -base-color-mid 74%, #555555 75%, #DDDDDD 76%, -base-color-mid 77%, -base-color-mid 100%);";

	@FXML
	private DetachableTab designTab;

	@FXML
	private DetachableTab statsTab;

	@FXML
	private MenuButton menuButton;

	@FXML
	private Button summaryButton;

	private ProjectPlaybackPanel playbackPanel;

	private ToggleButton linkButton;

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
					fireEvent( abortableTaskEvent( execution ) );
				}

				private IntentEvent<AbortableTask> abortableTaskEvent( final TestExecution execution )
				{
					return IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING_ABORTABLE,
							AbortableTask.onRun( new Runnable()
							{
								@Override
								public void run()
								{
									try
									{
										execution.complete().get();
									}
									catch( InterruptedException ie )
									{
										log.info( "Aborted running requests, not waiting for execution to complete" );
									}
									catch( ExecutionException ee )
									{
										log.warn( "There was a problem getting the result of an Execution", ee );
									}
								}
							} ).onAbort( new Runnable()
							{
								@Override
								public void run()
								{
									execution.abort( "Aborting running requests..." );
									project.cancelScenes( false );
									project.cancelComponents();
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
		log.info( "Initializing ProjectView" );
		playbackPanel = new ProjectPlaybackPanel( project );
		AnchorPane.setTopAnchor( playbackPanel, 7d );
		AnchorPane.setLeftAnchor( playbackPanel, 440.0 );
		getChildren().add( playbackPanel );

		menuButton.textProperty().bind( Properties.forLabel( project ) );
		designTab.setDetachableContent( this, new ProjectCanvasView( project ) );
		statsTab.setDetachableContent( this, new StatisticsView( project, executionsInfo ) );
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

						ProjectUtils.getProjectRef( project ).setAttribute( "miniature_fx2", base64 );

						grid.setVisible( true );
						event.consume();
					}
					else
					{
						log.debug( "Unhandled intent: %s", event );
						return;
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_OPEN && event.getArg() instanceof SceneItem )
				{
					SceneItem scenario = ( SceneItem )event.getArg();

					ToolBar projectToolbar = ( ( ToolBar )lookup( ".tool-bar" ) );
					projectToolbar.setStyle( TOOLBAR_STYLE_WITH_SCENARIO );

					ScenarioToolbar toolbar = new ScenarioToolbar( scenario );

					linkButton = ToggleButtonBuilder
							.create()
							.id( "link-scenario" )
							.graphic(
									HBoxBuilder
											.create()
											.children( RegionBuilder.create().styleClass( "graphic" ).build(),
													RegionBuilder.create().styleClass( "secondary-graphic" ).build() ).build() )
							.build();
					
					Property<Boolean> linkedProperty = Properties.convert( scenario.followProjectProperty() );
					linkButton.selectedProperty().bindBidirectional( linkedProperty );
					linkButton.visibleProperty().bind( statsTab.selectedProperty().not() );
					AnchorPane.setLeftAnchor( linkButton, 473d );
					AnchorPane.setTopAnchor( linkButton, 55d );
					ProjectView.this.getChildren().add( linkButton );

					StackPane.setAlignment( toolbar, Pos.TOP_CENTER );
					CanvasView canvas = new CanvasView( scenario );
					StackPane.setMargin( canvas, new Insets( 60, 0, 0, 0 ) );
					StackPane pane = StackPaneBuilder.create().children( canvas, toolbar ).build();
					designTab.setDetachableContent( ProjectView.this, pane );

					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CLOSE && event.getArg() instanceof SceneItem )
				{
					( ( ToolBar )lookup( ".tool-bar" ) ).setStyle( TOOLBAR_STYLE_WITHOUT_SCENARIO );
					ProjectView.this.getChildren().remove( linkButton );

					javafx.scene.layout.Pane canvas = ( javafx.scene.layout.Pane )lookup( ".pane" );
					javafx.scene.layout.Region grid = ( javafx.scene.layout.Region )lookup( ".grid" );
					StackPane parent = ( StackPane )grid.getParent();
					parent.getChildren().remove( grid );
					parent.getChildren().remove( canvas );

					Region gridRegion = RegionBuilder.create().styleClass( "grid" ).style( "-fx-background-repeat: repeat;" )
							.build();
					//Hack for setting CSS resources within an OSGi framework
					String gridUrl = CanvasView.class.getResource( "grid-box.png" ).toExternalForm();
					gridRegion.setStyle( "-fx-background-image: url('" + gridUrl + "');" );

					StackPane completeCanvas = StackPaneBuilder.create().children( gridRegion, canvas ).build();
					SceneBuilder.create().root( completeCanvas ).width( canvas.getWidth() ).height( canvas.getHeight() )
							.build();

					WritableImage fxImage = completeCanvas.snapshot( null, null );
					BufferedImage bimg = SwingFXUtils.fromFXImage( fxImage, null );
					bimg = UIUtils.scaleImage( bimg, 332, 175 );
					String base64 = NodeUtils.toBase64Image( bimg );

					SceneItem scenario = ( SceneItem )event.getArg();
					scenario.setAttribute( "miniature_fx2", base64 );

					designTab.setDetachableContent( ProjectView.this, new ProjectCanvasView( project ) );
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CLONE )
				{
					ProjectRef projectRef = ProjectUtils.getProjectRef( project );
					new CloneProjectDialog( project.getWorkspace(), projectRef, ProjectView.this ).show();
					event.consume();
					return;
				}
			}
		} );

		statsTab.selectedProperty().addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> _, Boolean __, Boolean ___ )
			{
				if( statsTab.selectedProperty().get() )
				{
					( ( ToolBar )lookup( ".tool-bar" ) ).setStyle( TOOLBAR_STYLE_WITH_SCENARIO );
				}
			}
		} );

		designTab.selectedProperty().addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> _, Boolean __, Boolean ___ )
			{
				if( designTab.selectedProperty().get() )
				{
					if( designTab.getContent().lookup( ".scenario-toolbar" ) != null )
					{
						( ( ToolBar )lookup( ".tool-bar" ) ).setStyle( TOOLBAR_STYLE_WITH_SCENARIO );
					}
					else
					{
						( ( ToolBar )lookup( ".tool-bar" ) ).setStyle( TOOLBAR_STYLE_WITHOUT_SCENARIO );
					}
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
		StatisticsView statisticsView = ( StatisticsView )statsTab.getDetachableContent();
		statisticsView.close();
		log.info( "Close project requested" );
		executionsInfo.reset();
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
		ReadOnlyProperty<Execution> executionProp = statisticsView.currentExecutionProperty();
		Collection<StatisticPage> pages = project.getStatisticPages().getChildren();

		Map<ChartView, Image> images = LineChartUtils.createImages( pages, executionProp, null );

		// Remove the Mac special case when we have switched to JavaFX 8.
		if( PlatformUtil.isMac() )
		{
			File reportFile = new File( "LoadUI_report.pdf" );
			reportingManager.createReport( project.getLabel(), executionProp.getValue(), pages, images, new File(
					"LoadUI_report.pdf" ), "PDF", executionProp.getValue().getSummaryReport() );
			BeanInjector.getBean( TestEventManager.class ).logMessage( MessageLevel.WARNING,
					"Report saved to " + reportFile.getAbsolutePath() );
		}
		else
		{
			reportingManager.createReport( project.getLabel(), executionProp.getValue(), pages, images, executionProp
					.getValue().getSummaryReport() );
		}
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
					closeProject();
				}
			} );
			return ProjectUtils.getProjectRef( project );
		}
	}

}
