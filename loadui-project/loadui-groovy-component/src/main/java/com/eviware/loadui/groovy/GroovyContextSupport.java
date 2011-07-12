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
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.eviware.loadui.api.component.ActivityStrategy;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.terminal.DualTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;

import groovy.lang.Closure;

public class GroovyContextSupport implements ComponentContext, Releasable
{
	private final PropertyEventHandler propertyEventHandler = new PropertyEventHandler();
	private final ActionEventHandler actionEventHandler = new ActionEventHandler();
	private final CounterHelper counterHelper = new CounterHelper();

	private final ComponentContext context;
	private final Logger log;

	private ScheduledExecutorService executor = null;
	private final HashSet<Future<?>> futures = new HashSet<Future<?>>();

	public GroovyContextSupport( ComponentContext context, Logger log )
	{
		this.context = context;
		this.log = log;

		context.addEventListener( PropertyEvent.class, propertyEventHandler );
		context.addEventListener( ActionEvent.class, actionEventHandler );
	}

	@Override
	public synchronized void release()
	{
		reset();
		clearEventListeners();
		context.removeEventListener( PropertyEvent.class, propertyEventHandler );
		context.removeEventListener( ActionEvent.class, actionEventHandler );
		if( executor != null )
			executor.shutdownNow();
		futures.clear();
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

	@Override
	public void triggerAction( String actionName, Scope scope )
	{
		context.triggerAction( actionName, scope );
	}

	public <T> Property<T> onReplace( Property<T> property, Closure<?> handler )
	{
		propertyEventHandler.replaceHandlers.put( property, handler );
		return property;
	}

	public void onAction( String action, Closure<?> handler )
	{
		actionEventHandler.actionHandlers.put( action, handler );
	}

	public InputTerminal likes( final InputTerminal input, final Closure<Boolean> handler )
	{
		context.setLikeFunction( input, new LikeFunction()
		{
			@Override
			public boolean call( OutputTerminal output )
			{
				try
				{
					return handler.call( output );
				}
				catch( Exception e )
				{
					log.error( "Exception caught when calling like function for " + input, e );
					return false;
				}
			}
		} );

		return input;
	}

	public CounterHelper getCounters()
	{
		return counterHelper;
	}

	void reset()
	{
		propertyEventHandler.replaceHandlers.clear();
		actionEventHandler.actionHandlers.clear();
		clearSettingsTabs();
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

	/**
	 * Invokes all registered onReplace handlers with the current property value.
	 */
	public void invokeReplaceHandlers()
	{
		for( Property<?> property : new HashSet<Property<?>>( propertyEventHandler.replaceHandlers.keySet() ) )
			propertyEventHandler.handleEvent( new PropertyEvent( property.getOwner(), property, PropertyEvent.Event.VALUE,
					null ) );
	}

	private synchronized ScheduledExecutorService getExecutor()
	{
		if( executor == null )
			executor = Executors.newSingleThreadScheduledExecutor();

		return executor;
	}

	private synchronized <V, T extends Future<V>> T addFuture( T future )
	{
		futures.add( future );
		return future;
	}

	public void cancelTasks()
	{
		synchronized( futures )
		{
			for( Future<?> future : futures )
			{
				if( !future.isDone() )
					future.cancel( true );
			}
			futures.clear();
		}
	}

	public Future<?> submit( Runnable runnable )
	{
		return addFuture( getExecutor().submit( runnable ) );
	}

	public <T> Future<T> submit( Runnable runnable, T result )
	{
		return addFuture( getExecutor().submit( runnable, result ) );
	}

	public <T> Future<T> submit( Callable<T> callable )
	{
		return addFuture( getExecutor().submit( callable ) );
	}

	public <V> ScheduledFuture<V> schedule( Callable<V> callable, long delay, TimeUnit unit )
	{
		return addFuture( getExecutor().schedule( callable, delay, unit ) );
	}

	public ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
	{
		return addFuture( getExecutor().schedule( command, delay, unit ) );
	}

	public ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
	{
		return addFuture( getExecutor().scheduleAtFixedRate( command, initialDelay, period, unit ) );
	}

	public ScheduledFuture<?> scheduleWithFixedDelay( Runnable command, long initialDelay, long delay, TimeUnit unit )
	{
		return addFuture( getExecutor().scheduleWithFixedDelay( command, initialDelay, delay, unit ) );
	}

	@Override
	public InputTerminal createInput( String name )
	{
		return context.createInput( name );
	}

	public InputTerminal createInput( String name, Closure<Boolean> likeFunction )
	{
		return likes( context.createInput( name ), likeFunction );
	}

	@Override
	public InputTerminal createInput( String name, String label )
	{
		return context.createInput( name, label );
	}

	public InputTerminal createInput( String name, String label, Closure<Boolean> likeFunction )
	{
		return likes( context.createInput( name, label ), likeFunction );
	}

	@Override
	public InputTerminal createInput( String name, String label, String description )
	{
		return context.createInput( name, label, description );
	}

	public InputTerminal createInput( String name, String label, String description, Closure<Boolean> likeFunction )
	{
		return likes( context.createInput( name, label, description ), likeFunction );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		context.addEventListener( type, listener );
	}

	@Override
	public Property<?> getProperty( String propertyName )
	{
		return context.getProperty( propertyName );
	}

	@Override
	public Counter getCounter( String counterName )
	{
		return context.getCounter( counterName );
	}

	@Override
	public Collection<String> getCounterNames()
	{
		return context.getCounterNames();
	}

	@Override
	public Collection<Terminal> getTerminals()
	{
		return context.getTerminals();
	}

	@Override
	public String getLabel()
	{
		return context.getLabel();
	}

	@Override
	public Collection<Property<?>> getProperties()
	{
		return context.getProperties();
	}

	@Override
	public Terminal getTerminalByName( String name )
	{
		return context.getTerminalByName( name );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		context.removeEventListener( type, listener );
	}

	@Override
	public OutputTerminal createOutput( String name )
	{
		return context.createOutput( name );
	}

	@Override
	public OutputTerminal createOutput( String name, String label )
	{
		return context.createOutput( name, label );
	}

	@Override
	public OutputTerminal createOutput( String name, String label, String description )
	{
		return context.createOutput( name, label, description );
	}

	@Override
	public void setLabel( String label )
	{
		context.setLabel( label );
	}

	@Override
	public void renameProperty( String oldName, String newName )
	{
		context.renameProperty( oldName, newName );
	}

	@Override
	public void handleTerminalEvent( InputTerminal input, TerminalEvent event )
	{
		context.handleTerminalEvent( input, event );
	}

	@Override
	public void clearEventListeners()
	{
		context.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		context.fireEvent( event );
	}

	@Override
	public void deleteTerminal( Terminal terminal )
	{
		context.deleteTerminal( terminal );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType )
	{
		return context.createProperty( propertyName, propertyType );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue )
	{
		if( initialValue instanceof Closure )
			return onReplace( context.createProperty( propertyName, propertyType ), ( Closure<?> )initialValue );

		return context.createProperty( propertyName, propertyType, initialValue );
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			boolean propagates )
	{
		return context.createProperty( propertyName, propertyType, initialValue, propagates );
	}

	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			Closure<?> handler )
	{
		return onReplace( context.createProperty( propertyName, propertyType, initialValue ), handler );
	}

	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			boolean propagates, Closure<?> handler )
	{
		return onReplace( context.createProperty( propertyName, propertyType, initialValue, propagates ), handler );
	}

