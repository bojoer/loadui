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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Source;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventData;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventSourceConfig;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class H2ExecutionManagerTest
{
	private static final String COLUMN_1 = "table";
	private static final String COLUMN_2 = "a column with spaces";
	private static final String COLUMN_3 = "Column@With #\"Characters";
	private static final String COLUMN_4 = "12 3 45";

	H2ExecutionManager h2;

	@Before
	public void initialize()
	{
		new BeanInjectorMocker();
		System.setProperty( LoadUI.LOADUI_HOME, "target" );
		h2 = new H2ExecutionManager( mock( TestEventRegistry.class ) );
	}

	@Test
	public void testStartExecution()
	{
		long time = System.currentTimeMillis();
		Execution e = h2.startExecution( "test1", time );
		assertEquals( time, e.getStartTime() );

		try
		{
			h2.startExecution( "test1", time );
			fail( "Should throw IllegalArgumentException when execution was already started." );
		}
		catch( IllegalArgumentException ex )
		{

		}
		h2.delete( "test1" );
	}

	@Test
	public void testGetCurrentExecution()
	{
		long time = System.currentTimeMillis();
		Execution e = h2.startExecution( "test2", time );
		assertEquals( e, h2.getCurrentExecution() );
		h2.delete( "test2" );
	}

	@Test
	public void testGetExecutionNames()
	{
		h2.startExecution( "test1", 10 );
		h2.startExecution( "test2", 20 );
		h2.startExecution( "test3", 30 );
		h2.startExecution( "test4", 40 );
		h2.startExecution( "test5", 50 );
		assertNotNull( h2.getExecution( "test1" ) );
		assertNotNull( h2.getExecution( "test2" ) );
		assertNotNull( h2.getExecution( "test3" ) );
		assertNotNull( h2.getExecution( "test4" ) );
		assertNotNull( h2.getExecution( "test5" ) );
		h2.delete( "test1" );
		h2.delete( "test2" );
		h2.delete( "test3" );
		h2.delete( "test4" );
		h2.delete( "test5" );
	}

	@Test
	public void testGetExecution()
	{
		h2.startExecution( "test1", 10 );

		// add descriptor
		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( COLUMN_1, Long.class );
		types.put( COLUMN_2, Long.class );
		types.put( COLUMN_3, Integer.class );
		types.put( COLUMN_4, Double.class );
		TrackDescriptorImpl td = new TrackDescriptorImpl( "t1", types, null );
		h2.registerTrackDescriptor( td );

		// release to invoke execution loading
		h2.release();
		h2.getExecution( "test1" );
		assertTrue( h2.getExecution( "test1" ).getStartTime() == 10 );

		try
		{
			h2.getExecution( "testX" );
			fail( "Should throw IllegalArgumentException when non existing execution is requested." );
		}
		catch( IllegalArgumentException e )
		{

		}
		h2.delete( "test1" );
	}

	@Test
	public void testGetTrack()
	{
		try
		{
			h2.getTrack( "t1" );
			fail( "Should throw IllegalArgumentException when no execution is started." );
		}
		catch( IllegalArgumentException ex )
		{

		}

		h2.startExecution( "test1", 10 );

		try
		{
			h2.getTrack( "t1" );
			fail( "Should throw IllegalArgumentException when track desriptor does not exist." );
		}
		catch( IllegalArgumentException ex )
		{

		}

		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( COLUMN_1, Long.class );
		types.put( COLUMN_2, Long.class );
		types.put( COLUMN_3, Integer.class );
		types.put( COLUMN_4, Double.class );

		Map<String, Number> values = new HashMap<String, Number>();
		values.put( COLUMN_1, 1 );
		values.put( COLUMN_2, 2 );
		values.put( COLUMN_3, 3 );
		values.put( COLUMN_4, 4 );

		TrackDescriptorImpl td = new TrackDescriptorImpl( "t1", types, null );
		h2.registerTrackDescriptor( td );
		Track t = h2.getTrack( "t1" );

		EntryImpl entry = new EntryImpl( ( int )( System.currentTimeMillis() / 10000 ), values );
		h2.writeEntry( t.getId(), entry, "local1" );

		h2.delete( "test1" );
	}

	@Test
	public void testWriteTestEvent()
	{
		Execution e = h2.startExecution( "test event sample execution", System.currentTimeMillis() );

		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < 10000; i++ )
			sb.append( "large-amount-of-data-" );
		byte[] data = sb.toString().getBytes();

		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source1 = mock( TestEvent.Source.class );
		when( source1.getLabel() ).thenReturn( "sample-source-label-1" );
		when( source1.getHash() ).thenReturn( "-sample-source-hash-1" );
		when( source1.getData() ).thenReturn( data );
		when( source1.getType() ).thenReturn( TestEvent.class );

		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source2 = mock( TestEvent.Source.class );
		when( source2.getLabel() ).thenReturn( "sample-source-label-2" );
		when( source2.getHash() ).thenReturn( "-sample-source-hash-2" );
		when( source2.getData() ).thenReturn( data );
		when( source2.getType() ).thenReturn( TestEvent.class );

		for( int i = 0; i < 20; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source1, System.currentTimeMillis(), data, 0 );
			h2.writeTestEvent( "test-event-type-label", source2, System.currentTimeMillis(), data, 0 );
		}

		assertTrue( h2.getTestEventTypes( e.getId() ).size() == 1 );
		assertTrue( h2.getTestEventCount( e.getId(), new ArrayList<TestEventSourceConfig>() ) == 40 );

		Iterable<TestEventData> result = h2.readTestEvents( e.getId(), 0, 19, new ArrayList<TestEventSourceConfig>() );
		List<TestEventData> source2List = Lists.newArrayList( Iterables.filter( result, new Predicate<TestEventData>()
		{
			@Override
			public boolean apply( TestEventData t )
			{
				return t.getTestEventSourceConfig().getLabel().equals( "sample-source-label-2" );
			}
		} ) );
		assertTrue( source2List.size() == 9 );

		List<TestEventData> source1List = Lists.newArrayList( Iterables.filter( result, new Predicate<TestEventData>()
		{
			@Override
			public boolean apply( TestEventData t )
			{
				return t.getTestEventSourceConfig().getLabel().equals( "sample-source-label-1" );
			}
		} ) );
		assertTrue( source1List.size() == 10 );

		h2.delete( e.getId() );
	}

	@Test
	public void testRelease()
	{
		h2.startExecution( "test1", 10 );

		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( COLUMN_1, Long.class );
		types.put( COLUMN_2, Long.class );
		types.put( COLUMN_3, Integer.class );
		types.put( COLUMN_4, Double.class );

		Map<String, Number> values = new HashMap<String, Number>();
		values.put( COLUMN_1, 1 );
		values.put( COLUMN_2, 2 );
		values.put( COLUMN_3, 3 );
		values.put( COLUMN_4, 4 );

		TrackDescriptorImpl td = new TrackDescriptorImpl( "t1", types, null );
		h2.registerTrackDescriptor( td );
		Track t = h2.getTrack( "t1" );

		EntryImpl entry = new EntryImpl( ( int )( System.currentTimeMillis() / 10000 ), values );
		h2.writeEntry( t.getId(), entry, "local1" );
		h2.release();

		h2.delete( "test1" );
	}

}
