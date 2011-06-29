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
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter.Stats;
import com.eviware.loadui.util.test.BeanInjectorMocker;

public class SampleStatisticsWriterTest
{
	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	SampleStatisticsWriter writer;
	StatisticsManager manager;
	Double[] data = { 0.3, 1.44, 2.56, 3.12, 4.44, 5.55, 6.6656, 7.6767, 8.567, 9.5675, 10.567 };
	private int size;
	private double avgSum;
	private double sumTotalSquare;
	private double stdDev;
	final Logger logger = LoggerFactory.getLogger( SampleStatisticsWriterTest.class );
	private double average;

	@Before
	public void setup() throws Exception
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

		new BeanInjectorMocker().put( StatisticsManager.class, manager );

		holderSupport = new StatisticHolderSupport( holderMock );
		StatisticVariable variable = holderSupport.addStatisticVariable( "AVG_TEST" );
		writer = ( SampleStatisticsWriter )new SampleStatisticsWriter.Factory().createStatisticsWriter( manager,
				variable, Collections.<String, Object> emptyMap() );
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
		assertEquals( stdDev, result.getValue( Stats.STD_DEV.name() ).doubleValue(), .005 );
	}

	@Test
	public void checkAverage()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( average, result.getValue( Stats.AVERAGE.name() ).doubleValue(), .005 );
	}

	@Test
	public void checkMedian()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( 5.55, result.getValue( Stats.MEDIAN.name() ).doubleValue(), .005 );
	}

	@Test
	public void checkPercentile()
	{
		calculate();
		Entry result = writer.output();
		assertEquals( 2.84, result.getValue( Stats.PERCENTILE_25TH.name() ).doubleValue(), .1 );
		assertEquals( 5.55, result.getValue( Stats.MEDIAN.name() ).doubleValue(), .1 );
		assertEquals( 8.12185, result.getValue( Stats.PERCENTILE_75TH.name() ).doubleValue(), .1 );
		assertEquals( 9.5675, result.getValue( Stats.PERCENTILE_90TH.name() ).doubleValue(), .1 );
	}

	@Test
	public void ensurePercentileOrder()
	{
		long time = System.currentTimeMillis();
		double[] values = new double[100];
		for( int i = 0; i < 100; i++ )
		{
			time += 100;
			values[i] = Math.random() * 100;
			writer.update( time, values[i] );
			if( i % 10 == 0 )
			{
				Entry entry = writer.output();
				double p25 = entry.getValue( Stats.PERCENTILE_25TH.name() ).doubleValue();
				double p50 = entry.getValue( Stats.MEDIAN.name() ).doubleValue();
				double p75 = entry.getValue( Stats.PERCENTILE_75TH.name() ).doubleValue();
				double p90 = entry.getValue( Stats.PERCENTILE_90TH.name() ).doubleValue();
				assertThat( "25th less than 50th", p25, lessThanOrEqualTo( p50 ) );
				assertThat( "50th less than 75th", p50, lessThanOrEqualTo( p75 ) );
				assertThat( "75th less than 90th", p75, lessThanOrEqualTo( p90 ) );
			}
		}
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
		assertEquals( 2.5133, result.getValue( Stats.STD_DEV.name() ).doubleValue(), 0.005 );
	}

	@Test
	public void testPercentileAggregation()
	{
		// Entry result = prepareAggregation();
		/*
		 * assertEquals( 7.25, result.getValue( Stats.PERCENTILE_25TH.name()
		 * ).doubleValue(), 0.005 ); assertEquals( 9.0, result.getValue(
		 * Stats.MEDIAN.name() ).doubleValue(), 0.005 ); assertEquals( 10.0,
		 * result.getValue( Stats.PERCENTILE_75TH.name() ).doubleValue(), 0.005 );
		 * assertEquals( 12.5, result.getValue( Stats.PERCENTILE_90TH.name()
		 * ).doubleValue(), 0.005 );
		 */
	}

	@Test
	public void testMinAggregation()
	{
		Entry result = prepareAggregation();
		assertEquals( 6, result.getValue( Stats.MIN.name() ).doubleValue(), 0.005 );
	}

	@Test
	public void testMaxAggregation()
	{
		Entry result = prepareAggregation();
		assertEquals( 17, result.getValue( Stats.MAX.name() ).doubleValue(), 0.005 );
	}

	private Entry prepareAggregation()
	{
		// Based on these three sets of samples: {{10, 8, 6}, {7, 7, 9, 17}, {12,
		// 10, 9}}
		writer.update( 123, 10 );
		writer.update( 223, 8 );
		writer.update( 323, 6 );
		Entry e1 = writer.output();

		writer.update( 1123, 7 );
		writer.update( 1223, 7 );
		writer.update( 1323, 9 );
		writer.update( 1423, 17 );
		Entry e2 = writer.output();

		writer.update( 2123, 12 );
		writer.update( 2223, 10 );
		writer.update( 2323, 9 );
		Entry e3 = writer.output();

		// HashSet<Entry> entries = new HashSet<Entry>();
		// entries.add( writer.at( 1 ).put( Stats.AVERAGE.name(), 8 ).put(
		// Stats.MEDIAN.name(), 8 )
		// .put( Stats.COUNT.name(), 3 ).put( Stats.STD_DEV.name(), 1.632993162
		// ).put( Stats.MIN.name(), 6 )
		// .put( Stats.MAX.name(), 10 ).build() );
		// entries.add( writer.at( 2 ).put( Stats.AVERAGE.name(), 10 ).put(
		// Stats.MEDIAN.name(), 11 )
		// .put( Stats.COUNT.name(), 4 ).put( Stats.STD_DEV.name(), 4.123105626
		// ).put( Stats.MIN.name(), 7 )
		// .put( Stats.MAX.name(), 17 ).build() );
		// entries.add( writer.at( 3 ).put( Stats.AVERAGE.name(), 10.3333333
		// ).put( Stats.MEDIAN.name(), 10 )
		// .put( Stats.COUNT.name(), 3 ).put( Stats.STD_DEV.name(), 1.247219129
		// ).put( Stats.MIN.name(), 9 )
		// .put( Stats.MAX.name(), 12 ).build() );

		return writer.aggregate( new HashSet<Entry>( Arrays.asList( e1, e2, e3 ) ), false );
	}
}
