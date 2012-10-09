package com.eviware.loadui.ui.fx.views.canvas.component;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.HBoxBuilder;

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.util.Properties;

public class OnOffComponentView extends ComponentView
{
	private final ToggleButton onOffButton = ToggleButtonBuilder.create().text( "ON" ).selected( true ).build();

	protected OnOffComponentView( ComponentItem component )
	{
		super( component );

		topBar.setLeft( HBoxBuilder.create().spacing( 3 ).children( onOffButton, topBar.getLeft() ).build() );

		onOffButton.selectedProperty().bindBidirectional(
				Properties.convert( ( ( OnOffCategory )component.getBehavior() ).getStateProperty() ) );
	}
}
