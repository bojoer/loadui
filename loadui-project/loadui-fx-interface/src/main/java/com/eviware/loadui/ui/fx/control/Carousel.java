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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import com.google.common.base.Preconditions;

@DefaultProperty( "items" )
public class Carousel<E extends Node> extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "carousel";

	private final ObservableList<E> items = FXCollections.observableArrayList();
	private final Label label;

	private final ObjectProperty<StringConverter<E>> converterProperty = new SimpleObjectProperty<>( this, "converter" );

	private final ObjectProperty<E> selectedProperty = new ObjectPropertyBase<E>()
	{
		@Override
		public Object getBean()
		{
			return Carousel.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

	public Carousel()
	{
		this.label = new Label();
		initialize();
	}

	public Carousel( String label )
	{
		this.label = new Label( label );
		initialize();
	}

	public Carousel( String label, Node graphic )
	{
		this.label = new Label( label, graphic );
		initialize();
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

	private void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );

		label.getStyleClass().add( "list-title" );

		items.addListener( new ListChangeListener<E>()
		{
			@Override
			public void onChanged( ListChangeListener.Change<? extends E> change )
			{
				final E selected = getSelected();

				if( items.isEmpty() )
				{
					setSelected( null );
					return;
				}

				while( change.next() )
				{

					if( change.wasAdded() )
					{
						setSelected( change.getAddedSubList().get( 0 ) );
					}
					else if( !items.contains( selected ) )
					{
						System.out.print( "*" );
						if( change.getRemoved().contains( selected ) )
						{
							setSelected( items.get( Math.max( 0, change.getFrom() - 1 ) ) );
							return;
						}
					}
				}

			}
		} );
	}

	public ObjectProperty<StringConverter<E>> converterProperty()
	{
		return converterProperty;
	}

	public StringConverter<E> getConverter()
	{
		return converterProperty.get();
	}

	public void setConverter( StringConverter<E> converter )
	{
		converterProperty.set( converter );
	}

	public ObjectProperty<E> selectedProperty()
	{
		return selectedProperty;
	}

	public E getSelected()
	{
		return selectedProperty.get();
	}

	public void setSelected( E selected )
	{
		Preconditions.checkArgument( selected == null || items.contains( selected ),
				"%s does not contain the given object: %s", Carousel.class.getSimpleName(), selected );

		selectedProperty.set( selected );
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
}
