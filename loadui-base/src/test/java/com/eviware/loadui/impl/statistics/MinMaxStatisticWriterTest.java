package com.eviware.loadui.impl.statistics;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.BeanInjector;

public class MinMaxStatisticWriterTest
{

	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	MinMaxStatisticsWriter writer;
	StatisticsManager manager;
	double[] data = { 12, 22, 13, 4, 53, 16, 75, 48, 559, 1044 };

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
		StatisticVariable variable = holderSupport.addStatisticVariable( "Exteme" );
		writer = ( MinMaxStatisticsWriter )new MinMaxStatisticsWriter.Factory().createStatisticsWriter( manager, variable,
				Collections.<String, Object> emptyMap() );

		for( int cnt = 0; cnt < data.length; cnt++ )
			writer.update( 1, data[cnt] );
	}

	@Test
	public void checkMax()
	{
		assertEquals( 1044, writer.maximum, 0 );
	}

	@Test
	public void checkMin()
	{
		assertEquals( 4, writer.minimum, 0 );
	}

}
