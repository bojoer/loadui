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
package com.eviware.loadui.ui.fx.views.workspace;

import static com.eviware.loadui.ui.fx.util.ObservableLists.bindSorted;
import static javafx.beans.binding.Bindings.bindContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Observables;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.projectref.ProjectRefView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

public class WorkspaceView extends StackPane
{
	public static final String CREATE_PROJECT = "Create project";

	protected static final Logger log = LoggerFactory.getLogger( WorkspaceView.class );

	private static final String LATEST_DIRECTORY = "gui.latestDirectory";
	private static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );
	private static final String HELPER_PAGE_URL = "http://www.loadui.org/Working-with-loadUI/workspace-overview.html";
	private static final String PROP_FILE = "res/application.properties";

	private final WorkspaceItem workspace;
	private final ObservableList<ProjectRef> projectRefList;
	private final ObservableList<ProjectRefView> projectRefViews;

	@FXML
	private VBox carouselArea;

	@FXML
	private ToolBox<Label> toolbox;

	@FXML
	private Carousel<ProjectRefView> projectRefCarousel;

	//	@FXML
	//	private WebView webView;
	private ObservableList<ReadOnlyStringProperty> labelProperties;

	public WorkspaceView( final WorkspaceItem workspace )
	{
		this.workspace = workspace;
		projectRefList = ObservableLists.fx( ObservableLists.ofCollection( workspace, WorkspaceItem.PROJECT_REFS,
				ProjectRef.class, workspace.getProjectRefs() ) );

		projectRefViews = ObservableLists.transform( projectRefList, new Function<ProjectRef, ProjectRefView>()
		{
			@Override
			public ProjectRefView apply( ProjectRef projectRef )
			{
				return new ProjectRefView( projectRef );
			}
		} );

		FXMLUtils.load( this );
	}

	@FXML
	protected void initialize()
	{
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
				}
				else if( event.getEventType() == IntentEvent.INTENT_OPEN && event.getArg() instanceof ProjectRef )
				{
					workspace.setAttribute( "lastOpenProject", ( ( ProjectRef )event.getArg() ).getProjectFile()
							.getAbsolutePath() );
				}
			}
		} );

		initProjectRefCarousel();

		java.util.Properties props = new java.util.Properties();

		try (InputStream propsStream = Files.newInputStreamSupplier( new File( PROP_FILE ) ).getInput())
		{
			props.load( propsStream );
		}
		catch( IOException e )
		{
			log.warn( "Unable to load resource file 'application.properties!'", e );
		}

		//		webView.getEngine().setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>()
		//		{
		//			@Override
		//			public WebEngine call( PopupFeatures pf )
		//			{
		//				final WebEngine popupWebEngine = new WebEngine();
		//				popupWebEngine.locationProperty().addListener( new InvalidationListener()
		//				{
		//					@Override
		//					public void invalidated( Observable _ )
		//					{
		//						UIUtils.openInExternalBrowser( popupWebEngine.getLocation() );
		//					}
		//				} );
		//				return popupWebEngine;
		//			}
		//		} );
		//		webView.getEngine().load( props.getProperty( "starter.page.url" ) + "?version=" + LoadUI.VERSION );

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

	public ToolBox<Label> getToolbox()
	{
		return toolbox;
	}

	private void initProjectRefCarousel()
	{
		final Observables.Group<ReadOnlyStringProperty> group = Observables.group( new ReadOnlyStringProperty[0] );
		final MenuItem[] carouselMenuItems = MenuItemsProvider.createWith( projectRefCarousel, null,
				Options.are().noDelete().noRename().create( ProjectItem.class, CREATE_PROJECT ) ).items();

		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( carouselMenuItems ).build();

		projectRefCarousel.setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				boolean hasProject = !projectRefCarousel.getItems().isEmpty();
				if( hasProject && NodeUtils.isMouseOn( projectRefCarousel.getSelected().getMenuButton() ) )
					return; // never show contextMenu when on top of the menuButton

				ctxMenu.getItems().setAll(
						hasProject && NodeUtils.isMouseOn( projectRefCarousel.getSelected() ) ? projectRefCarousel
								.getSelected().getMenuItemProvider().items() : carouselMenuItems );
				MenuItemsProvider.showContextMenu( projectRefCarousel, ctxMenu );
				event.consume();
			}
		} );

		bindSorted( projectRefCarousel.getItems(), projectRefViews, Ordering.usingToString(), group );

		labelProperties = ObservableLists.transform( projectRefCarousel.getItems(),
				new Function<ProjectRefView, ReadOnlyStringProperty>()
				{
					@Override
					public ReadOnlyStringProperty apply( ProjectRefView projectRefView )
					{
						return projectRefView.labelProperty();
					}
				} );

		bindContent( group.getObservables(), labelProperties );

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
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof NewProjectIcon )
				{
					event.accept();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
				}
			}
		} );
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

	public VBox getCarouselArea()
	{
		return carouselArea;
	}

	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@FXML
	public void openHelpPage()
	{
		UIUtils.openInExternalBrowser( HELPER_PAGE_URL );
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
