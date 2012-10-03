package com.eviware.loadui.ui.fx.control;

import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
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

	private void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );

		items.addListener( new ListChangeListener<E>()
		{
			@Override
			public void onChanged( ListChangeListener.Change<? extends E> change )
			{
				final E selected = getSelected();
				if( items.contains( selected ) )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							setSelected( null );
							if( items.contains( selected ) )
							{
								setSelected( selected );
							}
						}
					} );
				}
				else if( items.isEmpty() )
				{
					setSelected( null );
				}
				else
				{
					if( selected == null )
					{
						setSelected( items.get( 0 ) );
					}
					else
					{
						while( change.next() )
						{
							if( change.getRemoved().contains( selected ) )
							{
								setSelected( items.get( Math.max( 0, change.getFrom() - 1 ) ) );
								return;
							}
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
}
