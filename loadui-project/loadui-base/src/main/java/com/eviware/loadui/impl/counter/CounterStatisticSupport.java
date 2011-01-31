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
package com.eviware.loadui.impl.counter;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.MutableStatisticVariable;

public class CounterStatisticSupport implements EventHandler<CounterEvent>, Releasable
{
	private CounterHolder counterHolder;
	private MutableStatisticVariable statisticVariable;
	private String counterName;

	public CounterStatisticSupport( CounterHolder counterHolder, MutableStatisticVariable statisticVariable,
			String counterName )
	{
		this.counterHolder = counterHolder;
		this.statisticVariable = statisticVariable;
		this.counterName = counterName;
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

	@Override
	public void handleEvent( CounterEvent event )
	{
		if( event.getKey().equals( counterName ) )
			statisticVariable.update( System.currentTimeMillis(), event.getValue() );
	}
}
