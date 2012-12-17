package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.Arrays;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBoxBuilder;

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.util.Properties;

public class OnOffComponentView extends ComponentView
{
	private final OptionsSlider onOffSlider = new OptionsSlider( Arrays.asList( "ON", "OFF" ) );
	private final Property<Boolean> stateProperty;

	protected OnOffComponentView( ComponentItem component )
	{
		super( component );

		getStyleClass().add( "on-off" );

		onOffSlider.getStyleClass().add( "switch" );

		topBar.setLeft( HBoxBuilder.create().spacing( 3 ).children( onOffSlider, topBar.getLeft() ).build() );

		stateProperty = Properties.convert( ( ( OnOffCategory )component.getBehavior() ).getStateProperty() );

		stateProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean state )
			{
				onOffSlider.setSelected( state.booleanValue() ? "ON" : "OFF" );
			}
		} );

		onOffSlider.setSelected( stateProperty.getValue().booleanValue() ? "ON" : "OFF" );

		onOffSlider.selectedProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String state )
			{
				stateProperty.setValue( "ON".equals( state ) );
			}
		} );
	}
}
