package com.eviware.loadui.ui.fx.views.canvas;

import javafx.fxml.FXML;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class ScenarioView extends CanvasObjectView
{
	@FXML
	private ScenarioPlaybackPanel playbackPanel;

	public ScenarioView( SceneItem scenario )
	{
		super( scenario );
		FXMLUtils.load( this, this, ScenarioView.class.getResource( ScenarioView.class.getSimpleName() + ".fxml" ) );
		playbackPanel.setCanvas( scenario );
	}

	public SceneItem getScenario()
	{
		return ( SceneItem )getCanvasObject();
	}

	@FXML
	public void open()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, canvasObject ) );
	}

	@FXML
	public void settings()
	{
		//TODO
	}
}
