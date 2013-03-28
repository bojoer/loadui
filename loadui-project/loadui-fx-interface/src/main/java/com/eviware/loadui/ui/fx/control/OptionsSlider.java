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
package com.eviware.loadui.ui.fx.control;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@DefaultProperty( "options" )
public class OptionsSlider extends Control
{
	protected static final Logger log = LoggerFactory.getLogger( OptionsSlider.class );

	private static final String DEFAULT_STYLE_CLASS = "options-slider";

	private final ObservableList<String> options = FXCollections.observableArrayList();
	private final ObservableList<ImageView> images = FXCollections.observableArrayList();
	private final BooleanProperty showLabels = new SimpleBooleanProperty( this, "showLabels", true );

	private final ObjectProperty<String> selectedProperty = new ObjectPropertyBase<String>()
	{
		@Override
		public Object getBean()
		{
			return OptionsSlider.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

	public OptionsSlider()
	{
		initialize();
	}

	public OptionsSlider( Iterable<String> options )
	{
		getOptions().setAll( Lists.newArrayList( options ) );
		initialize();
	}

	public OptionsSlider( Iterable<String> options, Iterable<ImageView> images )
	{
		getOptions().setAll( Lists.newArrayList( options ) );
		getImages().setAll( Lists.newArrayList( images ) );
		initialize();
	}

	public OptionsSlider( Iterable<String> options, Iterable<ImageView> images, boolean showLabels )
	{
		getOptions().setAll( Lists.newArrayList( options ) );
		getImages().setAll( Lists.newArrayList( images ) );
		this.showLabels.set( showLabels );
		initialize();
	}

	private void initialize()
	{
		getStylesheets().add( OptionsSlider.class.getResource( "OptionsSlider.bss" ).toExternalForm() );
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public BooleanProperty showLabelsProperty()
	{
		return showLabels;
	}

	public boolean isShowLabels()
	{
		return showLabels.get();
	}

	public void setShowLabels( boolean value )
	{
		showLabels.set( value );
	}

	public ObservableList<String> getOptions()
	{
		return options;
	}

	public ObservableList<ImageView> getImages()
	{
		return images;
	}

	public ObjectProperty<String> selectedProperty()
	{
		return selectedProperty;
	}

	public void setSelected( String value )
	{
		log.debug( "newValue test 1: " + value );
		selectedProperty.setValue( value );
	}

	public String getSelected()
	{
		return selectedProperty.getValue();
	}
}
