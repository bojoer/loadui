package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;

import com.google.common.base.Preconditions;

public class ScrollList<E extends Node> extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "scroll-list";

	private final ObservableList<E> items = FXCollections.observableArrayList();
	private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>( this, "orientation",
			Orientation.VERTICAL );
	private final DoubleProperty sizePerItem = new SimpleDoubleProperty( this, "sizePerItem", 100.0 );

	public ScrollList()
	{
		initialize();
	}

	private void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public ObservableList<E> getItems()
	{
		return items;
	}

	public DoubleProperty sizePerItemProperty()
	{
		return sizePerItem;
	}

	public double getSizePerItem()
	{
		return sizePerItem.get();
	}

	public void setSizePerItem( double value )
	{
		Preconditions.checkArgument( value > 0, "sizePerItem must be >0, was %d", value );
		this.sizePerItem.set( value );
	}

	public Property<Orientation> orientationProperty()
	{
		return orientation;
	}

	public Orientation getOrientation()
	{
		return orientation.get();
	}

	public void setOrientation( Orientation value )
	{
		orientation.set( value );
	}
}