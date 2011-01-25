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
package com.eviware.loadui.impl.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public abstract class AbstractStatisticsWriter implements StatisticsWriter
{
	public final static Logger log = LoggerFactory.getLogger( AbstractStatisticsWriter.class );

	private final StatisticsManager manager;
	private final StatisticVariable variable;
	private final String id;
	private final TrackDescriptor descriptor;
	
	protected long delay;
	protected long lastTimeFlushed = System.currentTimeMillis();
	private long pauseStartedTime;

	private long totalPause = 0;
	
	private long[] aggregateIntervals = {
			6000,		// 6 seconds
			120000,	// 2 minutes
			3600000,	// 1 hour
			10800000	// 3 hours
			};
	private AggregateLevel[] aggregateLevels = new AggregateLevel[4];
	private ArrayList<Entry> firstLevelEntries = new ArrayList<Entry>();

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values )
	{
		this.manager = manager;
		this.variable = variable;
		id = DigestUtils.md5Hex( variable.getStatisticHolder().getId() + variable.getName() + getType() );
		descriptor = new TrackDescriptorImpl( id, values );
		delay = manager.getMinimumWriteDelay();

		// init AggregationLevels, each level getting a reference to the previous level's aggregated entries.
		ArrayList<Entry> previousLevelEntries = firstLevelEntries;
		for( int i=0; i<aggregateLevels.length; i++ )
		{
			aggregateLevels[i] = new AggregateLevel( aggregateIntervals[i] , i+1, previousLevelEntries );
			previousLevelEntries = aggregateLevels[i].aggregatedEntries;
		}
			
		// TODO
		manager.getExecutionManager().registerTrackDescriptor( descriptor );
		manager.addEventListener( CollectionEvent.class, new ExecutionListener() );

		// adding execution listeners.
		manager.getExecutionManager().addExecutionListener( new ExecutionListenerAdapter()
		{
			@Override
			public void executionStarted( ExecutionManager.State oldState )
			{
				// unpause
				if( oldState == State.PAUSED )
				{
					/*
					 * Continue, calculate time spent in inteval when pause occured.
					 * Next write to database will be at regular interval, since
					 * delta is taken in account.
					 * 
					 * Example: if delay is 1s. Which means that flush occures at 1s,
					 * 2s, 3s, etc.. Pause occurs in 4th interval ( between 3s and 4s
					 * ) Than unpause comes after 3s( that woud be between 6s and 7s
					 * from test start ). flush() will be when test paused (3s +
					 * delta) and next is at 7s.
					 */
					long pauseTime = ( System.currentTimeMillis() - pauseStartedTime );
					totalPause += pauseTime;
					lastTimeFlushed += pauseTime;
					for( AggregateLevel a : aggregateLevels )
						a.lastFlush += pauseTime;
					pauseStartedTime = 0;
					if( pauseTime > delay )
						flush();
				}
			}

			@Override
			public void executionPaused( ExecutionManager.State oldState )
			{
				if( oldState == State.STARTED )
				{
					/*
					 * write data at moment when paused.
					 * 
					 * rember how time is spent in this interval after last time data
					 * is written to db.
					 */
					pauseStartedTime = System.currentTimeMillis();
				}
			}

			@Override
			public void executionStopped( ExecutionManager.State oldState )
			{
				/*
				 * if stoping write last data that came in.
				 * 
				 * or this should be done by execution manager?
				 */
				if( oldState == State.STARTED || oldState == State.PAUSED )
				{
					flush();
					pauseStartedTime = 0;
					totalPause = 0;
				}
			}
		} );
	}
	
	/**
	 * Called between Executions, letting the StatisticsWriter know that it
	 * should clear any buffers and prepare for a new Execution.
	 */
	protected abstract void reset();

	@Override
	public void setBufferSize( int bufferSize )
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getBufferSize()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public void setMinimumWriteDelay( long delay )
	{
		this.delay = delay;
	}

	@Override
	public TrackDescriptor getTrackDescriptor()
	{
		return descriptor;
	}
	
	@Override
	public void flush()
	{
		ExecutionManager executionManager = manager.getExecutionManager();
		
		Entry e = output();
		executionManager.writeEntry( getId(), e, StatisticVariable.MAIN_SOURCE, 0 );
		firstLevelEntries.add( e );
		
		for( AggregateLevel a : aggregateLevels )
		{
			if ( !a.needsFlushing() )
				break;
			Entry aggregatedEntry = aggregate( a.sourceEntries );
			log.debug( "aggregatedEntry: "+aggregatedEntry);
			a.flush();
			if( aggregatedEntry != null )
			{
				a.aggregatedEntries.add( aggregatedEntry );
				executionManager.writeEntry( getId(), aggregatedEntry, StatisticVariable.MAIN_SOURCE, a.getDatabaseKey() );
			}
			
		}
	}

	protected EntryBuilder at( long timestamp )
	{
		return new EntryBuilder( timestamp );
	}

	/**
	 * Builder for use in at( int timestamp ) to make writing data to the proper
	 * Track easy.
	 * 
	 * @author dain.nilsson
	 */
	protected class EntryBuilder
	{
		private final long timestamp;
		private final Map<String, Number> values = new HashMap<String, Number>();

		public EntryBuilder( long timestamp )
		{
			this.timestamp = timestamp;
		}

		public <T extends Number> EntryBuilder put( String name, T value )
		{
			values.put( name, value );
			return this;
		}
		
		public long getTimestamp()
		{
			return timestamp;
		}
		
		public Entry build()
		{
			ExecutionManager executionManager = manager.getExecutionManager();
			Execution currentExecution = executionManager.getCurrentExecution();
			int time = currentExecution == null ? -1 : ( int )( ( pauseStartedTime == 0 ? timestamp : pauseStartedTime )
					- currentExecution.getStartTime() - totalPause );
			return new EntryImpl( time, values, true );
		}
	}
	
	private class AggregateLevel
	{
		private long intervalInMillis;
		private long lastFlush;
		private int databaseKey;
		private ArrayList<Entry> sourceEntries;
		final public ArrayList<Entry> aggregatedEntries = new ArrayList<Entry>();
		
		AggregateLevel( long intervalInMillis, int databaseKey, ArrayList<Entry> sourceEntries )
		{
			this.intervalInMillis = intervalInMillis;
			this.databaseKey = databaseKey;
			this.sourceEntries = sourceEntries;
		}
		
		public boolean needsFlushing()
		{
			log.debug( "lastTimeFlushed:"+lastTimeFlushed+" intervalInMillis: "+intervalInMillis+" System.currentTimeMillis(): "+System.currentTimeMillis() );
			return lastFlush + intervalInMillis <= System.currentTimeMillis();
		}
		
		public void flush()
		{
			lastFlush = System.currentTimeMillis();
			sourceEntries.clear();
		}
		
		private int getDatabaseKey()
		{
			return databaseKey;
		}
	}

	private class ExecutionListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( CollectionEvent.Event.ADDED.equals( event.getEvent() )
					&& event.getElement().equals( manager.getExecutionManager().getCurrentExecution() ) )
			{
				reset();
			}
		}
	}
}