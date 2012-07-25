package com.eviware.loadui.ui.fx.views.agent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AgentView extends StackPane
{
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

		getChildren().setAll( FXMLUtils.load( AgentView.class, new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				return new Controller();
			}
		} ) );
	}

	@Override
	public String toString()
	{
		return agent.getLabel();
	}

	public final class Controller implements Initializable
	{
		@FXML
		private ToggleButton onOffSwitch;

		@FXML
		private MenuButton menuButton;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			onOffSwitch.selectedProperty().bindBidirectional( enabledProperty );
			onOffSwitch.textProperty().bind(
					Bindings.when( enabledProperty ).then( Bindings.when( readyProperty ).then( "C" ).otherwise( "D" ) )
							.otherwise( "O" ) );
			Tooltip readyTooltip = new Tooltip();
			readyTooltip.textProperty().bind(
					Bindings.when( readyProperty ).then( "Connected" ).otherwise( "Disconnected" ) );
			onOffSwitch.setTooltip( readyTooltip );

			menuButton.textProperty().bind( labelProperty );

			Tooltip menuTooltip = new Tooltip();
			menuTooltip.textProperty().bind( Bindings.format( "%s (%s)", labelProperty, urlProperty ) );
			menuButton.setTooltip( menuTooltip );
		}

		public void delete()
		{
			//TODO: Show dialog?
			agent.delete();
		}
	}
}