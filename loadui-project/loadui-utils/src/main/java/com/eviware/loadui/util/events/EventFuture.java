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
package com.eviware.loadui.util.events;

import java.util.EventObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.google.common.base.Objects;
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
	private final CountDownLatch eventLatch = new CountDownLatch( 1 );
	private volatile T matchedEvent = null;

	/**
	 * Creates an EventFuture for the given EventFirer, matching events of the
	 * given event type with the given key.
	 * 
	 * @param eventFirer
	 * @param eventType
	 * @param key
	 * @return
	 */
	public static <V extends BaseEvent> EventFuture<V> forKey( EventFirer eventFirer, Class<V> eventType,
			final String key )
	{
		return new EventFuture<>( eventFirer, eventType, new Predicate<V>()
		{
			@Override
			public boolean apply( V input )
			{
				return Objects.equal( key, input.getKey() );
			}
		} );
	}

	/**
	 * Creates an EventFuture for the given EventFirer, matching events of
	 * BaseEvent with the given Key.
	 * 
	 * @param eventFirer
	 * @param key
	 * @return
	 */
	public static EventFuture<BaseEvent> forKey( EventFirer eventFirer, final String key )
	{
		return forKey( eventFirer, BaseEvent.class, key );
	}

	/**
	 * Create a new EventFuture listening for eventType events on eventFirer,
	 * which satisfy the given Predicate.
	 * 
	 * @param eventFirer
	 * @param eventType
	 * @param predicate
	 */
	public EventFuture( EventFirer eventFirer, Class<T> eventType, Predicate<T> predicate )
	{
		listener = new PredicateListener( eventFirer, eventType, predicate );
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		if( isDone() || !mayInterruptIfRunning )
			return false;

		listener.cancel();
		return true;
	}

	@Override
	public boolean isCancelled()
	{
		return isDone() && matchedEvent == null;
	}

	@Override
	public boolean isDone()
	{
		return eventLatch.getCount() == 0;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		eventLatch.await();

		return matchedEvent;
	}

	@Override
	public T get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		if( !eventLatch.await( timeout, unit ) )
		{
			throw new TimeoutException();
		}

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
				matchedEvent = event;
				eventLatch.countDown();
			}
		}

		private void cancel()
		{
			eventFirer.removeEventListener( eventType, this );
			eventLatch.countDown();
		}
	}
}
