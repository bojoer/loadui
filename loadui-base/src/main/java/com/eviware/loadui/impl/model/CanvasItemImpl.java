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
package com.eviware.loadui.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.BehaviorProvider.ComponentCreationException;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.config.CanvasItemConfig;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.config.ConnectionConfig;
import com.eviware.loadui.impl.counter.AggregatedCounterSupport;
import com.eviware.loadui.impl.counter.CounterSupport;
import com.eviware.loadui.impl.summary.MutableSummaryImpl;
import com.eviware.loadui.impl.terminal.ConnectionImpl;
import com.eviware.loadui.util.BeanInjector;

public abstract class CanvasItemImpl<Config extends CanvasItemConfig> extends ModelItemImpl<Config> implements
		CanvasItem
{
	private static final String LIMITS_ATTRIBUTE = "limits";

	protected final CounterSupport counterSupport;
	private final Set<ComponentItem> components = new HashSet<ComponentItem>();
	protected final Set<Connection> connections = new HashSet<Connection>();
	private final ComponentListener componentListener = new ComponentListener();
	private final ConnectionListener connectionListener = new ConnectionListener();
	private final BehaviorProvider behaviorProvider;
	private final ScheduledExecutorService scheduler;
	protected final Counter timerCounter = new TimerCounter();
	private ScheduledFuture<?> timerFuture;
	private ScheduledFuture<?> timeLimitFuture;
	private long time = 0;
	protected Summary summary = null;
	protected Date startTime = null;
	protected Date endTime = null;
	private boolean hasStarted = false;
	protected String lastSavedHash;

	private boolean loadingErrors = false;

	protected final Map<String, Long> limits = new HashMap<String, Long>();

	private boolean running = false;

	public CanvasItemImpl( Config config, CounterSupport counterSupport )
	{
		super( config );

		lastSavedHash = DigestUtils.md5Hex( config.xmlText() );

		this.counterSupport = counterSupport;

		scheduler = BeanInjector.getBean( ScheduledExecutorService.class );
		behaviorProvider = BeanInjector.getBean( BehaviorProvider.class );
	}

	@Override
	public void init()
	{
		super.init();

		counterSupport.init( this );

		loadingErrors = false;

		for( ComponentItemConfig componentConfig : getConfig().getComponentArray() )
		{
			try
			{
				loadComponent( componentConfig );
			}
			catch( ComponentCreationException e )
			{
				log.error( "Unable to load component: ", e );
				loadingErrors = true;
			}
		}

		for( ConnectionConfig connectionConfig : getConfig().getConnectionArray() )
		{
			try
			{
				Connection connection = new ConnectionImpl( connectionConfig );
				connection.getOutputTerminal().addEventListener( TerminalConnectionEvent.class, connectionListener );
				connections.add( connection );
			}
			catch( Exception e )
			{
				log.error( "Unable to create connection between terminals " + connectionConfig.getInputTerminalId()
						+ " and " + connectionConfig.getOutputTerminalId(), e );
			}
		}

		addEventListener( BaseEvent.class, new ActionListener() );

		String[] limitStrings = getAttribute( LIMITS_ATTRIBUTE, "" ).split( ";" );
		for( String limit : limitStrings )
		{
			String[] parts = limit.split( "=", 2 );
			try
			{
				if( parts.length == 2 )
					setLimit( parts[0], Long.parseLong( parts[1] ) );
			}
			catch( NumberFormatException e )
			{
				// Ignore
			}
		}

		// timer.scheduleAtFixedRate( timerTask, 1000, 1000 );
	}

	@Override
	public Counter getCounter( String counterName )
	{
		if( TIMER_COUNTER.equals( counterName ) )
			return timerCounter;

		return counterSupport.getCounter( counterName );
	}

	@Override
	public Collection<String> getCounterNames()
	{
		return counterSupport.getCounterNames();
	}

	@Override
	public ComponentItem createComponent( String label, ComponentDescriptor descriptor )
	{
		if( label == null )
			throw new IllegalArgumentException( "label is null!" );
		if( descriptor == null )
			throw new IllegalArgumentException( "descriptor is null!" );

		ComponentItemConfig config = getConfig().addNewComponent();
		config.setType( descriptor.getType() );
		config.setLabel( label );
		ComponentItemImpl component = new ComponentItemImpl( this, config );
		component.init();
		component.setAttribute( ComponentItem.TYPE, descriptor.getLabel() );

		try
		{
			component.setBehavior( behaviorProvider.createBehavior( descriptor, component.getContext() ) );
			component.addEventListener( BaseEvent.class, componentListener );
			if( counterSupport instanceof AggregatedCounterSupport )
				( ( AggregatedCounterSupport )counterSupport ).addChild( component );
			components.add( component );
			fireCollectionEvent( COMPONENTS, CollectionEvent.Event.ADDED, component );
		}
		catch( ComponentCreationException e )
		{
			log.error( "Unable to load component: " + component, e );
			component.release();
			component = null;
		}

		return component;
	}

	protected abstract Connection createConnection( OutputTerminal output, InputTerminal input );

	private ComponentItemImpl loadComponent( ComponentItemConfig config ) throws ComponentCreationException
	{
		ComponentItemImpl component = new ComponentItemImpl( this, config );
		component.init();
		try
		{
			component.setBehavior( behaviorProvider.loadBehavior( config.getType(), component.getContext() ) );
			if( components.add( component ) )
			{
				component.addEventListener( BaseEvent.class, componentListener );
				if( counterSupport instanceof AggregatedCounterSupport )
					( ( AggregatedCounterSupport )counterSupport ).addChild( component );
			}
		}
		catch( ComponentCreationException e )
		{
			log.error( "Unable to load component: " + component, e );
			component.release();
			throw e;
		}

		return component;
	}

	public ComponentItem injectComponent( ComponentItemConfig config ) throws ComponentCreationException
	{
		ComponentItemConfig componentConf = getConfig().addNewComponent();
		componentConf.set( config );
		ComponentItem component;
		try
		{
			component = loadComponent( componentConf );
			fireCollectionEvent( COMPONENTS, CollectionEvent.Event.ADDED, component );
			return component;
		}
		catch( ComponentCreationException e )
		{
			getConfig().removeComponent( getConfig().sizeOfComponentArray() - 1 );

			throw e;
		}
	}

	@Override
	public Collection<ComponentItem> getComponents()
	{
		return Collections.unmodifiableSet( components );
	}

	@Override
	public ComponentItem getComponentByLabel( String label )
	{
		for( ComponentItem component : components )
			if( component.getLabel().equals( label ) )
				return component;

		return null;
	}

	@Override
	public Collection<Connection> getConnections()
	{
		return Collections.unmodifiableSet( connections );
	}

	@Override
	public Connection connect( OutputTerminal output, InputTerminal input )
	{
		// Locate the correct CanvasItem for the Connection.
		CanvasItem canvas = output.getTerminalHolder().getCanvas();
		if( canvas == input.getTerminalHolder().getCanvas() )
		{
			if( canvas != this )
				return canvas.connect( output, input );
		}
		// If the two Terminals are in separate CanvasItems, the connection should
		// be made in the ProjectItem.
		else if( canvas.getProject() != this )
			return canvas.getProject().connect( output, input );

		// Make sure an identical Connection doesn't already exist.
		for( Connection connection : output.getConnections() )
			if( connection.getInputTerminal().equals( input ) )
				return connection;

		// Create the Connection.
		Connection connection = createConnection( output, input );
		if( connections.add( connection ) )
		{
			connection.getOutputTerminal().addEventListener( TerminalConnectionEvent.class, connectionListener );
			fireCollectionEvent( CONNECTIONS, CollectionEvent.Event.ADDED, connection );
		}

		return connection;
	}

	public boolean isRunning()
	{
		return running;
	}

	@Override
	public void release()
	{
		for( ComponentItem component : new ArrayList<ComponentItem>( components ) )
			component.release();
		components.clear();
		connections.clear();
		summary = null;

		super.release();
	}

	protected void disconnect( Connection connection )
	{
		if( connections.remove( connection ) )
		{
			for( int i = getConfig().sizeOfConnectionArray() - 1; i >= 0; i-- )
			{
				ConnectionConfig connConfig = getConfig().getConnectionArray( i );
				if( connection.getOutputTerminal().getId().equals( connConfig.getOutputTerminalId() )
						&& connection.getInputTerminal().getId().equals( connConfig.getInputTerminalId() ) )
				{
					getConfig().removeConnection( i );
				}
			}
			fireCollectionEvent( CONNECTIONS, CollectionEvent.Event.REMOVED, connection );
		}
	}

	@Override
	public long getLimit( String counterName )
	{
		return limits.containsKey( counterName ) ? limits.get( counterName ) : -1;
	}

	@Override
	public void setLimit( String counterName, long counterValue )
	{
		if( counterValue > 0 )
			limits.put( counterName, counterValue );
		else
			limits.remove( counterName );

		StringBuilder s = new StringBuilder();
		for( Entry<String, Long> e : limits.entrySet() )
			s.append( e.getKey() ).append( "=" ).append( e.getValue().toString() ).append( ";" );
		setAttribute( LIMITS_ATTRIBUTE, s.toString() );

		if( TIMER_COUNTER.equals( counterName ) )
			fixTimeLimit();
	}

	@Override
	public Summary getSummary()
	{
		return summary;
	}

	@Override
	public CanvasObjectItem duplicate( CanvasObjectItem obj )
	{
		if( !( obj instanceof ComponentItem ) )
			throw new IllegalArgumentException( this + " cannot duplicate object of type " + obj.getClass() );

		if( !( obj instanceof ComponentItemImpl ) )
			throw new IllegalArgumentException( obj + " needs to be an instance of: " + ComponentItemImpl.class.getName() );

		ComponentItemConfig config = getConfig().addNewComponent();
		config.set( ( ( ComponentItemImpl )obj ).getConfig() );
		config.setLabel( "Copy of " + config.getLabel() );
		config.setId( addressableRegistry.generateId() );
		ComponentItemImpl copy;
		try
		{
			copy = loadComponent( config );
			fireCollectionEvent( COMPONENTS, CollectionEvent.Event.ADDED, copy );
			return copy;
		}
		catch( ComponentCreationException e )
		{
			// Shouldn't happen...
			throw new RuntimeException( e );
		}
	}

	protected void onComplete( EventFirer source )
	{
	}

	protected void doGenerateSummary()
	{
		log.debug( "Generating summary for: {}", CanvasItemImpl.this );
		MutableSummary summary = new MutableSummaryImpl();
		generateSummary( summary );
		CanvasItemImpl.this.summary = summary;
		fireBaseEvent( SUMMARY );
	}

	private void fixTimeLimit()
	{
		if( timeLimitFuture != null )
			timeLimitFuture.cancel( true );

		if( limits.containsKey( TIMER_COUNTER ) )
		{
			long delay = limits.get( TIMER_COUNTER ) - time;
			if( delay > 0 )
			{
				timeLimitFuture = scheduler.schedule( new TimeLimitTask(), delay, TimeUnit.SECONDS );
			}
		}
	}

	protected void reset()
	{
		startTime = isRunning() ? new Date() : null;
		hasStarted = isRunning();
		// Do it in the Timer thread to prevent concurrent modification
		// of
		// time.
		scheduler.execute( new Runnable()
		{
			@Override
			public void run()
			{
				time = 0;
				fixTimeLimit();
			}
		} );
	}

	protected void setRunning( boolean running )
	{
		this.running = running;
	}

	private class ComponentListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( RELEASED ) && counterSupport instanceof AggregatedCounterSupport )
				( ( AggregatedCounterSupport )counterSupport ).removeChild( ( CounterHolder )event.getSource() );

			if( event.getKey().equals( DELETED ) )
			{
				ComponentItem component = ( ComponentItem )event.getSource();
				if( components.remove( component ) )
				{
					for( int i = 0; i < getConfig().sizeOfComponentArray(); i++ )
					{
						if( component.getId().equals( getConfig().getComponentArray( i ).getId() ) )
						{
							getConfig().removeComponent( i );
							break;
						}
					}
					log.debug( "Firing COMPONENTS REMOVED {}", event.getSource() );
					fireCollectionEvent( COMPONENTS, CollectionEvent.Event.REMOVED, event.getSource() );
				}
			}
		}
	}

	private class ConnectionListener implements EventHandler<TerminalConnectionEvent>
	{
		@Override
		public void handleEvent( TerminalConnectionEvent event )
		{
			if( event.getEvent() == TerminalConnectionEvent.Event.DISCONNECT )
				disconnect( event.getConnection() );
		}
	}

	private class ActionListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof ActionEvent )
			{
				if( !running && START_ACTION.equals( event.getKey() ) )
				{
					setRunning( true );
					timerFuture = scheduler.scheduleAtFixedRate( new TimeUpdateTask(), 1, 1, TimeUnit.SECONDS );
					fixTimeLimit();
					if( startTime == null )
						startTime = new Date();
					hasStarted = true;
				}
				else if( running && ( STOP_ACTION.equals( event.getKey() ) || COMPLETE_ACTION.equals( event.getKey() ) ) )
				{
					setRunning( false );
					if( timerFuture != null )
						timerFuture.cancel( true );
					if( timeLimitFuture != null )
						timeLimitFuture.cancel( true );
					endTime = new Date();
				}
				else if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
					reset();

				if( COMPLETE_ACTION.equals( event.getKey() ) && hasStarted )
				{
					hasStarted = false;
					onComplete( event.getSource() );
				}
			}
			else if( event instanceof CounterEvent && isRunning() )
			{
				CounterEvent cEvent = ( CounterEvent )event;
				long limit = getLimit( cEvent.getKey() );
				if( limit > 0 && limit <= cEvent.getSource().getCounter( cEvent.getKey() ).get() )
				{
					triggerAction( STOP_ACTION );
					triggerAction( COMPLETE_ACTION );
				}
			}
		}
	}

	private class TimeUpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			time++ ;
		}
	}

	private class TimeLimitTask implements Runnable
	{
		@Override
		public void run()
		{
			time = getLimit( TIMER_COUNTER );
			triggerAction( STOP_ACTION );
			triggerAction( COMPLETE_ACTION );
		}
	}

	private class TimerCounter implements Counter
	{
		@Override
		public long get()
		{
			return time;
		}

		@Override
		public void increment()
		{
			throw new UnsupportedOperationException( "The timer counter cannot be manually incremented!" );
		}

		@Override
		public Class<Long> getType()
		{
			return Long.class;
		}

		@Override
		public Long getValue()
		{
			return time;
		}
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public Date getEndTime()
	{
		return endTime;
	}

	public boolean isDirty()
	{
		return !DigestUtils.md5Hex( getConfig().xmlText() ).equals( lastSavedHash );
	}

	public boolean isStarted()
	{
		return hasStarted;
	}

	public boolean isLoadingError()
	{
		return loadingErrors;
	}
}
