package com.eviware.loadui.ui.fx.views.agent;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AgentView extends StackPane
{
	@FXML
	private OptionsSlider onOffSwitch;

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

		labelProperty = Properties.forLabel( agent );
		urlProperty = Properties.stringProperty( agent, "url", AgentItem.URL );
		enabledProperty = Properties.booleanProperty( agent, "enabled", AgentItem.ENABLED );
		readyProperty = Properties.readOnlyBooleanProperty( agent, "ready", AgentItem.READY );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		//Hack for setting CSS resources within an OSGi framework
		String dotPatternUrl = AgentView.class.getResource( "dot-pattern.png" ).toExternalForm();
		lookup( ".agent-view" ).setStyle( "-fx-background-image: url('" + dotPatternUrl + "');" );

		Tooltip readyTooltip = new Tooltip();
		readyTooltip.textProperty().bind( Bindings.when( readyProperty ).then( "Connected" ).otherwise( "Disconnected" ) );
		onOffSwitch.setTooltip( readyTooltip );
		System.out.println( "!!!!!!!!!!ON OFF: " + onOffSwitch.getStyleClass() );

		menuButton.textProperty().bind( labelProperty );

		Tooltip menuTooltip = new Tooltip();
		menuTooltip.textProperty().bind( Bindings.format( "%s (%s)", labelProperty, urlProperty ) );
		menuButton.setTooltip( menuTooltip );

		enabledProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean state )
			{
				onOffSwitch.setSelected( state.booleanValue() ? "ON" : "OFF" );
			}
		} );

		onOffSwitch.setSelected( enabledProperty.getValue().booleanValue() ? "ON" : "OFF" );

		onOffSwitch.selectedProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String state )
			{
				enabledProperty.setValue( "ON".equals( state ) );
			}
		} );
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