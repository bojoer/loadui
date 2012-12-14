package com.eviware.loadui.ui.fx.views.scenario;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.canvas.scenario.ScenarioView;

public class ScenarioToolbar extends AnchorPane
{
	protected static final Logger log = LoggerFactory.getLogger( ScenarioToolbar.class );

	private final SceneItem scenario;

	@FXML
	private MenuButton menuButton;

	public ScenarioToolbar( SceneItem scenario )
	{
		this.scenario = scenario;
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		menuButton.textProperty().bind( Properties.forLabel( scenario ) );
		ScenarioPlaybackPanel playbackPanel = new ScenarioPlaybackPanel( scenario );
		AnchorPane.setTopAnchor( playbackPanel, 14.0 );
		AnchorPane.setLeftAnchor( playbackPanel, 440.0 );
		getChildren().add( playbackPanel );
	}

	@FXML
	public void renameScenario()
	{
		log.info( "Rename scenario requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, scenario ) );
	}

	@FXML
	public void closeScenario()
	{
		log.info( "Close scenario requested" );
		fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, scenario ) );
	}

	@FXML
	public void openHelpPage()
	{
		log.info( "Open scenario help page requested" );
		UIUtils.openInExternalBrowser( ScenarioView.HELP_PAGE );
	}
}
