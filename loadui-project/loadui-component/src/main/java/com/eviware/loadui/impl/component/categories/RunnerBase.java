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
package com.eviware.loadui.impl.component.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.api.summary.SampleStatsImpl;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.impl.component.BlinkOnUpdateActivityStrategy;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.statistics.CounterStatisticSupport;
import com.eviware.loadui.util.statistics.StatisticDescriptorImpl;
import com.google.common.collect.ImmutableMap;

/**
 * Base class for runner components which defines base behavior which can be
 * extended to fully implement a runner ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class RunnerBase extends BaseCategory implements RunnerCategory, EventHandler<BaseEvent>
{
	public static final String REMOTE_DATA = "remoteData";

	private final static int NUM_TOP_BOTTOM_SAMPLES = 5;

	private final ScheduledExecutorService scheduler;
	private final BlinkOnUpdateActivityStrategy activityStrategy = ActivityStrategies.newBlinkOnUpdateStrategy();

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

	protected final Property<Long> concurrentSamplesProperty;
	protected final Property<Long> maxQueueSizeProperty;
	private final Property<Boolean> countDiscarded;

	private long concurrentSamples;
	private long queueSize;
	private final AtomicInteger workerCount = new AtomicInteger();
	private final AtomicInteger queued = new AtomicInteger();

	private final LinkedBlockingQueue<TerminalMessage> queue = new LinkedBlockingQueue<>();

	private final LinkedList<SampleStats> topStats = new LinkedList<>();
	private final LinkedList<SampleStats> bottomStats = new LinkedList<>();
	// needed to calc stat
	private long maxTime;
	private long minTime;
	private long avgTime;
	private long sumTotalTimeTaken;
	private long sumTotalSquare;

	private boolean hasCurrentlyRunning = false;
	private boolean released = false;

	private final StatisticVariable.Mutable timeTakenVariable;
	private final StatisticVariable.Mutable responseSizeVariable;
	private final StatisticVariable.Mutable throughputVariable;
	private final StatisticVariable.Mutable runningVariable;
	private final StatisticVariable.Mutable queuedVariable;

	private final CounterStatisticSupport counterStatisticSupport;

	private final Value<Number> currentlyRunningTotal;
	private final Value<Number> queueSizeTotal;

	/**
	 * Constructs an RunnerBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the RunnerBase to.
	 */
	public RunnerBase( ComponentContext context )
	{
		super( context );

		executor = BeanInjector.getBean( ExecutorService.class );
		scheduler = BeanInjector.getBean( ScheduledExecutorService.class );

		context.setNonBlocking( true );

		context.setActivityStrategy( activityStrategy );

		triggerTerminal = context.createInput( TRIGGER_TERMINAL, "Trigger Input",
				"Connect to a Generator to recieve trigger signals. Each signal will trigger the component to run once." );
		context.setLikeFunction( triggerTerminal, new ComponentContext.LikeFunction()
		{
			@Override
			public boolean call( OutputTerminal output )
			{
				return output.getMessageSignature().containsKey( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM );
			}
		} );

		resultTerminal = context.createOutput( RESULT_TERMINAL, "Results",
				"Outputs data such as TimeTaken for each request." );

		context.setSignature( resultTerminal, ImmutableMap.<String, Class<?>> of( TIME_TAKEN_MESSAGE_PARAM, Long.class,
				TIMESTAMP_MESSAGE_PARAM, Long.class, STATUS_MESSAGE_PARAM, Boolean.class ) );

		currentlyRunningTerminal = context.createOutput( CURRENLY_RUNNING_TERMINAL, "Requests Currently Running",
				"Outputs the number of currently running requests, when that number changes." );

		context.setSignature( currentlyRunningTerminal,
				ImmutableMap.<String, Class<?>> of( CURRENTLY_RUNNING_MESSAGE_PARAM, Long.class ) );

		requestCounter = context.getCounter( CanvasItem.REQUEST_COUNTER );
		sampleCounter = context.getCounter( CanvasItem.SAMPLE_COUNTER );
		failureCounter = context.getCounter( CanvasItem.FAILURE_COUNTER );
		failedRequestCounter = context.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER );
		failedAssertionCounter = context.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER );
		discardsCounter = context.getCounter( RunnerCategory.DISCARDED_SAMPLES_COUNTER );

		// AverageWriters and ThroughputWriters
		timeTakenVariable = context.addListenableStatisticVariable( "Time Taken",
				"elapsed time for a request to complete", "SAMPLE" );
		responseSizeVariable = context.addListenableStatisticVariable( "Response Size", "response size (in bytes)",
				"SAMPLE" );
		throughputVariable = context.addStatisticVariable( "Throughput", "", "THROUGHPUT" );
		runningVariable = context.addStatisticVariable( "Running", "running requests", "VARIABLE" );
		queuedVariable = context.addStatisticVariable( "Queued", "queued requests", "VARIABLE" );

		// CounterWriters
		counterStatisticSupport = new CounterStatisticSupport( context );
		StatisticVariable.Mutable requestVariable = context.addStatisticVariable( "Completed", "completed requests",
				"COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.SAMPLE_COUNTER, requestVariable );
		StatisticVariable.Mutable failedVariable = context
				.addStatisticVariable( "Failures", "failed requests", "COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.FAILURE_COUNTER, failedVariable );
		StatisticVariable.Mutable discardedVariable = context.addStatisticVariable( "Discarded", "discarded requests",
				"COUNTER" );
		counterStatisticSupport.addCounterVariable( RunnerCategory.DISCARDED_SAMPLES_COUNTER, discardedVariable );
		StatisticVariable.Mutable sentVariable = context.addStatisticVariable( "Sent", "sent requests", "COUNTER" );
		counterStatisticSupport.addCounterVariable( CanvasItem.REQUEST_COUNTER, sentVariable );

		context.getDefaultStatistics().add(
				new StatisticDescriptorImpl( timeTakenVariable.getStatistic( "AVERAGE", StatisticVariable.MAIN_SOURCE ) ) );
		context.getDefaultStatistics().add(
				new StatisticDescriptorImpl( throughputVariable.getStatistic( "TPS", StatisticVariable.MAIN_SOURCE ) ) );

		concurrentSamplesProperty = context.createProperty( CONCURRENT_SAMPLES_PROPERTY, Long.class, 100 );
		concurrentSamples = concurrentSamplesProperty.getValue();
		maxQueueSizeProperty = context.createProperty( MAX_QUEUE_SIZE_PROPERTY, Long.class, 1000 );
		countDiscarded = context.createProperty( COUNT_DISCARDED_REQUESTS_PROPERTY, Boolean.class, false );

		queueSize = maxQueueSizeProperty.getValue();

		context.getComponent().addEventListener( BaseEvent.class, this );
		// init statistics
		resetStatistics();

		currentlyRunningTotal = createTotal( "currentlyRunning", new Callable<Number>()
		{
			@Override
			public Number call() throws Exception
			{
				return currentlyRunning.get();
			}
		} );

		queueSizeTotal = createTotal( "queueSize", new Callable<Number>()
		{
			@Override
			public Number call() throws Exception
			{
				return queued.get();
			}
		} );

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
	 * 
	 * @return The number of running requests that were cancelled.
	 */
	protected abstract int onCancel();

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
		{
			getContext().setBusy( false );
			activityStrategy.setActivity( false );
		}

		// Update StatisticsWriters
		timeTakenVariable.update( startTime + timeTaken, timeTaken );
		responseSizeVariable.update( startTime + timeTaken, size );
		throughputVariable.update( startTime + timeTaken, size );
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

	private void updateCurrentlyRunning( long running )
	{
		runningVariable.update( System.currentTimeMillis(), running );
		if( hasCurrentlyRunning )
		{
			TerminalMessage message = getContext().newMessage();
			message.put( CURRENTLY_RUNNING_MESSAGE_PARAM, running );
			getContext().send( currentlyRunningTerminal, message );
		}
	}

	private void updateQueued( int currentlyQueued )
	{
		queuedVariable.update( System.currentTimeMillis(), currentlyQueued );
	}

	final public int getCurrentlyRunning()
	{
		return currentlyRunningTotal.getValue().intValue();
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
		discardsCounter.increment( queue.size() );
		queue.clear();
		queued.set( 0 );
		updateQueued( 0 );
		getContext().setBusy( false );
		activityStrategy.setActivity( false );

		int runningRequests = onCancel();
		discardsCounter.increment( runningRequests );
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
		activityStrategy.setActivity( true );

		if( queued.get() < queueSize && !released )
		{
			queue.add( message );
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
			catch( SampleCancelledException e )
			{
				updateCurrentlyRunning( currentlyRunning.decrementAndGet() );
				return;
			}
			catch( RuntimeException e )
			{
				updateCurrentlyRunning( currentlyRunning.decrementAndGet() );
				log.error( "Exception when calling 'sample'", e );

				sampleCounter.increment();
				failedRequestCounter.increment();
				failureCounter.increment();
				return;
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
		super.onTerminalConnect( output, input );

		if( output == currentlyRunningTerminal )
		{
			updateCurrentlyRunning( currentlyRunning.get() );
			hasCurrentlyRunning = true;
		}
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalDisconnect( output, input );

		if( output == currentlyRunningTerminal )
		{
			hasCurrentlyRunning = output.getConnections().size() > 0;
		}
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, final TerminalMessage message )
	{
		super.onTerminalMessage( output, input, message );

		if( input == triggerTerminal )
			enqueue( message );
	}

	@Override
	public synchronized void onRelease()
	{
		super.onRelease();
		ReleasableUtils.release( activityStrategy );
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
			getContext().getComponent().removeEventListener( BaseEvent.class, this );
		}
	}

	@Override
	public Object collectStatisticsData()
	{
		Map<String, Object> data = new HashMap<>();
		data.put( "min", minTime );
		data.put( "max", maxTime );
		data.put( "avg", avgTime );
		data.put( "sumTotalSquare", sumTotalSquare );

		Set<SampleStats> stats = new HashSet<>();
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
		return new ArrayList<>( topStats );
	}

	@Override
	public List<SampleStats> getBottomSamples()
	{
		return new ArrayList<>( bottomStats );
	}

	@Override
	public Map<String, String> getStatistics()
	{
		Map<String, String> statistics = new HashMap<>();

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
			statistics.put( "std-dev", String.format( "%.2f", ( double )sumTotalSquare / requestCount ) );
			if( avgTime > 0 )
			{
				statistics.put( "min/avg", String.format( "%.2f", ( double )minTime / avgTime ) );
				statistics.put( "max/avg", String.format( "%.2f", ( double )maxTime / avgTime ) );
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
		return queueSizeTotal.getValue().longValue();
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
