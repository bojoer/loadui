/*
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.events;

import java.util.EventObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.google.common.base.Predicate;

/**
 * Utility class for awaiting a specific Event matching a given Predicate in a
 * blocking fashion, through the standard Future interface.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public class EventFuture<T extends EventObject> implements Future<T>
{
	private final PredicateListener listener;
	private T matchedEvent = null;
	private boolean done = false;

	public EventFuture( EventFirer eventFirer, Class<T> eventType, Predicate<T> predicate )
	{
		listener = new PredicateListener( eventFirer, eventType, predicate );
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		if( done || !mayInterruptIfRunning )
			return false;

		listener.cancel();
		return true;
	}

	@Override
	public boolean isCancelled()
	{
		return done && matchedEvent == null;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		synchronized( listener )
		{
			while( matchedEvent == null )
			{
				listener.wait();
			}
		}

		return matchedEvent;
	}

	@Override
	public T get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		synchronized( listener )
		{
			if( matchedEvent == null )
			{
				listener.wait( unit.toMillis( timeout ) );
			}
		}

		if( matchedEvent == null )
			throw new TimeoutException();

		return matchedEvent;
	}

	private class PredicateListener implements EventHandler<T>
	{
		private final EventFirer eventFirer;
		private final Class<T> eventType;
		private final Predicate<T> predicate;

		public PredicateListener( EventFirer eventFirer, Class<T> eventType, Predicate<T> predicate )
		{
			this.eventFirer = eventFirer;
			this.eventType = eventType;
			this.predicate = predicate;

			eventFirer.addEventListener( eventType, this );
		}

		@Override
		public void handleEvent( T event )
		{
			if( predicate.apply( event ) )
			{
				eventFirer.removeEventListener( eventType, this );
				synchronized( this )
				{
					matchedEvent = event;
					notifyAll();
				}
			}
		}

		private void cancel()
		{
			synchronized( listener )
			{
				done = true;
				eventFirer.removeEventListener( eventType, this );
				notifyAll();
			}
		}
	}
}
