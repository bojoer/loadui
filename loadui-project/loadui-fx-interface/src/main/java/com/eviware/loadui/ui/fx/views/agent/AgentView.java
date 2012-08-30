package com.eviware.loadui.ui.fx.views.agent;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AgentView extends StackPane
{
	@FXML
	private ToggleButton onOffSwitch;

	@FXML
	private MenuButton menuButton;

	private final AgentItem agent;
	private final StringProperty labelProperty;
	private final StringProperty urlProperty;
	private final BooleanProperty enabledProperty;
	private final ReadOnlyBooleanProperty readyProperty;

	public AgentView( final AgentItem agent )
	{
		this.agent = agent;

		labelProperty = ( StringProperty )Properties.forLabel( agent );
		urlProperty = Properties.stringProperty( agent, "url", AgentItem.URL );
		enabledProperty = Properties.booleanProperty( agent, "enabled", AgentItem.ENABLED );
		readyProperty = Properties.readOnlyBooleanProperty( agent, "ready", AgentItem.READY );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		onOffSwitch.selectedProperty().bindBidirectional( enabledProperty );
		onOffSwitch.textProperty().bind(
				Bindings.when( enabledProperty ).then( Bindings.when( readyProperty ).then( "C" ).otherwise( "D" ) )
						.otherwise( "O" ) );
		Tooltip readyTooltip = new Tooltip();
		readyTooltip.textProperty().bind( Bindings.when( readyProperty ).then( "Connected" ).otherwise( "Disconnected" ) );
		onOffSwitch.setTooltip( readyTooltip );

		menuButton.textProperty().bind( labelProperty );

		Tooltip menuTooltip = new Tooltip();
		menuTooltip.textProperty().bind( Bindings.format( "%s (%s)", labelProperty, urlProperty ) );
		menuButton.setTooltip( menuTooltip );
	}

	@Override
	public String toString()
	{
		return agent.getLabel();
	}

	@FXML
	public void delete()
	{
		//TODO: Show dialog?
		agent.delete();
	}
}