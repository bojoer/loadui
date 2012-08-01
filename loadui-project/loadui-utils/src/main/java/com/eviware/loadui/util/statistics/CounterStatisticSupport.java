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
package com.eviware.loadui.util.statistics;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Releasable;

public class CounterStatisticSupport implements EventHandler<CounterEvent>, Releasable
{
	private CounterHolder counterHolder;
	private final Map<String, StatisticVariable.Mutable> counters = new HashMap<>();

	public CounterStatisticSupport( CounterHolder counterHolder )
	{
		this.counterHolder = counterHolder;
	}

	public void init()
	{
		counterHolder.addEventListener( CounterEvent.class, this );
	}

	@Override
	public void release()
	{
		counterHolder.removeEventListener( CounterEvent.class, this );
	}

	public void addCounterVariable( String counterName, StatisticVariable.Mutable statisticVariable )
	{
		if( counters.put( counterName, statisticVariable ) != null )
			throw new IllegalArgumentException( "CounterStatisticSupport already contains a mapping for " + counterName );
	}

	@Override
	public void handleEvent( CounterEvent event )
	{
		String counter = event.getKey();
		if( counters.containsKey( counter ) )
		{
			counters.get( counter ).update( System.currentTimeMillis(), event.getValue() );
		}
	}
}
