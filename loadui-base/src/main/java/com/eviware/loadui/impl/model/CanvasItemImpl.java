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
package com.eviware.loadui.impl.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.BehaviorProvider.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.config.CanvasItemConfig;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.config.ConnectionConfig;
import com.eviware.loadui.impl.counter.AggregatedCounterSupport;
import com.eviware.loadui.impl.counter.CounterStatisticSupport;
import com.eviware.loadui.impl.counter.CounterSupport;
import com.eviware.loadui.impl.statistics.CounterStatisticsWriter;
import com.eviware.loadui.impl.statistics.StatisticHolderSupport;
import com.eviware.loadui.impl.summary.MutableSummaryImpl;
import com.eviware.loadui.impl.terminal.ConnectionImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

public abstract class CanvasItemImpl<Config extends CanvasItemConfig> extends ModelItemImpl<Config> implements
		CanvasItem, StatisticHolder
{
	private static final String LIMITS_ATTRIBUTE = "limits";

	protected final CounterSupport counterSupport;
	private final Set<ComponentItem> components = new HashSet<ComponentItem>();
	protected final Set<Connection> connections = new HashSet<Connection>();
	private final ComponentListener componentListener = new ComponentListener();
	private final ConnectionListener connectionListener = new ConnectionListener();
	private final BehaviorProvider behaviorProvider;
	protected final ScheduledExecutorService scheduler;
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
	private boolean completed = false;

	private final Property<Boolean> abortOnFinish;

	// here keep all not loaded components and connections, remove them at the
	// end of init
	private ArrayList<ComponentItemConfig> badComponents = new ArrayList<ComponentItemConfig>();
	private ArrayList<ConnectionConfig> badConnections = new ArrayList<ConnectionConfig>();

	private final StatisticHolderSupport statisticHolderSupport;
	private final CounterStatisticSupport counterStatisticSupport;

	public CanvasItemImpl( Config config, CounterSupport counterSupport )
	{
		super( config );

		lastSavedHash = DigestUtils.md5Hex( config.xmlText() );

		this.counterSupport = counterSupport;

		scheduler = BeanInjector.getBean( ScheduledExecutorService.class );
		behaviorProvider = BeanInjector.getBean( BehaviorProvider.class );

		statisticHolderSupport = new StatisticHolderSupport( this );
		counterStatisticSupport = new CounterStatisticSupport( this );

		StatisticVariable.Mutable requestVariable = statisticHolderSupport.addStatisticVariable( "Requests" );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, requestVariable );
		counterStatisticSupport.addCounterVariable( REQUEST_COUNTER, requestVariable );

		StatisticVariable.Mutable failuresVariable = statisticHolderSupport.addStatisticVariable( "Total Failures" );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, failuresVariable );
		counterStatisticSupport.addCounterVariable( FAILURE_COUNTER, failuresVariable );

		StatisticVariable.Mutable assertionFailuresVariable = statisticHolderSupport
				.addStatisticVariable( "Assertion Failures" );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, assertionFailuresVariable );
		counterStatisticSupport.addCounterVariable( ASSERTION_FAILURE_COUNTER, assertionFailuresVariable );

		StatisticVariable.Mutable requestFailuresVariable = statisticHolderSupport
				.addStatisticVariable( "Request Failures" );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, requestFailuresVariable );
		counterStatisticSupport.addCounterVariable( REQUEST_FAILURE_COUNTER, requestFailuresVariable );

		abortOnFinish = createProperty( ABORT_ON_FINISH_PROPERTY, Boolean.class, false );
	}

	@Override
	public void init()
	{
		super.init();

		counterSupport.init( this );

		loadingErrors = false;

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
				badConnections.add( connectionConfig );
				log.error( "Unable to create connection between terminals " + connectionConfig.getInputTerminalId()
						+ " and " + connectionConfig.getOutputTerminalId(), e );
			}
		}

		// now remove bad connections and components

		for( ComponentItemConfig badComponent : badComponents )
		{
			int cnt = 0;
			boolean found = false;
			for( ; cnt < getConfig().getComponentArray().length; cnt++ )
				if( getConfig().getComponentArray()[cnt].equals( badComponent ) )
				{
					found = true;
					break;
				}
			if( found )
				getConfig().removeComponent( cnt );
		}

		for( ConnectionConfig badConnection : badConnections )
		{
			int cnt = 0;
			boolean found = false;
			for( ; cnt < getConfig().getConnectionArray().length; cnt++ )
				if( getConfig().getConnectionArray()[cnt].equals( badConnection ) )
				{
					found = true;
					break;
				}
			if( found )
				getConfig().removeConnection( cnt );
		}

		addEventListener( BaseEvent.class, new ActionListener() );

		// timer.scheduleAtFixedRate( timerTask, 1000, 1000 );

		statisticHolderSupport.init();
		counterStatisticSupport.init();
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
		component.getContext().setHelpUrl( descriptor.getHelpUrl() );

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
			badComponents.add( config );
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

	@Override
	public boolean isRunning()
	{
		return running;
	}

	@Override
	public boolean isCompleted()
	{
		return completed;
	}

	@Override
	public void release()
	{
		for( ComponentItem component : new ArrayList<ComponentItem>( components ) )
			component.release();
		components.clear();
		connections.clear();
		summary = null;

		ReleasableUtils.releaseAll( counterStatisticSupport, statisticHolderSupport );

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
			s.append( e.getKey() ).append( '=' ).append( e.getValue().toString() ).append( ';' );
		setAttribute( LIMITS_ATTRIBUTE, s.toString() );

		if( TIMER_COUNTER.equals( counterName ) )
			fixTimeLimit();
		fireBaseEvent( LIMITS );
	}

	@Override
	public Summary getSummary()
	{
		return summary;
	}

	@Override
	public CanvasObjectItem duplicate( CanvasObjectItem obj )
	{
		if( !( obj instanceof ComponentItemImpl ) )
			throw new IllegalArgumentException( obj + " needs to be an instance of: " + ComponentItemImpl.class.getName() );

		ComponentItemConfig config = getConfig().addNewComponent();
		config.set( ( ( ComponentItemImpl )obj ).getConfig() );
		if( obj.getCanvas().equals( this ) )
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
		doGenerateSummary();
	}

	protected void doGenerateSummary()
	{
		log.debug( "Generating summary for: {}", CanvasItemImpl.this );
		MutableSummary summary = new MutableSummaryImpl();
		generateSummary( summary );
		CanvasItemImpl.this.summary = summary;
		fireBaseEvent( SUMMARY );
		triggerAction( READY_ACTION );
	}

	private void fixTimeLimit()
	{
		if( timeLimitFuture != null )
			timeLimitFuture.cancel( true );

		if( running && limits.containsKey( TIMER_COUNTER ) )
		{
			long delay = limits.get( TIMER_COUNTER ) * 1000 - time;
			if( delay > 0 )
			{
				timeLimitFuture = scheduler.schedule( new TimeLimitTask(), delay, TimeUnit.MILLISECONDS );
			}
		}
	}

	protected void reset()
	{
		startTime = isRunning() ? new Date() : null;
		endTime = null;
		hasStarted = isRunning();
		setTime( 0 );
		fixTimeLimit();
	}

	protected synchronized void setTime( long time )
	{
		this.time = time;
	}

	protected void setRunning( boolean running )
	{
		if( this.running != running )
		{
			this.running = running;
			fireBaseEvent( RUNNING );
		}
	}

	protected void setCompleted( boolean completed )
	{
		if( this.completed != completed )
		{
			this.completed = completed;
			if( completed )
			{
				fireBaseEvent( ON_COMPLETE_DONE );
			}
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

	@Override
	public boolean isDirty()
	{
		return !DigestUtils.md5Hex( getConfig().xmlText() ).equals( lastSavedHash );
	}

	@Override
	public boolean isStarted()
	{
		return hasStarted;
	}

	@Override
	public boolean isLoadingError()
	{
		return loadingErrors;
	}

	@Override
	public void cancelComponents()
	{
		for( ComponentItem component : getComponents() )
			if( component.isBusy() )
				component.triggerAction( ComponentItem.CANCEL_ACTION );
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
		boolean paused = false;

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof ActionEvent )
			{
				if( !running && START_ACTION.equals( event.getKey() ) )
				{
					// reset time if the test was started again.
					if( !paused )
					{
						setTime( 0 );
						startTime = new Date();
					}

					setRunning( true );
					timerFuture = scheduler.scheduleAtFixedRate( new TimeUpdateTask(), 250, 250, TimeUnit.MILLISECONDS );
					fixTimeLimit();
					hasStarted = true;
					paused = false;
					setCompleted( false );
				}
				else if( running && ( STOP_ACTION.equals( event.getKey() ) || COMPLETE_ACTION.equals( event.getKey() ) ) )
				{
					if( STOP_ACTION.equals( event.getKey() ) )
					{
						paused = true;
					}

					setRunning( false );
					if( timerFuture != null )
						timerFuture.cancel( true );
					if( timeLimitFuture != null )
						timeLimitFuture.cancel( true );
				}
				else if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
				{
					reset();
					paused = false;
				}

				if( COMPLETE_ACTION.equals( event.getKey() ) )
				{
					// This event is fired first on project and then on test cases.
					if( hasStarted )
					{
						hasStarted = false;
						if( isAbortOnFinish() )
						{
							// calculate end time
							Calendar endTimeCal = Calendar.getInstance();
							endTimeCal.setTime( startTime );
							endTimeCal.add( Calendar.MILLISECOND, ( int )time );
							endTime = endTimeCal.getTime();

							// If on PROJECT: First cancel all project components, then
							// call its onComplete method to finalize it. Method
							// 'onComplete' in project will start waiter which will
							// wait for all test cases to receive COMPLETE_ACTION event
							// and finalize them self.

							// If on TEST CASE: Cancels test case components, calls
							// 'onComplete' method on test case and fires
							// ON_COMPLETE_DONE event which tells project that this
							// test case was finalized.
							cancelComponents();
							onComplete( event.getSource() );
						}
						else
						{
							// Does the very same thing as above but does not cancel
							// components. It waits them to finish instead.
							new ComponentBusyAwaiter( event.getSource() );
						}
					}
					else
					{
						triggerAction( READY_ACTION );
					}
				}
			}
			else if( LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) && event instanceof CounterEvent
					&& isRunning() )
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
		private final long startTime;
		private final long initialTime;

		public TimeUpdateTask()
		{
			startTime = System.currentTimeMillis();
			initialTime = time;
		}

		@Override
		public void run()
		{
			final long timePassed = ( System.currentTimeMillis() - startTime );
			setTime( initialTime + timePassed );
		}
	}

	private class TimeLimitTask implements Runnable
	{
		@Override
		public void run()
		{
			setTime( getLimit( TIMER_COUNTER ) * 1000 );
			triggerAction( STOP_ACTION );
			triggerAction( COMPLETE_ACTION );
		}
	}

	private class TimerCounter implements Counter
	{
		@Override
		public long get()
		{
			return time / 1000;
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
			return time / 1000;
		}
	}

	/**
	 * Waits for all components to finish, then calculates end time and calls
	 * onComplete method. After this fires ON_COMPLETE_DONE method. Currently
	 * this event is sent by test cases to inform project that they are done.
	 * 
	 * @author predrag.vucetic
	 * 
	 */
	private class ComponentBusyAwaiter implements EventHandler<BaseEvent>
	{
		private final String TRY_READY = ComponentBusyAwaiter.class.getName() + "@tryReady";

		private final EventFirer source;

		// Counts how many components are still busy.
		private final AtomicInteger awaiting = new AtomicInteger();

		// Used to calculate extra time to be added to test case or project
		// duration. This extra time is the delay between the moment when
		// COMPLETE_ACTION event was received and when the last component
		// finished.
		private long awaiterStartTime;

		public ComponentBusyAwaiter( EventFirer source )
		{
			this.source = source;
			awaiterStartTime = System.currentTimeMillis();
			addEventListener( BaseEvent.class, this );
			fireBaseEvent( TRY_READY );
		}

		private void tryReady()
		{
			awaiting.set( 0 );
			for( ComponentItem component : getComponents() )
			{
				if( component.isBusy() )
				{
					component.addEventListener( BaseEvent.class, this );
					awaiting.incrementAndGet();
				}
			}
			if( awaiting.get() == 0 )
			{
				// Calculate the actual time when this test case or project have
				// finished.
				Calendar endTimeCal = Calendar.getInstance();
				endTimeCal.setTime( startTime );
				endTimeCal.add( Calendar.MILLISECOND, ( int )( time + System.currentTimeMillis() - awaiterStartTime ) );
				endTime = endTimeCal.getTime();
				// Finalize
				onComplete( source );
			}
			else
			{
				log.debug( "Waiting for {} components to finish...", awaiting.get() );
			}
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( TRY_READY ) )
			{
				removeEventListener( BaseEvent.class, this );
				tryReady();
			}
			else if( event.getKey().equals( ComponentItem.BUSY ) )
			{
				event.getSource().removeEventListener( BaseEvent.class, this );
				if( awaiting.decrementAndGet() == 0 )
					tryReady();
			}
		}
	}

	@Override
	public StatisticVariable getStatisticVariable( String statisticVariableName )
	{
		return statisticHolderSupport.getStatisticVariable( statisticVariableName );
	}

	@Override
	public Set<String> getStatisticVariableNames()
	{
		return statisticHolderSupport.getStatisticVariableNames();
	}

	@Override
	public boolean isAbortOnFinish()
	{
		return abortOnFinish.getValue();
	}

	@Override
	public void setAbortOnFinish( boolean abort )
	{
		abortOnFinish.setValue( abort );
	}

}
