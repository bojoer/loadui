package com.eviware.loadui.impl.statistics;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;

public class MinMaxStatisticWriterTest
{

	StatisticHolder holderMock;
	StatisticHolderSupport holderSupport;
	MinMaxStatisticWriter writer;
	StatisticsManager manager;
	double[] data = { 12, 22, 13, 4, 53, 16, 75, 48, 559, 1044 };

	@Before
	public void setUp() throws Exception
	{
		holderMock = mock( StatisticHolder.class );
		manager = mock( StatisticsManager.class );
		holderSupport = new StatisticHolderSupport( holderMock );
		StatisticVariable variable = holderSupport.addStatisticVariable( "Exteme" );
		writer = ( MinMaxStatisticWriter )new MinMaxStatisticWriter.Factory().createStatisticsWriter( manager, variable );

		for( int cnt = 0; cnt < data.length; cnt++ )
			writer.update( 1, data[cnt] );
	}
	
	@Test
	public void checkMax() {
		assertEquals( 1044, writer.maximum, 0 );
	}
	
	@Test
	public void checkMin() {
		assertEquals( 4, writer.minimum, 0 );
	}

}
