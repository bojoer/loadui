/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.events;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;

public class EventSupport implements EventFirer
{
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
	public <T extends EventObject> void addEventListener( final Class<T> type, final EventHandler<T> listener )
	{
		eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.add( new ListenerEntry<T>( type, listener ) );
			}
		} );
	}

	@Override
	public <T extends EventObject> void removeEventListener( final Class<T> type, final EventHandler<T> listener )
	{
		eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.remove( new ListenerEntry<T>( type, listener ) );
			}
		} );
	}

	@Override
	public void clearEventListeners()
	{
		eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				listeners.clear();
			}
		} );
	}

	public void fireEvent( final EventObject event )
	{
		eventQueue.offer( new Runnable()
		{
			@Override
			public void run()
			{
				for( ListenerEntry<?> listenerEntry : listeners )
				{
					if( listenerEntry.type.isInstance( event ) )
					{
						queueEvent( event, listenerEntry.listener, listenerEntry.type );
					}
				}
			}
		} );
	}

	@SuppressWarnings( "unchecked" )
	private <E extends EventObject> void queueEvent( EventObject event, EventHandler<?> handler, Class<E> type )
	{
		eventQueue.offer( new PendingEvent<E>( ( E )event, ( EventHandler<E> )handler ) );
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
		private final EventHandler<T> listener;

		private ListenerEntry( Class<T> type, EventHandler<T> listener )
		{
			this.type = type;
			this.listener = listener;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( listener == null ) ? 0 : listener.hashCode() );
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
