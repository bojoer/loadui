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
