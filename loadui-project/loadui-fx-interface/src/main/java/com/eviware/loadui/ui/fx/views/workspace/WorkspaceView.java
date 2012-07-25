package com.eviware.loadui.ui.fx.views.workspace;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import javax.annotation.Nullable;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.agent.AgentView;
import com.eviware.loadui.ui.fx.views.projectref.ProjectRefView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

public class WorkspaceView extends StackPane
{
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

					fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new Runnable()
					{
						@Override
						public void run()
						{
							File projectFile = projectRef.getProjectFile();
							File cloneFile = null;
							int count = 1;
							while( ( cloneFile = new File( projectFile.getParentFile(), String.format( "copy-%d-of-%s", count,
									projectFile.getName() ) ) ).exists() )
							{
								count++ ;
							}

							try
							{
								Files.copy( projectFile, cloneFile );
								ProjectRef cloneRef = workspace.importProject( cloneFile, true );

								ProjectItem cloneProject = cloneRef.getProject();
								cloneProject.setLabel( String.format( "Copy %d of %s", count, projectRef.getLabel() ) );
								cloneProject.save();
								cloneRef.setEnabled( false );

								//TODO: Remote if miniatures aren't generated in the same way.
								cloneRef.setAttribute( "miniature", projectRef.getAttribute( "miniature", "" ) );

								workspace.save();
							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}
					} ) );

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
						//TODO: Open dialog.
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
				return new Controller();
			}
		} ) );
	}

	public final class Controller implements Initializable
	{
		@FXML
		private MenuButton workspaceButton;

		@FXML
		private Carousel<ProjectRefView> projectRefCarousel;

		@FXML
		private Carousel<Node> agentCarousel;

		@FXML
		private WebView webView;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			workspaceButton.textProperty().bind( Bindings.format( "Workspace: %s", Properties.forLabel( workspace ) ) );

			initProjectRefCarousel();
			initAgentCarousel();

			webView.getEngine().load( "http://www.loadui.org/loadUI-starter-pages/loadui-starter-page-os.html" );
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
			ObservableLists.bindSorted( projectRefCarousel.getItems(),
					ObservableLists.transform( projectRefList, new Function<ProjectRef, ProjectRefView>()
					{
						@Override
						public ProjectRefView apply( ProjectRef projectRef )
						{
							return new ProjectRefView( projectRef );
						}
					} ), Ordering.usingToString() );

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

		public void exit()
		{
			getScene().getWindow().hide();
		}
	}
}
