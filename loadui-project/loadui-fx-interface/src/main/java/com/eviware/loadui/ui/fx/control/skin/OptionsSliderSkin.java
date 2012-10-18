package com.eviware.loadui.ui.fx.control.skin;

import java.util.Iterator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class OptionsSliderSkin extends SkinBase<OptionsSlider, BehaviorBase<OptionsSlider>>
{
	protected static final Logger log = LoggerFactory.getLogger( OptionsSliderSkin.class );

	public OptionsSliderSkin( final OptionsSlider slider )
	{
		super( slider, new BehaviorBase<>( slider ) );

		final ToggleGroup toggleGroup = new ToggleGroup();

		VBox vBox = VBoxBuilder.create().styleClass( "container" ).build();

		Iterator<ImageView> it = slider.getImages().iterator();
		for( String option : slider.getOptions() )
		{
			RadioButton radio = RadioButtonBuilder.create().toggleGroup( toggleGroup ).build();
			radio.setUserData( option );
			//			if( i == 0 )
			//			{
			//				System.out.println( "select! " );
			//								slider.setSelected( slider.getOptions().get( 0 ) );
			//								radio.setSelected( true );
			//			}

			if( slider.showLabels() )
				radio.setText( option );
			radio.setGraphic( it.next() );
			radio.setId( UIUtils.toCssId( option ) );

			vBox.getChildren().add( radio );
		}

		System.out.println( "selected " + slider.getSelected() );

		if( slider.getSelected() != null )
			selectToggle( toggleGroup, slider.getSelected() );

		slider.selectedProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String oldValue, String newValue )
			{
				selectToggle( toggleGroup, newValue );
			}
		} );

		toggleGroup.selectedToggleProperty().addListener( new ChangeListener<Toggle>()
		{
			@Override
			public void changed( ObservableValue<? extends Toggle> arg0, Toggle oldValue, Toggle newValue )
			{
				slider.setSelected( ( String )newValue.getUserData() );
			}
		} );

		getChildren().add( createLayout( vBox ) );

	}

	protected Node createLayout( VBox vBox )
	{
		AnchorPane anchorPane = new AnchorPane();

		Region r = RegionBuilder.create().styleClass( "sliding-area" ).build();

		AnchorPane.setTopAnchor( r, 0.0 );
		AnchorPane.setLeftAnchor( r, 0.0 );
		AnchorPane.setBottomAnchor( r, 0.0 );

		AnchorPane.setTopAnchor( vBox, 0.0 );
		AnchorPane.setRightAnchor( vBox, 0.0 );
		AnchorPane.setBottomAnchor( vBox, 0.0 );
		AnchorPane.setLeftAnchor( vBox, 0.0 );

		anchorPane.getChildren().setAll( r, vBox );
		return anchorPane;
	}

	private static void selectToggle( final ToggleGroup toggleGroup, String newValue )
	{
		boolean foundToggle = false;
		for( Toggle toggle : toggleGroup.getToggles() )
		{
			if( newValue.equals( toggle.getUserData() ) )
			{
				toggle.setSelected( true );
				foundToggle = true;
				break;
			}
		}
		if( !foundToggle )
			throw new IllegalArgumentException( "No toggle found having the user data: " + newValue );
	}
}