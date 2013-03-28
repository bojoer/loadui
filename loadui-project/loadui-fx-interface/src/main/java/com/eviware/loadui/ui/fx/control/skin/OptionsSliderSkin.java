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
package com.eviware.loadui.ui.fx.control.skin;

import java.util.Iterator;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import com.google.common.collect.ImmutableList;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class OptionsSliderSkin extends SkinBase<OptionsSlider, BehaviorBase<OptionsSlider>>
{
	protected static final Logger log = LoggerFactory.getLogger( OptionsSliderSkin.class );
	private final ToggleGroup toggleGroup = new ToggleGroup();
	private final ObservableList<RadioButton> radioButtons = FXCollections.observableArrayList();

	public OptionsSliderSkin( final OptionsSlider slider )
	{
		super( slider, new BehaviorBase<>( slider ) );

		VBox vBox = VBoxBuilder.create().styleClass( "container" ).build();
		Bindings.bindContent( vBox.getChildren(), radioButtons );

		InvalidationListener recreateRadioButtons = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				createRadioButtons();
			}
		};

		slider.getOptions().addListener( recreateRadioButtons );
		slider.getImages().addListener( recreateRadioButtons );
		slider.showLabelsProperty().addListener( recreateRadioButtons );
		createRadioButtons();

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

	private void createRadioButtons()
	{
		ImmutableList.Builder<RadioButton> newRadioButtons = ImmutableList.builder();
		Iterator<ImageView> it = getSkinnable().getImages().iterator();
		for( String option : getSkinnable().getOptions() )
		{
			RadioButton radio = RadioButtonBuilder.create().toggleGroup( toggleGroup ).build();
			radio.setUserData( option );

			if( getSkinnable().isShowLabels() )
			{
				radio.setText( option );
			}
			if( it.hasNext() )
			{
				radio.setGraphic( it.next() );
			}
			radio.setId( UIUtils.toCssId( option ) );

			newRadioButtons.add( radio );
		}

		radioButtons.setAll( newRadioButtons.build() );
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
