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

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.EntryImpl;

public abstract class AbstractStatisticsWriter implements StatisticsWriter
{
	private final StatisticsManager manager;
	private final StatisticVariable variable;
	private final Map<String, Class<? extends Number>> trackStructure;
	private final String id;

	protected long delay;

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values )
	{
		this.manager = manager;
		this.variable = variable;
		trackStructure = values;
		id = DigestUtils.md5Hex( variable.getStatisticHolder().getId() + variable.getName() + getType() );
	}

	/**
	 * Gets the type of the StatisticsWriter, which should be unique. This can be
	 * the same as the associated StatisticsWriterFactory.getType().
	 * 
	 * @return
	 */
	protected abstract String getType();

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
	public Track getTrack()
	{
		return manager.getExecutionManager().createTrack( getId(), trackStructure );
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
		private final int time;
		private final Map<String, Number> values = new HashMap<String, Number>();

		public EntryBuilder( long timestamp )
		{
			time = ( int )( timestamp - manager.getExecutionManager().getCurrentExecution().getStartTime() );
		}

		public <T extends Number> EntryBuilder put( String name, T value )
		{
			values.put( name, value );
			return this;
		}

		public void write()
		{
			getTrack().write( new EntryImpl( time, values ), "local" );
		}
	}
}