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
package com.eviware.loadui.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.BehaviorProvider.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.Statistic;
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
import com.eviware.loadui.impl.counter.CounterSupport;
import com.eviware.loadui.impl.statistics.CounterStatisticsWriter;
import com.eviware.loadui.impl.statistics.StatisticHolderSupport;
import com.eviware.loadui.impl.summary.MutableSummaryImpl;
import com.eviware.loadui.impl.terminal.ConnectionImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.InitializableUtils;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.eviware.loadui.util.events.EventFuture;
import com.eviware.loadui.util.statistics.CounterStatisticSupport;
import com.eviware.loadui.util.statistics.StatisticDescriptorImpl;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;

public abstract class CanvasItemImpl<Config extends CanvasItemConfig> extends ModelItemImpl<Config> implements
		CanvasItem
{
	private static final String LIMITS_ATTRIBUTE = "limits";

	protected final CounterSupport counterSupport;
	private final CollectionEventSupport<ComponentItem, Void> componentList;
	protected final CollectionEventSupport<Connection, Void> connectionList;
	private final ComponentListener componentListener = new ComponentListener();
	private final ConnectionListener connectionListener = new ConnectionListener();
	private final CanvasTestExecutionTask executionTask = new CanvasTestExecutionTask();
	private final ComponentRegistry componentRegistry;
	protected final ScheduledExecutorService scheduler;
	protected final Counter timerCounter = new TimerCounter();
	protected final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );
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
	private final ArrayList<ComponentItemConfig> badComponents = new ArrayList<ComponentItemConfig>();
	private final ArrayList<ConnectionConfig> badConnections = new ArrayList<ConnectionConfig>();

	private final StatisticHolderSupport statisticHolderSupport;
	private final CounterStatisticSupport counterStatisticSupport;

	public CanvasItemImpl( Config config, CounterSupport counterSupport )
	{
		super( config );

		lastSavedHash = DigestUtils.md5Hex( config.xmlText() );

		this.counterSupport = counterSupport;

		scheduler = BeanInjector.getBean( ScheduledExecutorService.class );
		componentRegistry = BeanInjector.getBean( ComponentRegistry.class );

		componentList = CollectionEventSupport.of( this, COMPONENTS );
		connectionList = CollectionEventSupport.of( this, CONNECTIONS );

		statisticHolderSupport = new StatisticHolderSupport( this );
		counterStatisticSupport = new CounterStatisticSupport( this );

		StatisticVariable.Mutable requestVariable = statisticHolderSupport.addStatisticVariable( REQUEST_VARIABLE );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, requestVariable );
		counterStatisticSupport.addCounterVariable( REQUEST_COUNTER, requestVariable );

		StatisticVariable.Mutable failuresVariable = statisticHolderSupport.addStatisticVariable( FAILURE_VARIABLE );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, failuresVariable );
		counterStatisticSupport.addCounterVariable( FAILURE_COUNTER, failuresVariable );

		StatisticVariable.Mutable assertionFailuresVariable = statisticHolderSupport
				.addStatisticVariable( ASSERTION_FAILURE_VARIABLE );
		statisticHolderSupport.addStatisticsWriter( CounterStatisticsWriter.TYPE, assertionFailuresVariable );
		counterStatisticSupport.addCounterVariable( ASSERTION_FAILURE_COUNTER, assertionFailuresVariable );

		StatisticVariable.Mutable requestFailuresVariable = statisticHolderSupport
				.addStatisticVariable( REQUEST_FAILURE_VARIABLE );
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
				connectionList.addItem( connection );
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

		testRunner.registerTask( executionTask, Phase.START, Phase.PRE_STOP, Phase.STOP );

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
		ComponentItemImpl component = InitializableUtils.initialize( new ComponentItemImpl( this, config ) );
		component.setAttribute( ComponentItem.TYPE, descriptor.getLabel() );
		if( descriptor.getHelpUrl() != null )
			component.getContext().setHelpUrl( descriptor.getHelpUrl() );

		try
		{
			component.setBehavior( componentRegistry.createBehavior( descriptor, component.getContext() ) );
			component.addEventListener( BaseEvent.class, componentListener );
			if( counterSupport instanceof AggregatedCounterSupport )
				( ( AggregatedCounterSupport )counterSupport ).addChild( component );
			componentList.addItem( component );
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
		final ComponentItemImpl component = InitializableUtils.initialize( new ComponentItemImpl( this, config ) );
		try
		{
			component.setBehavior( componentRegistry.loadBehavior( config.getType(), component.getContext() ) );
			componentList.addItem( component, new Runnable()
			{
				@Override
				public void run()
				{
					component.addEventListener( BaseEvent.class, componentListener );
					if( counterSupport instanceof AggregatedCounterSupport )
						( ( AggregatedCounterSupport )counterSupport ).addChild( component );
				}
			} );
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
		try
		{
			return loadComponent( componentConf );
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
		return componentList.getItems();
	}

	@Override
	public ComponentItem getComponentByLabel( String label )
	{
		for( ComponentItem component : componentList.getItems() )
			if( component.getLabel().equals( label ) )
				return component;

		return null;
	}

	@Override
	public Collection<Connection> getConnections()
	{
		return connectionList.getItems();
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
		else if( !( this instanceof ProjectItem && canvas.getProject() == ( ProjectItem )this ) )
			return canvas.getProject().connect( output, input );

		// Make sure an identical Connection doesn't already exist.
		for( Connection connection : output.getConnections() )
			if( connection.getInputTerminal().equals( input ) )
				return connection;

		// Create the Connection.
		final Connection connection = createConnection( output, input );
		connectionList.addItem( connection, new Runnable()
		{
			@Override
			public void run()
			{
				connection.getOutputTerminal().addEventListener( TerminalConnectionEvent.class, connectionListener );
			}
		} );

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
		testRunner.unregisterTask( executionTask, Phase.values() );
		ReleasableUtils.releaseAll( componentList, connectionList );
		summary = null;

		ReleasableUtils.releaseAll( counterStatisticSupport, statisticHolderSupport );

		super.release();
	}

	protected void disconnect( final Connection connection )
	{
		connectionList.removeItem( connection, new Runnable()
		{
			@Override
			public void run()
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
			}
		} );
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
		try
		{
			return loadComponent( config );
		}
		catch( ComponentCreationException e )
		{
			// Shouldn't happen...
			throw new RuntimeException( e );
		}
	}

	abstract void onComplete( EventFirer source );

	/**
	 * Called on a CanvasItem to append its summary chapters to a common summary
	 * object.
	 * 
	 * @param summary
	 */
	abstract void appendToSummary( MutableSummary summary );

	@Override
	public void generateSummary()
	{
		log.debug( "Generating summary for: {}", CanvasItemImpl.this );
		endTime = new Date();
		MutableSummary summary = new MutableSummaryImpl( getStartTime(), getEndTime() );
		appendToSummary( summary );
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
				endTime = new Date();
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

	@Override
	public Set<Statistic.Descriptor> getDefaultStatistics()
	{
		return ImmutableSet.<Statistic.Descriptor> of( new StatisticDescriptorImpl( this, REQUEST_VARIABLE, "PER_SECOND",
				StatisticVariable.MAIN_SOURCE ) );
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
				final ComponentItem component = ( ComponentItem )event.getSource();
				componentList.removeItem( component, new Runnable()
				{
					@Override
					public void run()
					{
						for( int i = 0; i < getConfig().sizeOfComponentArray(); i++ )
						{
							if( component.getId().equals( getConfig().getComponentArray( i ).getId() ) )
							{
								getConfig().removeComponent( i );
								break;
							}
						}
					}
				} );
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
				if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
				{
					reset();
				}
			}
			else if( LoadUI.isController() && event instanceof CounterEvent && isRunning() )
			{
				CounterEvent cEvent = ( CounterEvent )event;
				long limit = getLimit( cEvent.getKey() );
				if( limit > 0 && limit <= cEvent.getSource().getCounter( cEvent.getKey() ).get() )
				{
					List<TestExecution> executions = testRunner.getExecutionQueue();
					if( !executions.isEmpty() && executions.get( 0 ).getCanvas() == CanvasItemImpl.this )
					{
						executions.get( 0 ).complete();
					}
					else
					{
						triggerAction( STOP_ACTION );
						triggerAction( COMPLETE_ACTION );
					}
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
			List<TestExecution> executions = testRunner.getExecutionQueue();
			if( !executions.isEmpty() && executions.get( 0 ).getCanvas() == CanvasItemImpl.this )
			{
				executions.get( 0 ).complete();
			}
			else
			{
				setTime( getLimit( TIMER_COUNTER ) * 1000 );
				triggerAction( STOP_ACTION );
				triggerAction( COMPLETE_ACTION );
			}
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
			increment( 1 );
		}

		@Override
		public void increment( long value )
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

	private class CanvasTestExecutionTask implements TestExecutionTask
	{
		private final Function<ComponentItem, Future<BaseEvent>> busyFuture = new Function<ComponentItem, Future<BaseEvent>>()
		{
			@Override
			public Future<BaseEvent> apply( ComponentItem component )
			{
				return component.isBusy() ? EventFuture.forKey( component, ComponentItem.BUSY ) : Futures
						.<BaseEvent> immediateFuture( null );
			}
		};

		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( execution.contains( CanvasItemImpl.this ) )
			{
				switch( phase )
				{
				case START :
					setRunning( true );
					setTime( 0 );
					startTime = new Date();
					timerFuture = scheduler.scheduleAtFixedRate( new TimeUpdateTask(), 250, 250, TimeUnit.MILLISECONDS );
					fixTimeLimit();
					hasStarted = true;
					setCompleted( false );
					break;
				case PRE_STOP :
					hasStarted = false;
					if( timeLimitFuture != null )
						timeLimitFuture.cancel( true );

					if( isAbortOnFinish() )
					{
						cancelComponents();
					}
					else
					{
						for( Future<BaseEvent> future : Iterables.transform( getComponents(), busyFuture ) )
						{
							try
							{
								future.get();
							}
							catch( InterruptedException e )
							{
								log.error( "Failed waiting for a Component", e );
							}
							catch( ExecutionException e )
							{
								log.error( "Failed waiting for a Component", e );
							}
						}
					}
					onComplete( execution.getCanvas() );
					break;
				case STOP :
					if( timerFuture != null )
						timerFuture.cancel( true );
					setRunning( false );
					break;
				}
			}
		}
	}
}