	@Override
	public void setAttribute( String key, String value )
	{
		context.setAttribute( key, value );
	}

	@Override
	public void deleteProperty( String propertyName )
	{
		context.deleteProperty( propertyName );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return context.getAttribute( key, defaultValue );
	}

	@Override
	public CanvasItem getCanvas()
	{
		return context.getCanvas();
	}

	@Override
	public ComponentItem getComponent()
	{
		return context.getComponent();
	}

	@Override
	public void setLikeFunction( InputTerminal terminal, LikeFunction likeFunction )
	{
		context.setLikeFunction( terminal, likeFunction );
	}

	@Override
	public void setSignature( OutputTerminal terminal, Map<String, Class<?>> signature )
	{
		context.setSignature( terminal, signature );
	}

	@Override
	public void send( OutputTerminal terminal, TerminalMessage message )
	{
		context.send( terminal, message );
	}

	@Override
	public DualTerminal getRemoteTerminal()
	{
		return context.getRemoteTerminal();
	}

	@Override
	public OutputTerminal getControllerTerminal()
	{
		return context.getControllerTerminal();
	}

	@Override
	public Collection<DualTerminal> getAgentTerminals()
	{
		return context.getAgentTerminals();
	}

	@Override
	public TerminalMessage newMessage()
	{
		return context.newMessage();
	}

