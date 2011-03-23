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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.impl.statistics.ThroughputStatisticsWriter.Factory;
import com.eviware.loadui.util.MapUtils;
import com.eviware.loadui.util.statistics.store.EntryImpl;

public class ThroughputStatisticsWriterTest
{
	private ThroughputStatisticsWriter writer;

	@Before
	public void setup()
	{

		Factory factory = new ThroughputStatisticsWriter.Factory();

		StatisticVariable variableMock = mock( StatisticVariable.class );
		StatisticHolder statisticHolderMock = mock( StatisticHolder.class );
		when( variableMock.getStatisticHolder() ).thenReturn( statisticHolderMock );

		StatisticsManager statisticsManagerMock = mock( StatisticsManager.class );
		ExecutionManager executionManagerMock = mock( ExecutionManager.class );
		when( statisticsManagerMock.getExecutionManager() ).thenReturn( executionManagerMock );
		when( statisticsManagerMock.getMinimumWriteDelay() ).thenReturn( 1000L );

		writer = factory.createStatisticsWriter( statisticsManagerMock, variableMock,
				Collections.<String, Object> emptyMap() );
	}

	@Test
	public void shouldCalculateTPS()
	{
		Random random = new Random();
		writer.update( 7, random.nextInt( 1000 ) );
		writer.update( 15, random.nextInt( 1000 ) );
		writer.update( 138, random.nextInt( 1000 ) );
		writer.update( 467, random.nextInt( 1000 ) );
		writer.update( 842, random.nextInt( 1000 ) );

		Entry entry = writer.output();

		assertNotNull( entry );
		Number tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );
		assertThat( tps, is( Double.class ) );
		assertThat( ( Double )tps, is( 5.0 ) );
	}

	@Test
	public void shouldCalculateBPS()
	{
		long timestamp = System.currentTimeMillis();
		writer.update( timestamp + 7, 123 );
		writer.update( timestamp + 15, 432 );
		writer.update( timestamp + 138, 143 );
		writer.update( timestamp + 467, 214 );
		writer.update( timestamp + 842, 165 );

		Entry entry = writer.output();

		assertNotNull( entry );
		Number bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		assertThat( bps, is( Double.class ) );
		assertThat( ( Double )bps, is( 1077.0 ) );

		writer.update( timestamp + 1010, 143 );
		writer.update( timestamp + 1324, 214 );
		writer.update( timestamp + 1702, 165 );

		entry = writer.output();

		assertNotNull( entry );
		bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		assertThat( bps, is( Double.class ) );
		assertThat( ( Double )bps, is( 522.0 ) );
	}

	@Test
	public void shouldAggregateValue()
	{
		Entry e1 = new EntryImpl( 123, MapUtils.build( String.class, Number.class )
				.put( ThroughputStatisticsWriter.Stats.BPS.name(), 321 )
				.put( ThroughputStatisticsWriter.Stats.TPS.name(), 4 ).getImmutable() );

		Entry e2 = new EntryImpl( 234, MapUtils.build( String.class, Number.class )
				.put( ThroughputStatisticsWriter.Stats.BPS.name(), 562 )
				.put( ThroughputStatisticsWriter.Stats.TPS.name(), 7 ).getImmutable() );

		Entry e3 = new EntryImpl( 345, MapUtils.build( String.class, Number.class )
				.put( ThroughputStatisticsWriter.Stats.BPS.name(), 93 )
				.put( ThroughputStatisticsWriter.Stats.TPS.name(), 11 ).getImmutable() );

		Entry entry = writer.aggregate( Collections.<Entry> emptySet(), false );
		assertNull( entry );

		HashSet<Entry> entries = new HashSet<Entry>();
		entries.add( e1 );
		entry = writer.aggregate( entries, false );

		assertThat( entry, is( e1 ) );

		entries.clear();
		entries.addAll( Arrays.asList( e1, e2, e3 ) );
		entry = writer.aggregate( entries, true );

		assertNotNull( entry );

		Number bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		Number tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );

		assertThat( ( Double )bps, is( 976.0 ) );
		assertThat( ( Double )tps, is( 22.0 ) );

		entry = writer.aggregate( entries, false );

		assertNotNull( entry );

		bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );

		assertEquals( ( Double )bps, 325.33, 0.01 );
		assertEquals( ( Double )tps, 7.33, 0.01 );
	}
}
