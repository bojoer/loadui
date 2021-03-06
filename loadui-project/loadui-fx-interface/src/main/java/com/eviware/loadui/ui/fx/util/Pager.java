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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.common.base.Preconditions;

public class Pager<T>
{
	private final IntegerProperty itemsPerPage = new SimpleIntegerProperty( this, "itemsPerPage", 1 );

	public IntegerProperty itemsPerPageProperty()
	{
		return itemsPerPage;
	}

	public int getItemsPerPage()
	{
		return itemsPerPage.get();
	}

	public void setItemsPerPage( int itemsPerPage )
	{
		Preconditions.checkArgument( itemsPerPage > 0, "itemsPerPage must be >0, was %d", itemsPerPage );
		this.itemsPerPage.set( itemsPerPage );
	}

	private final BooleanProperty fluentMode = new SimpleBooleanProperty( this, "fluentMode", false );

	public BooleanProperty fluentModeProperty()
	{
		return fluentMode;
	}

	public boolean isFluentMode()
	{
		return fluentMode.get();
	}

	public void setFluentMode( boolean fluentMode )
	{
		this.fluentMode.set( fluentMode );
	}

	private final IntegerProperty page = new SimpleIntegerProperty( this, "page", 0 );

	public IntegerProperty pageProperty()
	{
		return page;
	}

	public int getPage()
	{
		return page.get();
	}

	public void setPage( int page )
	{
		Preconditions.checkPositionIndex( page, getNumPages() - 1 );
		this.page.set( page );
	}

	private final ObservableList<T> items;

	public ObservableList<T> getItems()
	{
		return items;
	}

	private final ReadOnlyIntegerWrapper numPages;

	public int getNumPages()
	{
		return numPages.intValue();
	}

	public ReadOnlyIntegerProperty numPagesProperty()
	{
		return numPages.getReadOnlyProperty();
	}

	private final ReadOnlyIntegerWrapper offset = new ReadOnlyIntegerWrapper( this, "offset", 0 )
	{
		{
			bind( Bindings.when( fluentMode ).then( page ).otherwise( page.multiply( itemsPerPage ) ) );
		}
	};

	public int getOffset()
	{
		return offset.intValue();
	}

	public ReadOnlyIntegerProperty offsetProperty()
	{
		return offset.getReadOnlyProperty();
	}

	private final ObservableList<T> shownItems = FXCollections.observableArrayList();

	public ObservableList<T> getShownItems()
	{
		return FXCollections.unmodifiableObservableList( shownItems );
	}

	public Pager( ObservableList<T> providedList )
	{
		items = providedList;

		InvalidationListener shownItemsListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				shownItems.setAll( items.subList( offset.intValue(),
						offset.intValue() + Math.min( itemsPerPage.get(), items.size() - offset.intValue() ) ) );
			}
		};

		numPages = new ReadOnlyIntegerWrapper( this, "numPages" )
		{
			{
				//TODO: When fluentMode is off, this is wrong due to integer rounding down.
				bind( Bindings.when( fluentMode )
						.then( Bindings.max( Bindings.size( items ).subtract( itemsPerPage ), 0 ).add( 1 ) )
						.otherwise( Bindings.size( items ).divide( itemsPerPage ) ) );
			}
		};

		items.addListener( shownItemsListener );
		offset.addListener( shownItemsListener );
		itemsPerPage.addListener( shownItemsListener );

		numPages.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( page.get() >= numPages.get() )
				{
					page.set( numPages.get() - 1 );
				}
			}
		} );
	}

	public BooleanExpression hasPrevProperty()
	{
		return page.greaterThan( 0 );
	}

	public boolean hasPrev()
	{
		return hasPrevProperty().getValue();
	}

	public BooleanExpression hasNextProperty()
	{
		return page.lessThan( numPages.subtract( 1 ) );
	}

	public boolean hasNext()
	{
		return hasNextProperty().getValue();
	}

	public void nextPage()
	{
		if( hasNext() )
		{
			setPage( getPage() + 1 );
		}
	}

	public void prevPage()
	{
		if( hasPrev() )
		{
			setPage( getPage() - 1 );
		}
	}

	public Pager( ObservableList<T> providedList, int itemsPerPage )
	{
		this( providedList );
		setItemsPerPage( itemsPerPage );
	}

	public Pager( int itemsPerPage )
	{
		this( FXCollections.<T> observableArrayList(), itemsPerPage );
	}

	public Pager()
	{
		this( FXCollections.<T> observableArrayList() );
	}
}
