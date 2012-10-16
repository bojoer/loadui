package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class OptionsSliderSkin<T> extends SkinBase<OptionsSlider<T>, BehaviorBase<OptionsSlider<T>>>
{
	public OptionsSliderSkin( final OptionsSlider<T> slider )
	{
		super( slider, new BehaviorBase<>( slider ) );

		final ToggleGroup toggleGroup = new ToggleGroup();

		slider.selectedProperty().addListener( new ChangeListener<T>()
		{
			@Override
			public void changed( ObservableValue<? extends T> arg0, T oldValue, T newValue )
			{
				for( Toggle toggle : toggleGroup.getToggles() )
				{
					if( newValue.equals( toggle.getUserData() ) )
					{
						toggle.setSelected( true );
						break;
					}
				}
			}
		} );

		toggleGroup.selectedToggleProperty().addListener( new ChangeListener<Toggle>()
		{
			@SuppressWarnings( "unchecked" )
			@Override
			public void changed( ObservableValue<? extends Toggle> arg0, Toggle oldValue, Toggle newValue )
			{
				slider.setSelected( ( T )newValue.getUserData() );
			}
		} );

		VBox vBox = VBoxBuilder.create().styleClass( "container" ).build();

		for( int i = 0; i < slider.getOptions().size(); i++ )
		{
			RadioButton radio = RadioButtonBuilder.create().toggleGroup( toggleGroup ).build();
			radio.setUserData( slider.getOptions().get( i ) );
			if( i == 0 )
			{
				radio.setSelected( true );
			}
			slider.labelRadioButton( radio, i );
			vBox.getChildren().add( radio );
		}

		AnchorPane a = new AnchorPane();
		a.setStyle( "fx-background-color: blue;" );

		Region r = RegionBuilder.create().styleClass( "sliding-area" ).build();

		AnchorPane.setTopAnchor( r, 0.0 );
		AnchorPane.setLeftAnchor( r, 0.0 );
		AnchorPane.setBottomAnchor( r, 0.0 );

		AnchorPane.setTopAnchor( vBox, 0.0 );
		AnchorPane.setRightAnchor( vBox, 0.0 );
		AnchorPane.setBottomAnchor( vBox, 0.0 );
		AnchorPane.setLeftAnchor( vBox, 0.0 );

		a.getChildren().setAll( r, vBox );

		getChildren().add( a );
	}
}