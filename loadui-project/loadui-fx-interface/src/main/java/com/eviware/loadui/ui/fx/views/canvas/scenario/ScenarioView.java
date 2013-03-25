package com.eviware.loadui.ui.fx.views.canvas.scenario;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.eviware.loadui.ui.fx.views.canvas.MiniScenarioPlaybackPanel;

public class ScenarioView extends CanvasObjectView
{
	public static final String HELP_PAGE = "http://loadui.org/Working-with-loadUI/scenarios.html";
	private static final Options MENU_ITEM_OPTIONS = Options.are().open();

	public ScenarioView( SceneItem scenario )
	{
		super( scenario );
		getStyleClass().add( "scenario-view" );

		FXMLUtils.load( content, new Controller(),
				ScenarioView.class.getResource( ScenarioView.class.getSimpleName() + ".fxml" ) );

		HasMenuItems hasMenuItems = MenuItemsProvider.createWith( this, getCanvasObject(), MENU_ITEM_OPTIONS );
		menuButton.getItems().setAll( hasMenuItems.items() );
		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( hasMenuItems.items() ).build();

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );
	}

	public SceneItem getScenario()
	{
		return ( SceneItem )getCanvasObject();
	}

	@Override
	public void delete()
	{
		super.delete();
	}

	private final class Controller
	{
		@FXML
		private VBox vBox;

		@FXML
		private ImageView miniature;

		@FXML
		void initialize()
		{
			vBox.getChildren().add( 0, new MiniScenarioPlaybackPanel( getScenario() ) );
			String base64 = getCanvasObject().getAttribute( "miniature_fx2", null );

			if( base64 != null )
				miniature.setImage( NodeUtils.fromBase64Image( base64 ) );
			else
				miniature.setImage( new Image( ScenarioView.class.getResourceAsStream( "grid.png" ) ) );

			miniature.setOnMouseClicked( new EventHandler<MouseEvent>()
			{
				@Override
				public void handle( MouseEvent event )
				{
					if( event.getButton() == MouseButton.PRIMARY )
					{
						if( event.getClickCount() == 2 )
						{
							fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, getCanvasObject() ) );
						}
					}
				}
			} );
		}
	}
}
