/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.impl.statistics.VariableStatisticsWriter.Factory;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class VariableStatisticsWriterTest
{
	private VariableStatisticsWriter writer;
	private final LinkedList<Entry> flushedEntries = Lists.newLinkedList();

	@Before
	public void setup()
	{
		Factory factory = new VariableStatisticsWriter.Factory();

		StatisticVariable variableMock = mock( StatisticVariable.class );
		StatisticHolder statisticHolderMock = mock( StatisticHolder.class );
		when( variableMock.getStatisticHolder() ).thenReturn( statisticHolderMock );

		StatisticsManager statisticsManagerMock = mock( StatisticsManager.class );
		ExecutionManager executionManagerMock = mock( ExecutionManager.class );
		when( statisticsManagerMock.getExecutionManager() ).thenReturn( executionManagerMock );
		when( statisticsManagerMock.getMinimumWriteDelay() ).thenReturn( 1000L );

		StatisticsAggregator aggregator = mock( StatisticsAggregator.class );
		new BeanInjectorMocker( ImmutableMap.<Class<?>, Object> of( StatisticsAggregator.class, aggregator ) );
		doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				flushedEntries.push( ( Entry )invocation.getArguments()[1] );
				return null;
			}
		} ).when( aggregator ).addEntry( anyString(), ( Entry )anyObject() );

		writer = factory.createStatisticsWriter( statisticsManagerMock, variableMock,
				Collections.<String, Object> emptyMap() );
	}

	@Test
	public void shouldFlushMultipleEntriesCorrectly() throws InterruptedException
	{
		writer.update( 0, 50 );
		Thread.sleep( 250 );
		writer.update( 250, 100 );
		Thread.sleep( 1250 );
		writer.update( 1500, 100 );

		Entry entry = writer.output();

		assertNotNull( entry );
		Number value = entry.getValue( VariableStatisticsWriter.Stats.VALUE.name() );
		assertThat( value, instanceOf( Double.class ) );
		assertThat( ( Double )value, is( 100.0 ) );

		Entry e = flushedEntries.pop();
		assertThat( e.getTimestamp(), is( 1000L ) );
		assertEquals( 87.5, e.getValue( VariableStatisticsWriter.Stats.VALUE.name() ) );
	}
}
