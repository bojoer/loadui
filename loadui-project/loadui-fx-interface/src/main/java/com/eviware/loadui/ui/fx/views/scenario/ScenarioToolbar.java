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
		AnchorPane.setTopAnchor( playbackPanel, 4d );
		AnchorPane.setLeftAnchor( playbackPanel, 440d );
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
