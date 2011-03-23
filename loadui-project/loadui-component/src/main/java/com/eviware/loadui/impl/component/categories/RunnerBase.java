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
package com.eviware.loadui.impl.component.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.api.summary.SampleStatsImpl;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.statistics.CounterStatisticSupport;

/**
 * Base class for runner components which defines base behavior which can be
 * extended to fully implement a runner ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class RunnerBase extends BaseCategory implements RunnerCategory, EventHandler<BaseEvent>
{
	private final static int NUM_TOP_BOTTOM_SAMPLES = 5;

	private final ScheduledExecutorService scheduler;
	private final ScheduledFuture<?> updateTask;
	private final AssignmentListener assignmentListener;

	private final InputTerminal triggerTerminal;
	private final OutputTerminal resultTerminal;
	private final OutputTerminal currentlyRunningTerminal;

	private final AtomicInteger currentlyRunning = new AtomicInteger();
	private final AtomicBoolean isSleeping = new AtomicBoolean();

	private final Counter requestCounter;
	private final Counter sampleCounter;
	private final Counter failureCounter;
	private final Counter failedRequestCounter;
	private final Counter failedAssertionCounter;
	private final Counter discardsCounter;

	private final ExecutorService executor;

	private final Property<Long> concurrentSamplesProperty;
	private final Property<Long> maxQueueSizeProperty;
	private final Property<Boolean> countDiscarded;

	private long concurrentSamples;
	private long queueSize;
	private AtomicInteger workerCount = new AtomicInteger();
	private AtomicInteger queued = new AtomicInteger();

	private final BlockingQueue<TerminalMessage> queue = new LinkedBlockingQueue<TerminalMessage>();

	private final LinkedList<SampleStats> topStats = new LinkedList<SampleStats>();
	private final LinkedList<SampleStats> bottomStats = new LinkedList<SampleStats>();
	// needed to calc stat
	private long maxTime;
	private long minTime;
	private long avgTime;
	private long sumTotalTimeTaken;
	private long sumTotalSquare;

	private boolean hasCurrentlyRunning = false;
	private boolean released = false;

	private final Map<String, String> remoteValues = new HashMap<String, String>();
	private final OutputTerminal controllerTerminal;

	private final StatisticVariable.Mutable timeTakenVariable;
	private final StatisticVariable.Mutable responseSizeVariable;
	private final StatisticVariable.Mutable throughputVariable;
	private final StatisticVariable.Mutable runningVariable;
	private final StatisticVariable.Mutable queuedVariable;

	private final CounterStatisticSupport counterStatisticSupport;

	/**
	 * Constructs an RunnerBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the RunnerBase to.
	 */
	public RunnerBase( ComponentContext context )
	{
		super( context );

		context.setNonBlocking( true );

		executor = BeanInjector.getBean( ExecutorService.class );
		scheduler = BeanInjector.getBean( ScheduledExecutorService.class );

		triggerTerminal = context.createInput( TRIGGER_TERMINAL, "Trigger Input" );

		resultTerminal = context.createOutput( RESULT_TERMINAL, "Results" );
		Map<String, Class<?>> resultSignature = new HashMap<String, Class<?>>();
		resultSignature.put( TIME_TAKEN_MESSAGE_PARAM, Long.class );
		resultSignature.put( TIMESTAMP_MESSAGE_PARAM, Long.class );
		resultSignature.put( STATUS_MESSAGE_PARAM, Boolean.class );
		context.setSignature( resultTerminal, resultSignature );

		currentlyRunningTerminal = context.createOutput( CURRENLY_RUNNING_TERMINAL, "Requests Currently Running" );

		requestCounter = context.getCounter( CanvasItem.REQUEST_COUNTER );
		sampleCounter = context.getCounter( CanvasItem.SAMPLE_COUNTER );
		failureCounter = context.getCounter( CanvasItem.FAILURE_COUNTER );
		failedRequestCounter = context.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER );
		failedAssertionCounter = context.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER );
		discardsCounter = context.getCounter( RunnerCategory.DISCARDED_SAMPLES_COUNTER );

		// AverageWriters and ThroughputWriters
		timeTakenVariable = context.addStatisticVariable( "Time Taken", "SAMPLE" );
		responseSizeVariable = context.addStatisticVariable( "Response Size", "SAMPLE" );
		throughputVariable = context.addStatisticVariable( "Throughput", "THROUGHPUT" );
		runningVariable = context.addStatisticVariable( "Running", "VARIABLE" );
		queuedVariable = context.addStatisticVariable( "Queued", "VARIABLE" );

		// CounterWriters
		counterStatisticSupport = new CounterStatisticSupport( context );
		StatisticVariable.Mutable requestVariable = context.addStatisticVariable( "Completed", "COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.SAMPLE_COUNTER, requestVariable );
		StatisticVariable.Mutable failedVariable = context.addStatisticVariable( "Failures", "COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.FAILURE_COUNTER, failedVariable );
		StatisticVariable.Mutable discardedVariable = context.addStatisticVariable( "Discarded", "COUNTER" );
		counterStatisticSupport.addCounterVariable( RunnerCategory.DISCARDED_SAMPLES_COUNTER, discardedVariable );
		StatisticVariable.Mutable sentVariable = context.addStatisticVariable( "Sent", "COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.REQUEST_COUNTER, sentVariable );

		concurrentSamplesProperty = context.createProperty( CONCURRENT_SAMPLES_PROPERTY, Long.class, 100 );
		concurrentSamples = concurrentSamplesProperty.getValue();
		maxQueueSizeProperty = context.createProperty( MAX_QUEUE_SIZE_PROPERTY, Long.class, 1000 );
		countDiscarded = context.createProperty( COUNT_DISCARDED_REQUESTS_PROPERTY, Boolean.class, false );

		queueSize = maxQueueSizeProperty.getValue();

		context.getComponent().addEventListener( BaseEvent.class, this );
		// init statistics
		resetStatistics();

		controllerTerminal = context.getControllerTerminal();

		CanvasItem canvasItem = context.getCanvas();
		if( canvasItem instanceof SceneItem )
		{
			updateTask = scheduler.scheduleAtFixedRate( new UpdateRemoteTask(), 1, 1, TimeUnit.SECONDS );
			if( LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) )
				canvasItem.getProject().addEventListener( CollectionEvent.class,
						assignmentListener = new AssignmentListener() );
			else
				assignmentListener = null;
		}
		else
		{
			updateTask = null;
			assignmentListener = null;
		}

		counterStatisticSupport.init();
	}

	/**
	 * Causes a sample to be made. The sample may be run synchronously, in which
	 * case the result of the sample should be returned in the form of a
	 * TerminalMessage, or asynchronously, in which case null should be returned,
	 * and sampleCompleted should later be called with the result, as well as the
	 * exact sampleId which was used to initiate the sample.
	 * 
	 * @param triggerMessage
	 *           The triggering TerminalMessage. It may optionally contain
	 *           arguments which are used by the runner.
	 * @param sampleId
	 *           An ID which is used to identify the sample, and should be used
	 *           if the runner is executed asynchronously.
	 * @return The result of the sample as a TerminalMessage, or null if the
	 *         sample is executed asynchronously.
	 */
	protected abstract TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId )
			throws SampleCancelledException;

	/**
	 * Invoked when a request is made to cancel the running samples. The
	 * RunnerBase will take care of clearing the queue. This method can be
	 * implemented to cancel running samples.
	 */
	protected abstract void onCancel();

	/**
	 * Called when a sample is completed. Updates the currentlyRunning count as
	 * well as adds the timeTaken parameter to the message.
	 * 
	 * @param message
	 *           The result of the sample.
	 * @param sampleId
	 *           The ID of the sample.
	 */
	final public void sampleCompleted( TerminalMessage message, Object sampleId )
	{
		long timeTaken = ( System.nanoTime() - ( Long )sampleId ) / 1000000;
		long startTime = System.currentTimeMillis() - timeTaken;

		int cRunning = currentlyRunning.decrementAndGet();
		updateCurrentlyRunning( cRunning );

		if( !message.containsKey( TIMESTAMP_MESSAGE_PARAM ) )
			message.put( TIMESTAMP_MESSAGE_PARAM, startTime );

		if( !message.containsKey( TIME_TAKEN_MESSAGE_PARAM ) )
			message.put( TIME_TAKEN_MESSAGE_PARAM, timeTaken );
		getContext().send( resultTerminal, message );
		sampleCounter.increment();

		// Gather statistics from the completed sample.
		timeTaken = ( Long )message.get( TIME_TAKEN_MESSAGE_PARAM );
		if( timeTaken > maxTime )
			maxTime = timeTaken;
		if( timeTaken < minTime || minTime == -1 )
			minTime = timeTaken;
		sumTotalTimeTaken += timeTaken;
		avgTime = sumTotalTimeTaken / sampleCounter.get();
		sumTotalSquare += Math.pow( timeTaken - avgTime, 2 );
		long size = message.containsKey( "Bytes" ) ? ( ( Number )message.get( "Bytes" ) ).longValue() : ( message
				.containsKey( "Response" ) ? ( ( String )message.get( "Response" ) ).length() : 0 );

		addTopBottomSample( startTime, timeTaken, size );

		if( cRunning == 0 )
			getContext().setBusy( false );

		// Update StatisticsWriters
		timeTakenVariable.update( startTime, timeTaken );
		responseSizeVariable.update( startTime, size );
		throughputVariable.update( startTime, size );
	}

	private synchronized void addTopBottomSample( long time, long timeTaken, long size )
	{
		SampleStats current = new SampleStatsImpl( time, size, timeTaken );
		SampleStats stat = null;
		boolean inserted = false;

		// top
		ListIterator<SampleStats> it = topStats.listIterator();
		while( it.hasNext() )
		{
			stat = it.next();
			if( current.getTimeTaken() < stat.getTimeTaken() )
			{
				it.previous();
				it.add( current );
				inserted = true;
				break;
			}
		}
		if( !inserted )
		{
			if( topStats.size() < NUM_TOP_BOTTOM_SAMPLES )
				topStats.addLast( current );
		}
		else if( topStats.size() > NUM_TOP_BOTTOM_SAMPLES )
			topStats.removeLast();

		// bottom
		inserted = false;
		it = bottomStats.listIterator();
		while( it.hasNext() )
		{
			stat = it.next();
			if( current.getTimeTaken() > stat.getTimeTaken() )
			{
				it.previous();
				it.add( current );
				inserted = true;
				break;
			}
		}
		if( !inserted )
		{
			if( bottomStats.size() < NUM_TOP_BOTTOM_SAMPLES )
				bottomStats.addLast( current );
		}
		else if( bottomStats.size() > NUM_TOP_BOTTOM_SAMPLES )
			bottomStats.removeLast();
	}

	private void updateCurrentlyRunning( int running )
	{
		runningVariable.update( System.currentTimeMillis(), running );
		if( hasCurrentlyRunning )
		{
			TerminalMessage message = getContext().newMessage();
			message.put( CURRENTLY_RUNNING_MESSAGE_PARAM, running );
			getContext().send( currentlyRunningTerminal, message );
		}
	}

	private void updateQueued( int queued )
	{
		queuedVariable.update( System.currentTimeMillis(), queued );
	}

	final public int getCurrentlyRunning()
	{
		int runningSize = currentlyRunning.get();
		for( String value : remoteValues.values() )
		{
			String[] parts = value.split( ";" );
			runningSize += Integer.parseInt( parts[0] );
		}
		return runningSize;
	}

	@Override
	final public OutputTerminal getCurrentlyRunningTerminal()
	{
		return currentlyRunningTerminal;
	}

	@Override
	final public OutputTerminal getResultTerminal()
	{
		return resultTerminal;
	}

	@Override
	final public InputTerminal getTriggerTerminal()
	{
		return triggerTerminal;
	}

	@Override
	public Counter getRequestCounter()
	{
		return requestCounter;
	}

	@Override
	final public Counter getSampleCounter()
	{
		return sampleCounter;
	}

	@Override
	final public Counter getDiscardCounter()
	{
		return discardsCounter;
	}

	final public Counter getFailureCounter()
	{
		return failureCounter;
	}

	final public Counter getFailedRequestCounter()
	{
		return failedRequestCounter;
	}

	final public Counter getFailedAssertionCounter()
	{
		return failedAssertionCounter;
	}

	@Override
	protected void cancel()
	{
		queue.clear();
		queued.set( 0 );
		updateQueued( 0 );
		getContext().setBusy( false );

		onCancel();
	}

	@Override
	final public String getCategory()
	{
		return RunnerCategory.CATEGORY;
	}

	@Override
	final public String getColor()
	{
		return COLOR;
	}

	private void enqueue( TerminalMessage message )
	{
		if( queued.get() < queueSize && !released )
		{
			queue.offer( message );
			updateQueued( queued.incrementAndGet() );
		}
		else
		{
			if( countDiscarded.getValue() )
			{
				requestCounter.increment();
				failedRequestCounter.increment();
				failureCounter.increment();
			}
			discardsCounter.increment();
		}

		if( workerCount.get() < concurrentSamples && !isSleeping.get() )
		{
			long current = workerCount.incrementAndGet();
			if( current <= concurrentSamples )
				executor.execute( new Worker() );
			else
				workerCount.decrementAndGet();
		}
	}

	private void doSample( TerminalMessage message )
	{
		if( !getContext().isInvalid() )
		{
			requestCounter.increment();
			getContext().setBusy( true );
			Long startTime = System.nanoTime();
			updateCurrentlyRunning( currentlyRunning.incrementAndGet() );

			// remove leftovers from previous runner
			message.remove( TIMESTAMP_MESSAGE_PARAM );
			message.remove( TIME_TAKEN_MESSAGE_PARAM );

			TerminalMessage result = null;
			try
			{
				result = sample( message, startTime );
			}
			catch( Exception e )
			{
				updateCurrentlyRunning( currentlyRunning.decrementAndGet() );
				if( !( e instanceof SampleCancelledException ) )
					log.error( "Exception when calling 'sample'", e );
			}
			if( result != null )
			{
				// DON'T REMOVE THIS! Returning null means that the runner will
				// manually call sampleCompleted (for asynchronous runners).
				sampleCompleted( result, startTime );
			}
		}
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
		if( output == currentlyRunningTerminal )
		{
			updateCurrentlyRunning( currentlyRunning.get() );
			hasCurrentlyRunning = true;
		}
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		if( output == currentlyRunningTerminal )
		{
			hasCurrentlyRunning = output.getConnections().size() > 0;
		}
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, final TerminalMessage message )
	{
		if( input == triggerTerminal )
			enqueue( message );
		else if( message.containsKey( "remoteData" ) && isAssigned( output.getId() ) )
			remoteValues.put( output.getId(), ( String )message.get( "remoteData" ) );
	}

	private boolean isAssigned( String id )
	{
		try
		{
			String agentId = id.split( "/" )[1];
			CanvasItem canvasItem = getContext().getCanvas();
			for( AgentItem agent : canvasItem.getProject().getAgentsAssignedTo( ( SceneItem )canvasItem ) )
				if( agent.getId().equals( agentId ) )
					return true;
		}
		catch( Exception e )
		{
		}

		return false;
	}

	@Override
	public void handleEvent( BaseEvent event )
	{
		if( event instanceof ActionEvent )
		{
			if( SAMPLE_ACTION.equals( event.getKey() ) )
			{
				enqueue( getContext().newMessage() );
			}
			else if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
			{
				bottomStats.clear();
				topStats.clear();
				resetStatistics();
				int size = queue.size();
				queued.set( size );
				updateQueued( size );
			}
			else if( CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
			{
				queue.clear();
				queued.set( 0 );
				updateQueued( 0 );
			}
		}
		else if( event instanceof PropertyEvent )
		{
			PropertyEvent pEvent = ( PropertyEvent )event;
			if( pEvent.getProperty() == concurrentSamplesProperty )
				concurrentSamples = concurrentSamplesProperty.getValue();
			else if( pEvent.getProperty() == maxQueueSizeProperty )
				queueSize = maxQueueSizeProperty.getValue();
		}
		else if( ModelItem.RELEASED.equals( event.getKey() ) )
		{
			released = true;
			queue.clear();
			if( updateTask != null )
				updateTask.cancel( true );
			if( assignmentListener != null )
				getContext().getCanvas().getProject().removeEventListener( CollectionEvent.class, assignmentListener );
			getContext().getComponent().removeEventListener( BaseEvent.class, this );
		}
	}

	@Override
	public Object collectStatisticsData()
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put( "min", minTime );
		data.put( "max", maxTime );
		data.put( "avg", avgTime );
		data.put( "sumTotalSquare", sumTotalSquare );

		Set<SampleStats> stats = new HashSet<SampleStats>();
		stats.addAll( getTopSamples() );
		stats.addAll( getBottomSamples() );
		if( !stats.isEmpty() )
		{
			StringBuilder s = new StringBuilder();
			for( SampleStats stat : stats )
				s.append( stat.getTime() + ":" + stat.getTimeTaken() + ":" + stat.getSize() + ";" );
			data.put( "samples", s.toString() );
		}
		return data;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData )
	{
		long avgSum = 0;
		for( Object data : statisticsData.values() )
		{
			Map<String, Object> map = ( Map<String, Object> )data;
			long newMinTime = ( ( Number )map.get( "min" ) ).longValue();
			minTime = minTime == -1 ? newMinTime : Math.min( minTime, newMinTime );
			maxTime = Math.max( maxTime, ( ( Number )map.get( "max" ) ).longValue() );
			sumTotalSquare += ( ( Number )map.get( "sumTotalSquare" ) ).longValue();
			avgSum += ( ( Number )map.get( "avg" ) ).longValue();

			if( map.containsKey( "samples" ) )
			{
				String[] entries = ( ( String )map.get( "samples" ) ).split( ";" );
				for( String entry : entries )
				{
					String[] vals = entry.split( ":" );
					addTopBottomSample( Long.parseLong( vals[0] ), Long.parseLong( vals[1] ), Long.parseLong( vals[2] ) );
				}
			}
		}
		avgTime = avgSum / statisticsData.size();
	}

	@Override
	public List<SampleStats> getTopSamples()
	{
		return new ArrayList<SampleStats>( topStats );
	}

	@Override
	public List<SampleStats> getBottomSamples()
	{
		return new ArrayList<SampleStats>( bottomStats );
	}

	@Override
	public Map<String, String> getStatistics()
	{
		Map<String, String> statistics = new HashMap<String, String>();

		long requestCount = requestCounter.get();
		long failureCount = failedRequestCounter.get();

		if( requestCount > 0 )
		{
			long perc = failureCount * 100 / requestCount;
			String errorRatio = perc + "%"; // failureCount + "/" + sampleCount +
			// " (" + perc + "%)";

			statistics.put( "cnt", String.valueOf( requestCount ) );
			statistics.put( "min", String.valueOf( minTime ) );
			statistics.put( "max", String.valueOf( maxTime ) );
			statistics.put( "avg", String.valueOf( avgTime ) );
			statistics.put( "std-dev",
					String.valueOf( Math.round( Math.sqrt( sumTotalSquare / requestCount ) * 100d ) / 100d ) );
			if( avgTime > 0 )
			{
				statistics.put( "min/avg", String.valueOf( Math.round( ( minTime / avgTime ) * 100d ) / 100d ) );
				statistics.put( "max/avg", String.valueOf( Math.round( ( maxTime / avgTime ) * 100d ) / 100d ) );
			}
			else
			{
				statistics.put( "min/avg", "N/A" );
				statistics.put( "max/avg", "N/A" );
			}
			statistics.put( "err", String.valueOf( failureCount ) );
			statistics.put( "ratio", errorRatio );
		}
		else
		{
			statistics.put( "cnt", "N/A" );
			statistics.put( "min", "N/A" );
			statistics.put( "max", "N/A" );
			statistics.put( "avg", "N/A" );
			statistics.put( "std-dev", "N/A" );
			statistics.put( "min/avg", "N/A" );
			statistics.put( "max/avg", "N/A" );
			statistics.put( "err", "N/A" );
			statistics.put( "ratio", "N/A" );
		}

		return statistics;
	}

	/*
	 * Here is all statistics reseted/initialized to avoid NPE
	 */
	private void resetStatistics()
	{
		maxTime = -1l;
		minTime = -1l;
		sumTotalTimeTaken = 0;
		sumTotalSquare = 0;

		remoteValues.clear();

		topStats.clear();
		bottomStats.clear();
	}

	public long getDiscarded()
	{
		return discardsCounter.get();
	}

	@Override
	final public long getQueueSize()
	{
		long queueSize = queued.get();
		for( String value : remoteValues.values() )
		{
			String[] parts = value.split( ";" );
			queueSize += Long.parseLong( parts[1] );
		}
		return queueSize;
	}

	private class Worker implements Runnable
	{
		private boolean exit = false;

		@Override
		public void run()
		{
			TerminalMessage message;

			while( !exit )
			{
				try
				{
					message = queue.poll();
					if( message == null && isSleeping.compareAndSet( false, true ) )
					{
						message = queue.poll( 10, TimeUnit.SECONDS );
						isSleeping.set( false );
					}
					if( message == null || released )
					{
						exit = true;
					}
					else
					{
						updateQueued( queued.decrementAndGet() );
						doSample( message );
					}
				}
				catch( InterruptedException e )
				{
					// Ignore
				}
			}
			workerCount.decrementAndGet();
		}
	}

	private class UpdateRemoteTask implements Runnable
	{
		private final ComponentContext context;
		private final TerminalMessage message;

		public UpdateRemoteTask()
		{
			context = getContext();
			message = context.newMessage();
		}

		@Override
		public void run()
		{
			message.put( "remoteData", currentlyRunning.get() + ";" + queued.get() );
			context.send( controllerTerminal, message );
		}
	}

	private class AssignmentListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( ProjectItem.ASSIGNMENTS.equals( event.getKey() ) && event.getEvent().equals( Event.REMOVED ) )
			{
				Assignment assignment = ( Assignment )event.getElement();
				remoteValues.remove( getContext().getId() + "/" + assignment.getAgent().getId() );
			}
		}
	}

	public static class SampleCancelledException extends Exception
	{
		private static final long serialVersionUID = 1442916589020990178L;
	}

	protected ScheduledExecutorService getScheduler()
	{
		return scheduler;
	}

	protected ExecutorService getExecutor()
	{
		return executor;
	}
}