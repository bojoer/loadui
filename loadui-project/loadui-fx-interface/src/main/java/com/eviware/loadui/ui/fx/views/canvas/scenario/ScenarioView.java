package com.eviware.loadui.ui.fx.views.canvas.scenario;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.eviware.loadui.ui.fx.views.canvas.MiniScenarioPlaybackPanel;

public class ScenarioView extends CanvasObjectView
{
	public static final String HELP_PAGE = "http://loadui.org/Working-with-loadUI/scenarios.html";

	public ScenarioView( SceneItem scenario )
	{
		super( scenario );

		FXMLUtils.load( content, new Controller(),
				ScenarioView.class.getResource( ScenarioView.class.getSimpleName() + ".fxml" ) );

		menuButton.getItems().add( 0,
				MenuItemBuilder.create().id( "open" ).text( "Open" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						open();
					}
				} ).build() );
	}

	public SceneItem getScenario()
	{
		return ( SceneItem )getCanvasObject();
	}

	public void open()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, getCanvasObject() ) );
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
					if( event.getButton().equals( MouseButton.PRIMARY ) )
					{
						if( event.getClickCount() == 2 )
						{
							open();
						}
					}
				}
			} );
		}
	}
}
