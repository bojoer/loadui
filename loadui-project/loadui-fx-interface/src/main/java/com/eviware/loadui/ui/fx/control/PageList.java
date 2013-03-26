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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.util.Callback;

import com.google.common.base.Preconditions;

public class PageList<E extends Node> extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "page-list";

	private final ObservableList<E> items = FXCollections.observableArrayList();
	private final Label label;

	public PageList()
	{
		this.label = new Label();
		initialize();
	}

	public PageList( String label )
	{
		this.label = new Label( label );
		initialize();
	}

	public PageList( String label, Node graphic )
	{
		this.label = new Label( label, graphic );
		initialize();
	}

	private void initialize()
	{
		label.getStyleClass().add( "title" );
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public StringProperty textProperty()
	{
		return getLabel().textProperty();
	}

	public String getText()
	{
		return getLabel().getText();
	}

	public void setText( String text )
	{
		getLabel().setText( text );
	}

	public Node getGraphic()
	{
		return getLabel().getGraphic();
	}

	public void setGraphic( Node graphic )
	{
		getLabel().setGraphic( graphic );
	}

	public Label getLabel()
	{
		return label;
	}

	public ObservableList<E> getItems()
	{
		return items;
	}

	private final StringProperty placeholderText = new SimpleStringProperty( this, "placeholderText", "No items" );

	public StringProperty placeholderTextProperty()
	{
		return placeholderText;
	}

	public String getPlaceholderText()
	{
		return placeholderText.get();
	}

	public void setPlaceholderText( String value )
	{
		placeholderText.set( value );
	}

	private final DoubleProperty widthPerItem = new SimpleDoubleProperty( this, "widthPerItem", 100.0 );

	public DoubleProperty widthPerItemProperty()
	{
		return widthPerItem;
	}

	public double getWidthPerItem()
	{
		return widthPerItem.get();
	}

	public void setWidthPerItem( double widthPerItem )
	{
		Preconditions.checkArgument( widthPerItem > 0, "widthPerItem must be >0, was %d", widthPerItem );
		this.widthPerItem.set( widthPerItem );
	}

	private final DoubleProperty spacing = new SimpleDoubleProperty( this, "spacing", 0.0 );

	public DoubleProperty spacingProperty()
	{
		return spacing;
	}

	public double getSpacing()
	{
		return spacing.get();
	}

	public void setSpacing( double value )
	{
		Preconditions.checkArgument( value >= 0, "spacing must be >=0, was %d", value );
		this.spacing.set( value );
	}

	private final Property<Callback<? super E, ? extends Label>> labelFactory = new SimpleObjectProperty<>( this,
			"labelFactory" );

	public Property<Callback<? super E, ? extends Label>> labelFactoryProperty()
	{
		return labelFactory;
	}

	public void setLabelFactory( Callback<? super E, ? extends Label> value )
	{
		labelFactory.setValue( value );
	}

	public Callback<? super E, ? extends Label> getLabelFactory()
	{
		return labelFactory.getValue();
	}
}
