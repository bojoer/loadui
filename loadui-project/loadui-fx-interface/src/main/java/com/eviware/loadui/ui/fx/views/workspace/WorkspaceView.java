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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.SceneBuilder;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.util.BeanInjector;
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
			System.out.println( "Opening dialog" );
			final Stage dialog = StageBuilder.create().resizable( false ).title( "Dialog" )
					.icons( BeanInjector.getBean( Stage.class ).getIcons() ).build();

			dialog.setScene( SceneBuilder
					.create()
									.root(
											VBoxBuilder
													.create()
													.spacing( 25 )
									.padding( new Insets( 20, 75, 20, 75 ) )
									.alignment( Pos.CENTER )
													.children( LabelBuilder.create().text( "A dialog window" ).build(),
											ButtonBuilder.create().onAction( new EventHandler<ActionEvent>()
											{
												@Override
												public void handle( ActionEvent arg0 )
												{
													dialog.close();
												}
											} ).text( "Ok" ).build() ).build() ).build() );
			dialog.initStyle( StageStyle.UTILITY );
			dialog.initModality( Modality.APPLICATION_MODAL );

			Window window = getScene().getWindow();
			double x = window.getX() + window.getWidth() / 2;
			double y = window.getY() + window.getHeight() / 2;

			final Parent root = getScene().getRoot();
			final Effect effect = root.getEffect();
			root.setEffect( GaussianBlurBuilder.create().radius( 8 ).build() );
			dialog.setOnHidden( new EventHandler<WindowEvent>()
	{
		@Override
				public void handle( WindowEvent arg0 )
		{
					root.setEffect( effect );
			}
			} );
			dialog.show();

			dialog.setX( x - dialog.getWidth() / 2 );
			dialog.setY( y - dialog.getHeight() / 2 );

			System.out.println( "width: " + dialog.getWidth() + " height: " + dialog.getHeight() );
			System.out.println( "x: " + dialog.getX() + " y: " + dialog.getY() );
			}
			}
}
