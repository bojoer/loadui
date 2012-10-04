package com.eviware.loadui.ui.fx.views.canvas.scenario;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItemBuilder;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
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
		private MiniScenarioPlaybackPanel playbackPanel;

		@FXML
		private void initialize()
		{
			playbackPanel.setCanvas( getScenario() );
		}
	}
}
