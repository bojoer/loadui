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
package com.eviware.loadui.groovy;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

import groovy.lang.Closure;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentContext.Scope;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;
import com.eviware.loadui.util.groovy.LayoutBuilder;

public class GroovyComponentContext implements Releasable
{
	private final ActionEventHandler actionEventHandler = new ActionEventHandler();
	private final CounterHelper counterHelper = new CounterHelper();

	private final ComponentContext context;
	private final Logger log;

	public GroovyComponentContext( ComponentContext context, Logger log )
	{
		this.context = context;
		this.log = log;

		context.addEventListener( ActionEvent.class, actionEventHandler );
	}

	@Override
	public synchronized void release()
	{
		reset();
		context.removeEventListener( ActionEvent.class, actionEventHandler );
	}

	public ComponentContext getContext()
	{
		return context;
	}

	public void layout( Closure<?> closure )
	{
		Map<String, ?> map = Collections.emptyMap();
		layout( map, closure );
	}

	public void layout( Map<String, ?> args, Closure<?> closure )
	{
		LayoutBuilder layoutBuilder = new LayoutBuilder( new LayoutContainerImpl( args ) );
		closure.setDelegate( layoutBuilder );
		closure.call();
		context.setLayout( layoutBuilder.build() );
	}

	public void compactLayout( Closure<?> closure )
	{
		Map<String, ?> map = Collections.emptyMap();
		compactLayout( map, closure );
	}

	public void compactLayout( Map<String, ?> args, Closure<?> closure )
	{
		LayoutBuilder layoutBuilder = new LayoutBuilder( new LayoutContainerImpl( args ) );
		closure.setDelegate( layoutBuilder );
		closure.call();
		context.setCompactLayout( layoutBuilder.build() );
	}

	public void settings( Closure<?> closure )
	{
		Map<String, ?> map = Collections.emptyMap();
		settings( map, closure );
	}

	public void settings( Map<String, ?> args, Closure<?> closure )
	{
		SettingsLayoutContainer layoutContainer = new SettingsLayoutContainerImpl( args );
		LayoutBuilder layoutBuilder = new LayoutBuilder( layoutContainer );
		closure.setDelegate( layoutBuilder );
		closure.call();
		context.addSettingsTab( ( SettingsLayoutContainer )layoutBuilder.build() );
	}

	public void triggerAction( String actionName, String scope )
	{
		context.triggerAction( actionName, Scope.valueOf( scope ) );
	}

	public void triggerAction( String actionName )
	{
		context.triggerAction( actionName, Scope.COMPONENT );
	}

	public void onAction( String action, Closure<?> handler )
	{
		actionEventHandler.actionHandlers.put( action, handler );
	}

	public CounterHelper getCounters()
	{
		return counterHelper;
	}

	void reset()
	{
		actionEventHandler.actionHandlers.clear();
		context.clearSettingsTabs();
	}

	public <T extends EventObject> EventHandler<T> addEventListener( EventFirer target, Class<T> type,
			final Closure<?> closure )
	{
		EventHandler<T> listener = new EventHandler<T>()
		{
			@Override
			public void handleEvent( T event )
			{
				closure.call( event );
			}
		};
		target.addEventListener( type, listener );
		return listener;
	}

	public <T extends EventObject> EventHandler<T> addEventListener( Class<T> type, final Closure<?> closure )
	{
		return addEventListener( context, type, closure );
	}

	public Value<?> value( Closure<?> closure )
	{
		return new ClosureValue( closure );
	}

	private class ClosureValue implements Value<Object>
	{
		private final Closure<?> closure;

		public ClosureValue( Closure<?> closure )
		{
			this.closure = closure;
		}

		@Override
		public Class<Object> getType()
		{
			return Object.class;
		}

		@Override
		public Object getValue()
		{
			try
			{
				return closure.call();
			}
			catch( Exception e )
			{
				log.error( "Error in value Closure:", e );
				return null;
			}
		}
	}

	private class CounterHelper implements Map<String, Counter>
	{
		@Override
		public int size()
		{
			return context.getCounterNames().size();
		}

		@Override
		public boolean isEmpty()
		{
			return context.getCounterNames().isEmpty();
		}

		@Override
		public boolean containsKey( Object key )
		{
			return context.getCounterNames().contains( key );
		}

		@Override
		public boolean containsValue( Object value )
		{
			return value instanceof Counter && context.getCounterNames().contains( ( ( Counter )value ).getValue() );
		}

		@Override
		public Counter get( Object key )
		{
			return context.getCounter( String.valueOf( key ) );
		}

		@Override
		public Counter put( String key, Counter value )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Counter remove( Object key )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll( Map<? extends String, ? extends Counter> m )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<String> keySet()
		{
			return new HashSet<String>( context.getCounterNames() );
		}

		@Override
		public Collection<Counter> values()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<java.util.Map.Entry<String, Counter>> entrySet()
		{
			throw new UnsupportedOperationException();
		}
	}

	private class ActionEventHandler implements WeakEventHandler<ActionEvent>
	{
		private final HashMap<String, Closure<?>> actionHandlers = new HashMap<String, Closure<?>>();

		@Override
		public void handleEvent( ActionEvent event )
		{
			Closure<?> handler = actionHandlers.get( event.getKey() );
			if( handler != null )
			{
				try
				{
					handler.call();
				}
				catch( Exception e )
				{
					log.error( "Exception caught when calling onAction handler for " + event.getKey(), e );
				}
			}
		}
	}
}