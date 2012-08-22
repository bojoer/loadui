package com.eviware.loadui.ui.fx.views.workspace;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Observables;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.agent.AgentView;
import com.eviware.loadui.ui.fx.views.projectref.ProjectRefView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.sun.javafx.PlatformUtil;

public class WorkspaceView extends StackPane
{
	private static final Logger LOG = LoggerFactory.getLogger( WorkspaceView.class );
	private static final String LATEST_DIRECTORY = "gui.latestDirectory";
	private static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );

	private final WorkspaceItem workspace;
	private final ObservableList<ProjectRef> projectRefList;
	private final ObservableList<AgentItem> agentList;

	public WorkspaceView( final WorkspaceItem workspace )
	{
		this.workspace = workspace;
		projectRefList = ObservableLists.fx( ObservableLists.ofCollection( workspace, WorkspaceItem.PROJECT_REFS,
				ProjectRef.class, workspace.getProjectRefs() ) );
		agentList = ObservableLists.fx( ObservableLists.ofCollection( workspace, WorkspaceItem.AGENTS, AgentItem.class,
				workspace.getAgents() ) );

		addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<? extends Object>>()
		{
			@Override
			public void handle( IntentEvent<? extends Object> event )
			{
				if( event.getEventType() == IntentEvent.INTENT_CLONE && event.getArg() instanceof ProjectRef )
				{
					final ProjectRef projectRef = ( ProjectRef )event.getArg();
					new CloneProjectDialog( workspace, projectRef, WorkspaceView.this ).show();
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CREATE )
				{
					if( event.getArg() == ProjectItem.class )
					{
						new CreateNewProjectDialog( workspace, WorkspaceView.this ).show();
						event.consume();
					}
					else if( event.getArg() == AgentItem.class )
					{
						new CreateNewAgentDialog( workspace, WorkspaceView.this ).show();
						event.consume();
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_OPEN && event.getArg() instanceof ProjectRef )
				{
					workspace.setAttribute( "lastOpenProject", ( ( ProjectRef )event.getArg() ).getProjectFile()
							.getAbsolutePath() );
				}
			}
		} );

		getChildren().setAll( FXMLUtils.load( WorkspaceView.class, new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				return new Controller( "res/application.properties" );
			}
		} ) );
	}

	public final class Controller implements Initializable
	{
		private static final String HELPER_PAGE_URL = "http://www.loadui.org";

		private final String propFile;

		@FXML
		private MenuButton workspaceButton;

		@FXML
		private Carousel<ProjectRefView> projectRefCarousel;

		@FXML
		private Carousel<Node> agentCarousel;

		@FXML
		private WebView webView;

		public Controller( String propFile )
		{
			this.propFile = propFile;
		}

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			workspaceButton.textProperty().bind( Bindings.format( "Workspace: %s", Properties.forLabel( workspace ) ) );

			initProjectRefCarousel();
			initAgentCarousel();

			java.util.Properties props = new java.util.Properties();

			try (InputStream propsStream = Files.newInputStreamSupplier( new File( propFile ) ).getInput())
			{
				props.load( propsStream );
			}
			catch( IOException e )
			{
				LOG.warn( "Unable to load resource file 'application.properties!'", e );
			}

			webView.getEngine().load( props.getProperty( "starter.page.url" ) );

			initGettingStartedWizard();
		}

		private void initGettingStartedWizard()
		{
			if( workspace.getAttribute( GettingStartedDialog.SHOW_GETTING_STARTED, "true" ).equals( "true" ) )
			{
				sceneProperty().addListener( new ChangeListener<Scene>()
				{
					@Override
					public void changed( ObservableValue<? extends Scene> sceneProperty, Scene oldScene, Scene newScene )
					{
						if( newScene != null )
						{
							sceneProperty.removeListener( this );
							newScene.windowProperty().addListener( new ChangeListener<Window>()
							{
								@Override
								public void changed( ObservableValue<? extends Window> windowProperty, Window oldWindow,
										final Window newWindow )
								{
									windowProperty.removeListener( this );
									newWindow.addEventHandler( WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
									{
										@Override
										public void handle( WindowEvent event )
										{
											newWindow.removeEventHandler( WindowEvent.WINDOW_SHOWN, this );
											Platform.runLater( new Runnable()
											{
												@Override
												public void run()
												{
													gettingStarted();
												}
											} );
										}
									} );
								}
							} );
						}
					}
				} );
			}
		}

		private void initAgentCarousel()
		{
			ObservableLists.bindSorted( agentCarousel.getItems(),
					ObservableLists.transform( agentList, new Function<AgentItem, AgentView>()
					{
						@Override
						public AgentView apply( AgentItem agent )
						{
							return new AgentView( agent );
						}
					} ), Ordering.usingToString() );

			agentCarousel.setSelected( Iterables.getFirst( agentCarousel.getItems(), null ) );

			agentCarousel.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
			{
				@Override
				public void handle( DraggableEvent event )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof NewAgentIcon )
					{
						event.accept();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, AgentItem.class ) );
					}
				}
			} );

			agentCarousel.setContextMenu( ContextMenuBuilder.create()
					.items( MenuItemBuilder.create().text( "Add Agent" ).onAction( new EventHandler<ActionEvent>()
					{
						@Override
						public void handle( ActionEvent arg0 )
						{
							fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, AgentItem.class ) );
						}
					} ).build() ).build() );
		}

		private void initProjectRefCarousel()
		{
			final Observables.Group group = Observables.group();

			ObservableLists.bindSorted( projectRefCarousel.getItems(),
					ObservableLists.transform( projectRefList, new Function<ProjectRef, ProjectRefView>()
					{
						@Override
						public ProjectRefView apply( ProjectRef projectRef )
						{
							return new ProjectRefView( projectRef );
						}
					} ), Ordering.usingToString(), group );

			Bindings.bindContent( group.getObservables(),
					ObservableLists.transform( projectRefCarousel.getItems(), new Function<ProjectRefView, Observable>()
					{
						@Override
						public Observable apply( ProjectRefView projectRefView )
						{
							return projectRefView.labelProperty();
						}
					} ) );

			final String lastProject = workspace.getAttribute( "lastOpenProject", "" );
			projectRefCarousel.setSelected( Iterables.find( projectRefCarousel.getItems(), new Predicate<ProjectRefView>()
			{
				@Override
				public boolean apply( @Nullable ProjectRefView view )
				{
					return lastProject.equals( view.getProjectRef().getProjectFile().getAbsolutePath() );
				}
			}, Iterables.getFirst( projectRefCarousel.getItems(), null ) ) );

			projectRefCarousel.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
			{
				@Override
				public void handle( DraggableEvent event )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED
							&& event.getData() instanceof NewProjectIcon )
					{
						event.accept();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
					}
				}
			} );

			projectRefCarousel.setContextMenu( ContextMenuBuilder.create()
					.items( MenuItemBuilder.create().text( "Create Project" ).onAction( new EventHandler<ActionEvent>()
					{
						@Override
						public void handle( ActionEvent arg0 )
						{
							fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
						}
					} ).build() ).build() );
		}

		public void importProject()
		{
			FileChooser fileChooser = FileChooserBuilder
					.create()
					.initialDirectory(
							new File( workspace.getAttribute( LATEST_DIRECTORY, System.getProperty( LoadUI.LOADUI_HOME ) ) ) )
					.extensionFilters( XML_EXTENSION_FILTER ).build();
			File file = fileChooser.showOpenDialog( getScene().getWindow() );
			if( file != null )
			{
				workspace.setAttribute( LATEST_DIRECTORY, file.getParentFile().getAbsolutePath() );
				fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new ImportProjectTask( workspace, file ) ) );
			}
		}

		public void openHelpPage()
		{
			if( !PlatformUtil.isMac() )
			{
				try
				{
					Desktop.getDesktop().browse( new java.net.URI( HELPER_PAGE_URL ) );
				}
				catch( IOException | URISyntaxException e )
				{
					LOG.error( "Unable to launch browser with helper page in external browser!", e );
				}
				return;
			}

			try
			{
				Thread t = new Thread( new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							Runtime.getRuntime().exec( "open " + HELPER_PAGE_URL );
						}
						catch( IOException e )
						{
							LOG.error( "Unable to fork native browser with helper page in external browser!", e );
						}
					}
				} );
				t.start();
			}
			catch( Exception e )
			{
				LOG.error( "unable to display help page!", e );
			}
		}

		public void gettingStarted()
		{
			new GettingStartedDialog( workspace, WorkspaceView.this ).show();
		}

		public void exit()
		{
			getScene().getWindow().hide();
		}
	}
}
