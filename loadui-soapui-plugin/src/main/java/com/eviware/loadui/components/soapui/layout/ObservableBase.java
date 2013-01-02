package com.eviware.loadui.components.soapui.layout;

import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import com.google.common.collect.Sets;

public class ObservableBase implements Observable
{
	private final Set<InvalidationListener> listeners = Sets.newCopyOnWriteArraySet();

	@Override
	public void addListener( InvalidationListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void removeListener( InvalidationListener listener )
	{
		listeners.remove( listener );
	}

	protected void fireInvalidation()
	{
		for( InvalidationListener listener : listeners )
		{
			listener.invalidated( this );
		}
	}
}
