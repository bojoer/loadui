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
package com.eviware.loadui.impl.statistics;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractStatisticsWriter implements StatisticsWriter, Releasable
{
	public final static Logger log = LoggerFactory.getLogger( AbstractStatisticsWriter.class );

	public static final String DELAY = "delay";
	public static final String NAMES = "names";

	private final StatisticVariable variable;
	private final String id;
	private final TrackDescriptor descriptor;

	private final StatisticsAggregator aggregator;

	protected long delay;
	protected long lastTimeFlushed = System.currentTimeMillis();

	private long lastFlushed;

	private final Map<String, Object> config;

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values, Map<String, Object> config )
	{
		this.config = config;
		this.variable = variable;
		id = DigestUtils.md5Hex( variable.getStatisticHolder().getId() + variable.getName() + getType() );
		descriptor = new TrackDescriptorImpl( id, values );
		delay = config.containsKey( DELAY ) ? ( ( Number )config.get( DELAY ) ).longValue() : manager
				.getMinimumWriteDelay();

		// TODO
		if( LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) )
			manager.getExecutionManager().registerTrackDescriptor( descriptor );

		aggregator = BeanInjector.getBean( StatisticsAggregator.class );
	}

	protected Map<String, Object> getConfig()
	{
		return config;
	}

	@Override
	public void reset()
	{
		lastTimeFlushed = System.currentTimeMillis();
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
	public TrackDescriptor getTrackDescriptor()
	{
		return descriptor;
	}

	@Override
	public void flush()
	{
		Entry entry = output();
		if( entry != null )
		{
			if( entry.getTimestamp() == lastFlushed )
				return;

			lastFlushed = entry.getTimestamp();
			aggregator.addEntry( id, entry );
		}
	}

	@Override
	public void release()
	{
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
		private final ImmutableMap.Builder<String, Number> mapBuilder = ImmutableMap.builder();

		public EntryBuilder( long timestamp )
		{
			this.timestamp = timestamp;
		}

		public <T extends Number> EntryBuilder put( String name, T value )
		{
			mapBuilder.put( name, value );
			return this;
		}

		public long getTimestamp()
		{
			return timestamp;
		}

		public Entry build()
		{
			return new EntryImpl( timestamp, mapBuilder.build() );
		}
	}
}