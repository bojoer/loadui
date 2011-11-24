/*
 * Copyright 2011 SmartBear Software
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

import java.lang.ref.WeakReference;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.traits.Releasable;

public class EventSupport implements EventFirer, Releasable
{
	private static final Logger log = LoggerFactory.getLogger( EventSupport.class );

	private final Set<ListenerEntry<?>> listeners = new HashSet<ListenerEntry<?>>();
	private static BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>();
	private static Thread eventThread = new Thread( new Runnable()
	{
		@Override
		public void run()
		{
			while( true )
			{
				try
				{
					eventQueue.take().run();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

		}
	}, "loadUI Event Thread" );

	static
	{
		eventThread.setDaemon( true );
		eventThread.start();
	}

	@Override
	public <T extends EventObject> void addEventListener( final Class<T> type, final EventHandler<? super T> listener )
	{
		if( listener == null )
			throw new NullPointerException( "Cannot add null EventHandler!" );

		if( !eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.add( new ListenerEntry<T>( type, listener ) );
			}
		} ) )
			log.error( "Event queue full! Unable to add event listener: {}", listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( final Class<T> type, final EventHandler<? super T> listener )
	{
		if( !eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.remove( new ListenerEntry<T>( type, listener ) );
			}
		} ) )
			log.error( "Event queue full! Unable to remove event listener: {}", listener );
	}

	@Override
	public void clearEventListeners()
	{
		if( !eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.clear();
			}
		} ) )
			log.error( "Event queue full! Unable to clear event listeners!" );
	}

	@Override
	public void release()
	{
		clearEventListeners();
	}

	public void fireEvent( final EventObject event )
	{
		if( !eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				for( ListenerEntry<?> listenerEntry : new HashSet<ListenerEntry<?>>( listeners ) )
				{
					if( listenerEntry.type.isInstance( event ) )
					{
						if( listenerEntry.listener != null )
						{
							queueEvent( event, listenerEntry.listener, listenerEntry.type );
						}
						else
						{
							EventHandler<?> listener = listenerEntry.weakListener.get();
							if( listener != null )
							{
								queueEvent( event, listener, listenerEntry.type );
							}
							else
							{
								listeners.remove( listenerEntry );
							}
						}
					}
				}
			}
		} ) )
			log.error( "Event queue full! Unable to fire event: {}", event );
	}

	@SuppressWarnings( "unchecked" )
	private <E extends EventObject> void queueEvent( EventObject event, EventHandler<?> handler, Class<E> type )
	{
		if( !eventQueue.offer( new PendingEvent<E>( ( E )event, ( EventHandler<E> )handler ) ) )
			log.error( "Event queue full! Unable to queue event: {}", event );
	}

	private static class PendingEvent<E extends EventObject> implements Runnable
	{
		private final E event;
		private final EventHandler<E> handler;

		private PendingEvent( E event, EventHandler<E> handler )
		{
			this.event = event;
			this.handler = handler;
		}

		public void run()
		{
			handler.handleEvent( event );
		}
	}

	private static class ListenerEntry<T extends EventObject>
	{
		private final Class<T> type;
		private final EventHandler<?> listener;
		private final WeakReference<EventHandler<?>> weakListener;

		private ListenerEntry( Class<T> type, EventHandler<? super T> listener )
		{
			this.type = type;

			if( listener instanceof WeakEventHandler )
			{
				this.listener = null;
				this.weakListener = new WeakReference<EventHandler<?>>( listener );
			}
			else
			{
				this.listener = listener;
				this.weakListener = null;
			}
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( listener == null ) ? 0 : listener.hashCode() );
			result = prime * result + ( ( weakListener == null ) ? 0 : weakListener.hashCode() );
			result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
			return result;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			ListenerEntry<?> other = ( ListenerEntry<?> )obj;
			if( listener == null )
			{
				if( other.listener != null )
					return false;
			}
			else if( !listener.equals( other.listener ) )
				return false;
			if( weakListener == null )
			{
				if( other.weakListener != null )
					return false;
			}
			else if( !weakListener.equals( other.weakListener ) )
				return false;
			if( type == null )
			{
				if( other.type != null )
					return false;
			}
			else if( !type.equals( other.type ) )
				return false;
			return true;
		}
	}
}
