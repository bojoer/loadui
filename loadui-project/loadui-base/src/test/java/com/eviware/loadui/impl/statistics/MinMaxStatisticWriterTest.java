package com.eviware.loadui.impl.statistics;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter.Stats;
import com.eviware.loadui.util.BeanInjector;

public class MinMaxStatisticWriterTest
{

	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	MinMaxStatisticsWriter writer;
	StatisticsManager manager;
	double[] data = { 12, 22, 13, -411, 53, 16, 75, 48, 59, 1044, 768, 4711, 0, 85, 99, 7 };

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
		AddressableRegistry addressableRegistryMock = mock( AddressableRegistry.class );
		when( appContext.getBean( "addressableRegistry", AddressableRegistry.class ) ).thenReturn(
				addressableRegistryMock );

		new BeanInjector().setApplicationContext( appContext );

		holderSupport = new StatisticHolderSupport( holderMock );
		StatisticVariable variable = holderSupport.addStatisticVariable( "Extreme" );
		writer = ( MinMaxStatisticsWriter )new MinMaxStatisticsWriter.Factory().createStatisticsWriter( manager, variable,
				Collections.<String, Object> emptyMap() );

		for( int cnt = 0; cnt < data.length/2; cnt++ )
			writer.update( 1, data[cnt] );
	}

	@Test
	public void checkMax()
	{
		assertEquals( 75, writer.maximum, 0 );
	}

	@Test
	public void checkMin()
	{
		assertEquals( -411, writer.minimum, 0 );
	}
	
	@Test
	public void checkMinAfterFlush()
	{
		Entry result = prepareAggregation();
		assertEquals( -70, result.getValue( MinMaxStatisticsWriter.Stats.MIN.name() ).doubleValue(), 0.005 );
	}
	
	@Test
	public void checkMaxAfterFlush()
	{
		Entry result = prepareAggregation();
		assertEquals( 8.01, result.getValue( MinMaxStatisticsWriter.Stats.MAX.name() ).doubleValue(), 0.005 );
	}

	private Entry prepareAggregation()
	{
		HashSet<Entry> entries = new HashSet<Entry>();
		entries.add( writer.at( 1 ).put( MinMaxStatisticsWriter.Stats.MIN.name(), 8 ).put( MinMaxStatisticsWriter.Stats.MAX.name(), 8 ).build( false ) );
		entries.add( writer.at( 2 ).put( MinMaxStatisticsWriter.Stats.MIN.name(), -56.6 ).put( MinMaxStatisticsWriter.Stats.MAX.name(), 8.01 ).build( false ) );
		entries.add( writer.at( 3 ).put( MinMaxStatisticsWriter.Stats.MIN.name(), -70 ).put( MinMaxStatisticsWriter.Stats.MAX.name(), 5 ).build( false ) );
		
		return writer.aggregate( entries );
	}
}
