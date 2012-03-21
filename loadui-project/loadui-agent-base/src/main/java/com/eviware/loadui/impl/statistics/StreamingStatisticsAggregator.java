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
package com.eviware.loadui.impl.statistics;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.store.Entry;

public class StreamingStatisticsAggregator implements StatisticsAggregator
{
	public static final Logger log = LoggerFactory.getLogger( StreamingStatisticsAggregator.class );

	private static final String STATISTICS_CHANNEL = "/" + Statistic.class.getName();

	private final MessageEndpoint endpoint;

	public StreamingStatisticsAggregator( MessageEndpoint endpoint )
	{
		this.endpoint = endpoint;
	}

	@Override
	public void addEntry( String trackId, Entry entry )
	{
		Map<String, Object> data = new HashMap<String, Object>();
		for( String key : entry.getNames() )
			data.put( key, entry.getValue( key ) );
		data.put( "_CURRENT_TIME", System.currentTimeMillis() );
		data.put( "_TIMESTAMP", entry.getTimestamp() );
		data.put( "_TRACK_ID", trackId );

		log.debug( "Sending entry: {}", entry );
		endpoint.sendMessage( STATISTICS_CHANNEL, data );
	}

	@Override
	public void addEntry( String trackId, Entry entry, String source )
	{
		throw new UnsupportedOperationException(
				"StreamingStatisticsAggregator cannot add an Entry for a specified source!" );
	}
}
