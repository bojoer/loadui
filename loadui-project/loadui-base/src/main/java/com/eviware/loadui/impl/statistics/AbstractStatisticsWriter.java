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

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values )
	{
		this.manager = manager;
		this.variable = variable;
		id = DigestUtils.md5Hex( variable.getStatisticHolder().getId() + variable.getName() + getType() );
		descriptor = new TrackDescriptorImpl( id, values );
		delay = manager.getMinimumWriteDelay();

		// TODO
		manager.getExecutionManager().registerTrackDescriptor( descriptor );
		manager.addEventListener( CollectionEvent.class, new ExecutionListener() );

		// adding execution listeners.
		manager.getExecutionManager().addExecutionListener( new ExecutionListenerAdapter()
		{
			private long delta;

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
					pauseStartedTime = System.currentTimeMillis();
					lastTimeFlushed = System.currentTimeMillis() + delta;
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
					delta = pauseStartedTime - lastTimeFlushed;
					flush();
					totalPause += System.currentTimeMillis() - pauseStartedTime;
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
					delta = 0;
					flush();
					pauseStartedTime = 0;
					totalPause = 0;
				}
			}
		} );
	}

	/**
	 * Gets the type of the StatisticsWriter, which should be unique. This can be
	 * the same as the associated StatisticsWriterFactory.getType().
	 * 
	 * @return
	 */
	protected abstract String getType();

	/**
	 * Called between Executions, letting the StatisticsWriter know that it
	 * should clear any buffers and prepare for a new Execution.
	 */
	protected abstract void reset();

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

		public void write()
		{
			ExecutionManager executionManager = manager.getExecutionManager();
			Execution currentExecution = executionManager.getCurrentExecution();

			int time = currentExecution == null ? -1
					: ( int )( timestamp - currentExecution.getStartTime() - totalPause );
			executionManager.writeEntry( getId(), new EntryImpl( time, values, true ), StatisticVariable.MAIN_SOURCE );
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