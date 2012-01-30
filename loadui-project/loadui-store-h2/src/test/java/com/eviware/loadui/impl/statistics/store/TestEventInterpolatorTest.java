/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.statistics.store;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.testevents.TestEvent;

public class TestEventInterpolatorTest
{
	TestEventInterpolator interpolator;
	ExecutionManagerImpl manager;

	private static TestEvent testEvent( long timestamp )
	{
		TestEvent testEvent = mock( TestEvent.class );
		when( testEvent.getTimestamp() ).thenReturn( timestamp );

		return testEvent;
	}

	@Before
	public void setup()
	{
		manager = mock( ExecutionManagerImpl.class );
		interpolator = new TestEventInterpolator( manager );
	}

	@Test
	public void shouldAggregateToEachLevel()
	{
		@SuppressWarnings( "unchecked" )
		TestEvent.Source<TestEvent> sourceMock = mock( TestEvent.Source.class );

		interpolator.interpolate( "type", sourceMock, testEvent( 0 ) );
		interpolator.interpolate( "type", sourceMock, testEvent( 1 ) );
		interpolator.interpolate( "type", sourceMock, testEvent( 2 ) );
		verify( manager, never() ).writeTestEvent( anyString(), ( TestEvent.Source<?> )any(), anyLong(), ( byte[] )any(),
				anyInt() );

		//Should trigger aggregation to level 1.
		interpolator.interpolate( "type", sourceMock, testEvent( TestEventInterpolator.aggregateIntervals[0] + 1 ) );
		verify( manager ).writeTestEvent( anyString(), ( TestEvent.Source<?> )any(), anyLong(), ( byte[] )any(), eq( 1 ) );

		//Should trigger aggregation to level 1 and 2.
		interpolator.interpolate( "type", sourceMock, testEvent( TestEventInterpolator.aggregateIntervals[1] + 1 ) );
		verify( manager, times( 2 ) ).writeTestEvent( anyString(), ( TestEvent.Source<?> )any(), anyLong(),
				( byte[] )any(), eq( 1 ) );
		verify( manager ).writeTestEvent( anyString(), ( TestEvent.Source<?> )any(), anyLong(), ( byte[] )any(), eq( 2 ) );

		verifyNoMoreInteractions( manager );
	}
}
