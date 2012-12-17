package com.eviware.loadui.ui.fx.util;

import java.util.Comparator;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.common.collect.Ordering;

/**
 * Support class for creating Explicit orderings in FXML.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
@DefaultProperty( "order" )
public class ExplicitOrdering<T> implements Comparator<T>
{
	private Ordering<? super T> ordering = Ordering.usingToString();
	private final ObservableList<T> elementsInOrder = FXCollections.observableArrayList();

	public ExplicitOrdering()
	{
		elementsInOrder.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				ordering = Ordering.explicit( elementsInOrder );
			}
		} );
	}

	public ObservableList<T> getOrder()
	{
		return elementsInOrder;
	}

	@Override
	public int compare( T o1, T o2 )
	{
		return ordering.compare( o1, o2 );
	}
}
