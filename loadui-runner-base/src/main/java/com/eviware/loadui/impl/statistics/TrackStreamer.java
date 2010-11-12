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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;

/**
 * Periodically reads the current value of each Track in the current Execution,
 * and sends this data to the controller.
 * 
 * @author dain.nilsson
 */
public class TrackStreamer
{
	private final static Logger log = LoggerFactory.getLogger( TrackStreamer.class );

	private final ExecutionManager manager;
	private final ScheduledExecutorService executor;
	private final ScheduledFuture<?> future;
	private Map<String, Map<String, Number>> lastSent;

	public TrackStreamer( ScheduledExecutorService scheduledExecutorService, ExecutionManager executionManager )
	{
		manager = executionManager;
		executor = scheduledExecutorService;

		future = executor.scheduleAtFixedRate( new StreamTask(), 1, 1, TimeUnit.SECONDS );
	}

	public void release()
	{
		future.cancel( true );
	}

	private class StreamTask implements Runnable
	{
		@Override
		public void run()
		{
			Execution execution = manager.getCurrentExecution();
			if( execution != null )
			{
				Map<String, Map<String, Number>> currentData = new HashMap<String, Map<String, Number>>();
				for( String trackId : execution.getTrackIds() )
				{
					Entry entry = execution.getTrack( trackId ).getLastEntry( "local" );
					Map<String, Number> entryData = new HashMap<String, Number>();
					for( String key : entry.getNames() )
						entryData.put( key, entry.getValue( key ) );
					currentData.put( entry.getTimestamp() + ":" + trackId, entryData );
				}

				// Remove any entries that have already been sent.
				if( lastSent != null )
				{
					Map<String, Map<String, Number>> nextLastSent = new HashMap<String, Map<String, Number>>( currentData );
					currentData.keySet().removeAll( lastSent.keySet() );
					lastSent = nextLastSent;
				}
				else
				{
					lastSent = currentData;
				}

				// TODO: Send currentData
				log.debug( "Sending Track data: {}", currentData );
			}
		}
	}
}
