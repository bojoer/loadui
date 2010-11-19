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

			int time = currentExecution == null ? -1 : ( int )( timestamp - currentExecution.getStartTime() );
			executionManager.writeEntry( getId(), new EntryImpl( time, values, true ), "local" );
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