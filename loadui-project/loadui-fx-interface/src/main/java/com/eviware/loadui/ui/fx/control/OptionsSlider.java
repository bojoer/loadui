package com.eviware.loadui.ui.fx.control;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class OptionsSlider extends Control
{
	protected static final Logger log = LoggerFactory.getLogger( OptionsSlider.class );

	private static final String DEFAULT_STYLE_CLASS = "options-slider";

	private final Iterable<String> options;
	private final Iterable<ImageView> images;
	private final boolean showLabels;

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

	public OptionsSlider( Iterable<String> options )
	{
		this( options, Iterables.cycle( ( ImageView )null ) );
	}

	public OptionsSlider( Iterable<String> options, Iterable<ImageView> images )
	{
		this( options, images, true );
	}

	public OptionsSlider( Iterable<String> options, Iterable<ImageView> images, boolean showLabels )
	{
		this.options = options;
		this.images = images;
		this.showLabels = showLabels;
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public boolean showLabels()
	{
		return showLabels;
	}

	public List<String> getOptions()
	{
		return ImmutableList.copyOf( options );
	}

	public Iterable<ImageView> getImages()
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
