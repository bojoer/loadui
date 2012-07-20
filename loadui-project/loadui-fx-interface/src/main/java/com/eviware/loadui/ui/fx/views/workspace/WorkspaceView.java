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
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.Dialog;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;

public class WorkspaceView extends Region
{
	private final WorkspaceItem workspace;
	private final ObservableList<ProjectRef> projectRefList;

	public WorkspaceView( WorkspaceItem workspace )
	{
		this.workspace = workspace;
		projectRefList = ObservableLists.ofCollection( workspace, WorkspaceItem.PROJECT_REFS, ProjectRef.class,
				workspace.getProjectRefs() );

		getChildren().add( FXMLUtils.load( WorkspaceView.class, new Callable<Object>()
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
		//		@FXML
		//		private Label workspaceLabel;

		@FXML
		private MenuButton workspaceButton;

		@FXML
		private ListView<ProjectRefNode> projectRefNodeList;

		@FXML
		private WebView webView;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			workspaceButton.textProperty().bind( Bindings.format( "Workspace: %s", Properties.forLabel( workspace ) ) );

			Bindings.bindContent( projectRefNodeList.getItems(), ObservableLists.fx( ObservableLists.transform(
					projectRefList, new Function<ProjectRef, ProjectRefNode>()
					{
						@Override
						public ProjectRefNode apply( ProjectRef projectRef )
						{
							return new ProjectRefNode( projectRef );
						}
					} ) ) );

			webView.getEngine().load( "http://www.loadui.org/loadUI-starter-pages/loadui-starter-page-os.html" );

			projectRefNodeList.setContextMenu( ContextMenuBuilder.create()
					.items( MenuItemBuilder.create().text( "Create Project" ).onAction( new EventHandler<ActionEvent>()
					{
						@Override
						public void handle( ActionEvent arg0 )
						{
							fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new Runnable()
							{
								@Override
								public void run()
								{
									try
									{
										workspace.createProject(
												File.createTempFile( "loadui-project", ".xml",
														new File( System.getProperty( LoadUI.LOADUI_HOME ) ) ), "New Project", false );
									}
									catch( IOException e )
									{
										throw new RuntimeException( e );
									}
								}
							} ) );
						}
					} ).build() ).build() );
		}

		public void exit()
		{
			getScene().getWindow().hide();
		}

		public void openDialog()
		{
			final Dialog dialog = new ConfirmationDialog( getScene(), "Save", "Do you really want to?", "It is dangerous." );

			//			dialog.getChildren().setAll( LabelBuilder.create().text( "A dialog window" ).build(),
			//					ButtonBuilder.create().onAction( new EventHandler<ActionEvent>()
			//					{
			//						@Override
			//						public void handle( ActionEvent arg0 )
			//						{
			//							dialog.close();
			//						}
			//					} ).text( "Ok" ).build() );

			dialog.show();
		}
	}
}
