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

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.impl.statistics.AverageStatisticWriter.Stats;
import com.eviware.loadui.util.BeanInjector;

public class AverageStatisticWriterTest
{
	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	AverageStatisticWriter writer;
	StatisticsManager manager;
	Double[] data = { 1.44, 2.56, 3.12, 4.44, 5.55, 6.6656, 7.6767, 8.567, 9.5675, 10.567 };
	private int size;
	private double avgSum;
	private double sumTotalSquare;
	private double stdDev;
	final Logger logger = LoggerFactory.getLogger( AverageStatisticWriterTest.class );
	private double average;

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
		when( manager.getMinimumWriteDelay() ).thenReturn( 1000L );

		ApplicationContext appContext = mock( ApplicationContext.class );
		when( appContext.getBean( "statisticsManager", StatisticsManager.class ) ).thenReturn( manager );
		AddressableRegistry addressableRegistryMock = mock( AddressableRegistry.class );
		when( appContext.getBean( "addressableRegistry", AddressableRegistry.class ) ).thenReturn(
				addressableRegistryMock );

		new BeanInjector().setApplicationContext( appContext );
		holderSupport = new StatisticHolderSupport( holderMock );
		StatisticVariable variable = holderSupport.addStatisticVariable( "AVG_TEST" );
		writer = ( AverageStatisticWriter )new AverageStatisticWriter.Factory()
				.createStatisticsWriter( manager, variable );
	}

	private void calculate()
	{
		size = data.length;
		for( int cnt = 0; cnt < data.length; cnt++ )
		{
			avgSum += data[cnt];
			writer.update( System.currentTimeMillis(), data[cnt] );
			try
			{
				Thread.sleep( 10 );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		sumTotalSquare = 0;
		average = avgSum / size;
		for( double val : data )
		{
			sumTotalSquare += Math.pow( val - average, 2 );
		}
		stdDev = Math.sqrt( sumTotalSquare / size );
	}

	@Test
	public void checkCounter()
	{
		calculate();
		assertEquals( size, writer.count );
	}

	@Test
	public void checkAverageSum()
	{
		calculate();
		assertEquals( avgSum, writer.sum, .5 );
	}

	@Test
	public void checkStdDev()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( stdDev, result.getValue( Stats.STD_DEV.name() ).doubleValue() , .005 );
	}

	@Test
	public void checkAverage()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( average, result.getValue( Stats.AVERAGE.name() ).doubleValue() , .005 );
	}
	
	@Test
	public void checkMedian()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( 6.1078, result.getValue( Stats.MEDIAN.name() ).doubleValue() , .005 );
	}
	
	@Test
	public void checkPercentile()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( 10.46705, result.getValue( Stats.PERCENTILE_90TH.name() ).doubleValue() , .1 );
	}
	
	/* Test aggregations */
	@Test
	public void testAverageAggregation()
	{
		Entry result = prepareAggregation();
		assertEquals( 9.5, result.getValue( Stats.AVERAGE.name() ).doubleValue(), 0.005 );
	}
	
	@Test
	public void testStdDevAggregation()
	{
		Entry result = prepareAggregation();
		assertEquals( 3.008322, result.getValue( Stats.STD_DEV.name() ).doubleValue(), 0.005 );
	}
	
	@Test
	public void testMedianAggregation()
	{
		Entry result = prepareAggregation();
		assertEquals( 9.8, result.getValue( Stats.MEDIAN.name() ).doubleValue(), 0.005 );
	}
	
	private Entry prepareAggregation()
	{
		// Based on these three sets of samples: {{10, 8, 6}, {7, 7, 9, 17}, {12, 10, 9}}
		HashSet<Entry> entries = new HashSet<Entry>();
		entries.add( writer.at( 1 ).put( Stats.AVERAGE.name(), 8 ).put( Stats.MEDIAN.name(), 8 ).put( Stats.COUNT.name(), 3 ).put( Stats.STD_DEV.name(), 1.632993162 ).build(false) );
		entries.add( writer.at( 2 ).put( Stats.AVERAGE.name(), 10 ).put( Stats.MEDIAN.name(), 11 ).put( Stats.COUNT.name(), 4 ).put( Stats.STD_DEV.name(), 4.123105626 ).build(false) );
		entries.add( writer.at( 3 ).put( Stats.AVERAGE.name(), 10.3333333 ).put( Stats.MEDIAN.name(), 10 ).put( Stats.COUNT.name(), 3 ).put( Stats.STD_DEV.name(), 1.247219129 ).build(false) );
		
		return writer.aggregate( entries );
	}

}
