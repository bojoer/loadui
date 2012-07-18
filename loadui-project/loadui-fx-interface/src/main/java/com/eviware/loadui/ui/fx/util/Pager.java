package com.eviware.loadui.ui.fx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
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
		Preconditions.checkPositionIndex( page, getNumPages() );
		this.page.set( page );
	}

	private final ObservableList<T> items = FXCollections.observableArrayList();

	public ObservableList<T> getItems()
	{
		return items;
	}

	private final ReadOnlyIntegerWrapper numPages = new ReadOnlyIntegerWrapper( this, "numPages" )
	{
		{
			//TODO: When fluentMode is off, this is wrong due to integer rounding down.
			bind( Bindings.when( fluentMode )
					.then( Bindings.max( Bindings.size( items ).subtract( itemsPerPage ), 0 ).add( 1 ) )
					.otherwise( Bindings.size( items ).divide( itemsPerPage ) ) );
		}
	};

	public int getNumPages()
	{
		return numPages.intValue();
	}

	public ReadOnlyIntegerProperty numPagesProperty()
	{
		return numPages.getReadOnlyProperty();
	}

	private final ReadOnlyIntegerWrapper offset = new ReadOnlyIntegerWrapper( this, "offset" )
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

	public Pager()
	{
		InvalidationListener shownItemsListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				shownItems.setAll( items.subList( offset.intValue(),
						offset.intValue() + Math.min( itemsPerPage.get(), items.size() - offset.intValue() ) ) );
			}
		};

		items.addListener( shownItemsListener );
		offset.addListener( shownItemsListener );
		itemsPerPage.addListener( shownItemsListener );
	}

	public Pager( int itemsPerPage )
	{
		this();
		setItemsPerPage( itemsPerPage );
	}
}
