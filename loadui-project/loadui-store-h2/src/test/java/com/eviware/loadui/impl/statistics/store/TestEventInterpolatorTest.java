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

	@SuppressWarnings( "rawtypes" )
	private static TestEvent testEvent( long timestamp )
	{
		TestEvent testEvent = mock( TestEvent.class );
		when( testEvent.getTimestamp() ).thenReturn( timestamp );
		when( ( Class )testEvent.getType() ).thenReturn( TestEventA.class );

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
		verify( manager, never() ).writeTestEvent( eq( "type" ), eq( sourceMock ), anyLong(), ( byte[] )any(), anyInt() );

		//Should trigger aggregation to level 1.
		interpolator.interpolate( "type", sourceMock, testEvent( TestEventInterpolator.aggregateIntervals[0] + 1 ) );
		verify( manager ).writeTestEvent( eq( "type" ), eq( sourceMock ), anyLong(), eq( InterpolatedTestEvent.strong ),
				eq( 1 ) );

		//Should trigger aggregation to level 1 and 2.
		interpolator.interpolate( "type", sourceMock, testEvent( TestEventInterpolator.aggregateIntervals[1] + 1 ) );
		verify( manager, times( 1 ) ).writeTestEvent( eq( "type" ), eq( sourceMock ), anyLong(),
				eq( InterpolatedTestEvent.notStrong ), eq( 1 ) );
		verify( manager ).writeTestEvent( eq( "type" ), eq( sourceMock ), anyLong(), eq( InterpolatedTestEvent.strong ),
				eq( 2 ) );

		verifyNoMoreInteractions( manager );
	}

	@Test
	public void shouldSeparateBetweenSources()
	{
		@SuppressWarnings( "unchecked" )
		TestEvent.Source<TestEvent> sourceMock1 = mock( TestEvent.Source.class );
		@SuppressWarnings( "unchecked" )
		TestEvent.Source<TestEvent> sourceMock2 = mock( TestEvent.Source.class );

		//Test aggregation with multiple sources.
		interpolator.interpolate( "type", sourceMock1, testEvent( 0 ) );
		interpolator.interpolate( "type", sourceMock2, testEvent( TestEventInterpolator.aggregateIntervals[0] - 1 ) );
		interpolator.flush();

		for( int i = 1; i <= TestEventInterpolator.aggregateIntervals.length; i++ )
		{
			verify( manager, times( 2 ) ).writeTestEvent( eq( "type" ), ( TestEvent.Source<?> )any(), anyLong(),
					eq( InterpolatedTestEvent.notStrong ), eq( i ) );
		}

		//Test aggregation with single source.
		interpolator.interpolate( "type", sourceMock1, testEvent( 0 ) );
		interpolator.interpolate( "type", sourceMock1, testEvent( TestEventInterpolator.aggregateIntervals[0] - 1 ) );
		interpolator.flush();

		for( int i = 1; i <= TestEventInterpolator.aggregateIntervals.length; i++ )
		{
			verify( manager ).writeTestEvent( eq( "type" ), eq( sourceMock1 ), anyLong(),
					eq( InterpolatedTestEvent.strong ), eq( i ) );
		}
	}

	@Test
	@SuppressWarnings( "rawtypes" )
	public void shouldSeparateBetweenTypes()
	{
		@SuppressWarnings( "unchecked" )
		TestEvent.Source<TestEvent> sourceMock = mock( TestEvent.Source.class );

		//Test aggregation with multiple types.
		interpolator.interpolate( "type1", sourceMock, testEvent( 0 ) );

		TestEvent testEvent2 = testEvent( TestEventInterpolator.aggregateIntervals[0] - 1 );
		when( ( Class )testEvent2.getType() ).thenReturn( TestEventB.class );
		interpolator.interpolate( "type2", sourceMock, testEvent2 );
		interpolator.flush();

		for( int i = 1; i <= TestEventInterpolator.aggregateIntervals.length; i++ )
		{
			verify( manager ).writeTestEvent( eq( "type1" ), eq( sourceMock ), anyLong(),
					eq( InterpolatedTestEvent.notStrong ), eq( i ) );
			verify( manager ).writeTestEvent( eq( "type2" ), eq( sourceMock ), anyLong(),
					eq( InterpolatedTestEvent.notStrong ), eq( i ) );
		}
	}

	private static interface TestEventA extends TestEvent
	{
	}

	private static interface TestEventB extends TestEvent
	{
	}
}
