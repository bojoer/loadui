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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.BeanInjector;

public class AverageStatisticWriterTest
{
	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	AverageStatisticWriter writer;
	StatisticsManager manager;
	long[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	@Before
	public void setUp() throws Exception
	{
		holderMock = mock( StatisticHolder.class );
		manager = mock( StatisticsManager.class );
		ExecutionManager executionManagerMock = mock( ExecutionManager.class );
		Execution executionMock = mock( Execution.class );
		Track trackMock = mock( Track.class );
		when( executionManagerMock.getCurrentExecution() ).thenReturn( executionMock );
		when( executionManagerMock.getTrack( anyString() ) ).thenReturn( trackMock );
		when( manager.getExecutionManager() ).thenReturn( executionManagerMock );

		ApplicationContext appContext = mock( ApplicationContext.class );
		when( appContext.getBean( "statisticsManager", StatisticsManager.class ) ).thenReturn( manager );

		new BeanInjector().setApplicationContext( appContext );
		holderSupport = new StatisticHolderSupport( holderMock );
		StatisticVariable variable = holderSupport.addStatisticVariable( "AVG_TEST" );
		writer = ( AverageStatisticWriter )new AverageStatisticWriter.Factory()
				.createStatisticsWriter( manager, variable );

		for( int cnt = 0; cnt < data.length; cnt++ )
			writer.update( 1, data[cnt] );
	}

	@Test
	public void checkCounter()
	{
		assertEquals( 10, writer.avgCnt );
	}

	@Test
	public void checkAverageSum()
	{
		assertEquals( 55, writer.avgSum );
	}

	@Test
	public void checkSquareSum()
	{
		assertEquals( 4917.0, writer.sumTotalSquare, 0 );
	}

	@Test
	public void checkStdDev()
	{
		assertEquals( 491.7, writer.stdDev, 0 );
	}

}