	@Override
	public void setCategory( String category )
	{
		context.setCategory( category );
	}

	@Override
	public String getCategory()
	{
		return context.getCategory();
	}

	@Override
	public String getId()
	{
		return context.getId();
	}

	@Override
	public void setLayout( LayoutComponent layout )
	{
		context.setLayout( layout );
	}

	@Override
	public void refreshLayout()
	{
		context.refreshLayout();
	}

	@Override
	public void setCompactLayout( LayoutComponent layout )
	{
		context.setCompactLayout( layout );
	}

	@Override
	public void setNonBlocking( boolean nonBlocking )
	{
		context.setNonBlocking( nonBlocking );
	}

	@Override
	public void setHelpUrl( String helpUrl )
	{
		context.setHelpUrl( helpUrl );
	}

	@Override
	public void addSettingsTab( SettingsLayoutContainer tab )
	{
		context.addSettingsTab( tab );
	}

	@Override
	public void clearSettingsTabs()
	{
		context.clearSettingsTabs();
	}

	@Override
	public boolean isRunning()
	{
		return context.isRunning();
	}

	@Override
	public boolean isInvalid()
	{
		return context.isInvalid();
	}

	@Override
	public void setInvalid( boolean state )
	{
		context.setInvalid( state );
	}

	@Override
	public boolean isBusy()
	{
		return context.isBusy();
	}

	@Override
	public void setBusy( boolean state )
	{
		context.setBusy( state );
	}

	@Override
	public void setActivityStrategy( ActivityStrategy strategy )
	{
		context.setActivityStrategy( strategy );
	}

	@Override
	public boolean isController()
	{
		return context.isController();
	}

	@Override
	public StatisticVariable.Mutable addStatisticVariable( String statisticVariableName, String... writerTypes )
	{
		return context.addStatisticVariable( statisticVariableName, writerTypes );
	}

	@Override
	public void removeStatisticVariable( String statisticVariableName )
	{
		context.removeStatisticVariable( statisticVariableName );
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

	private class PropertyEventHandler implements WeakEventHandler<PropertyEvent>
	{
		private final HashMap<Property<?>, Closure<?>> replaceHandlers = new HashMap<Property<?>, Closure<?>>();

		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( PropertyEvent.Event.VALUE == event.getEvent() )
			{
				Closure<?> handler = replaceHandlers.get( event.getProperty() );
				if( handler != null )
				{
					try
					{
						switch( handler.getMaximumNumberOfParameters() )
						{
						case 0 :
							handler.call();
							break;
						case 1 :
							handler.call( event.getProperty().getValue() );
							break;
						case 2 :
						default :
							handler.call( event.getProperty().getValue(), event.getPreviousValue() );
						}
					}
					catch( Exception e )
					{
						log.error( "Exception caught when calling onReplace handler for " + event.getProperty().getKey(), e );
					}
				}
			}
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
