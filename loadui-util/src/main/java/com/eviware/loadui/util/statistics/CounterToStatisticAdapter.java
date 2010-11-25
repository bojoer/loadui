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
package com.eviware.loadui.util.statistics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.statistics.MutableStatisticVariable;
import com.eviware.loadui.util.BeanInjector;

/**
 * Polls a Counter periodically, plotting its values in a StatisticVariable.
 * 
 * @author dain.nilsson
 */
public class CounterToStatisticAdapter
{
	private final Counter counter;
	private final MutableStatisticVariable statisticVariable;

	public CounterToStatisticAdapter( Counter counter, MutableStatisticVariable statisticVariable )
	{
		this( counter, statisticVariable, BeanInjector.getBean( ScheduledExecutorService.class ) );
	}

	public CounterToStatisticAdapter( Counter counter, MutableStatisticVariable statisticVariable,
			ScheduledExecutorService scheduledExecutor )
	{
		this.counter = counter;
		this.statisticVariable = statisticVariable;

		scheduledExecutor.scheduleAtFixedRate( new PollRunnable(), 1, 1, TimeUnit.SECONDS );
	}

	private class PollRunnable implements Runnable
	{
		long lastValue = -1;

		@Override
		public void run()
		{
			long currentValue = counter.get();
			if( lastValue != currentValue )
			{
				statisticVariable.update( System.currentTimeMillis(), currentValue );
				lastValue = currentValue;
			}
		}
	}
}
