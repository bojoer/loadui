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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Source;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.api.testevents.TestEventTypeDescriptor;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.eviware.loadui.util.testevents.AbstractTestEvent;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ExecutionImplTest
{

	private static final String EXECUTION_NAME = "executionTestExecution";
	private static final String EXECUTION_NAME_2 = "executionTestExecution2";

	H2ExecutionManager h2;
	ExecutionImpl execution;
	ExecutionImpl currentExecution;
	TestEventRegistry testEventRegistry;

	@Before
	public void initialize()
	{
		new BeanInjectorMocker().put( TestEventRegistry.class, testEventRegistry = mock( TestEventRegistry.class ) );
		System.setProperty( LoadUI.LOADUI_HOME, "target" );

		h2 = new H2ExecutionManager( testEventRegistry );
		execution = ( ExecutionImpl )h2.startExecution( EXECUTION_NAME, 10 );
		// unload and load execution
		h2.release();
		execution = h2.getExecution( EXECUTION_NAME );
		currentExecution = ( ExecutionImpl )h2.startExecution( EXECUTION_NAME_2, 20 );
	}

	@Test
	public void testArchive()
	{
		assertFalse( execution.isArchived() );
		execution.archive();
		assertTrue( execution.isArchived() );
	}

	@Test
	public void testSetLabel()
	{
		execution.setLabel( "testLabel" );
		assertTrue( execution.getLabel().equals( "testLabel" ) );
		execution.setLabel( "" );
		assertTrue( execution.getLabel().equals( "" ) );
	}

	@Test
	public void testSetLength()
	{
		assertTrue( execution.getLength() == 0 );
		execution.updateLength( 10 );
		execution.flushLength();
		h2.release();
		execution = h2.getExecution( EXECUTION_NAME );
		assertTrue( execution.getLength() == 10 );
		h2.release();
		execution = h2.getExecution( EXECUTION_NAME );
		execution.updateLength( 20 );
		execution.flushLength();
		assertTrue( execution.getLength() == 20 );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testDelete()
	{
		assertNotNull( h2.getExecution( EXECUTION_NAME ) );
		execution.delete();
		assertNull( h2.getExecution( EXECUTION_NAME ) );
	}

	@Test
	public void testNoTestEvents()
	{
		assertThat( execution.getEventTypes(), equalTo( Collections.<TestEventTypeDescriptor> emptySet() ) );
		assertThat( execution.getTestEventCount(), is( 0 ) );

		assertThat( Iterables.size( execution.getTestEvents( 0, false ) ), is( 0 ) );
		assertThat( Iterables.size( execution.getTestEventRange( 0, Long.MAX_VALUE ) ), is( 0 ) );
	}

	@Test
	public void testAddingTestEvents()
	{
		assertThat( h2.getCurrentExecution(), sameInstance( ( Execution )currentExecution ) );

		MyTestEventFactory factory = new MyTestEventFactory();
		when( testEventRegistry.lookupFactory( factory.getType() ) ).thenReturn( factory );
		MyTestEventSource source = new MyTestEventSource();

		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 37, new byte[0], 0 );
		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 137, new byte[0], 0 );
		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 68, new byte[0], 0 );

		assertThat( Iterables.getOnlyElement( currentExecution.getEventTypes() ).getLabel(), is( factory.getLabel() ) );

		assertThat( currentExecution.getTestEventCount(), is( 3 ) );

		List<Long> timestamps = Lists.newArrayList( Iterables.transform( currentExecution.getTestEvents( 0, false ),
				new Function<TestEvent.Entry, Long>()
				{
					@Override
					public Long apply( TestEvent.Entry input )
					{
						return input.getTestEvent().getTimestamp();
					}
				} ) );

		List<Long> timestampsInOrder = Lists.newArrayList( timestamps );
		Collections.sort( timestampsInOrder );

		assertThat( timestamps, is( timestampsInOrder ) );

		assertThat( Iterables.size( currentExecution.getTestEvents( 0, false ) ), is( 3 ) );
		assertThat( Iterables.size( currentExecution.getTestEventRange( 68, 68 ) ), is( 1 ) );
	}

	@Test
	public void testTraversingBackwards()
	{
		MyTestEventFactory factory = new MyTestEventFactory();
		when( testEventRegistry.lookupFactory( factory.getType() ) ).thenReturn( factory );
		MyTestEventSource source = new MyTestEventSource();

		long time = currentExecution.getStartTime();
		for( int i = 1000; i > 0; i-- )
		{
			h2.writeTestEvent( factory.getLabel(), source, time++ , new byte[0], 0 );
		}

		assertThat( Iterables.size( currentExecution.getTestEvents( 0, false ) ), is( 1000 ) );

		time -= currentExecution.getStartTime();
		int count = 0;

		Iterator<TestEvent.Entry> iterator = currentExecution.getTestEvents( 999, true ).iterator();
		while( iterator.hasNext() )
		{
			TestEvent.Entry entry = iterator.next();
			assertThat( "At index: " + count++ , entry.getTestEvent().getTimestamp(), is( --time ) );
		}
	}

	@Test
	public void testGetEventTypes()
	{
		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source = mock( TestEvent.Source.class );
		when( source.getLabel() ).thenReturn( "sample-source-label-1" );
		when( source.getHash() ).thenReturn( "-sample-source-hash-1" );
		when( source.getData() ).thenReturn( "source-data".getBytes() );
		when( source.getType() ).thenReturn( TestEvent.class );

		for( int i = 0; i < 20; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source, System.currentTimeMillis(), "event-data".getBytes(), 0 );
		}

		Set<TestEventTypeDescriptor> types = currentExecution.getEventTypes();
		assertTrue( types.size() == 1 );
		for( TestEventTypeDescriptor t : types )
		{
			assertTrue( t.getTestEventSources().size() == 1 );
		}

		List<String> typeNames = Lists.newArrayList( Iterables.transform( types,
				new Function<TestEventTypeDescriptor, String>()
				{
					@Override
					public String apply( TestEventTypeDescriptor type )
					{
						return type.getLabel();
					}
				} ) );
		assertTrue( typeNames.contains( "test-event-type-label" ) );
	}

	@Test
	public void testGetTestEventCount()
	{
		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source = mock( TestEvent.Source.class );
		when( source.getLabel() ).thenReturn( "sample-source-label-1" );
		when( source.getHash() ).thenReturn( "-sample-source-hash-1" );
		when( source.getData() ).thenReturn( "source-data".getBytes() );
		when( source.getType() ).thenReturn( TestEvent.class );

		for( int i = 0; i < 98; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source, System.currentTimeMillis(), "event-data".getBytes(), 0 );
		}

		assertThat( currentExecution.getTestEventCount(), is( 98 ) );

		for( int i = 0; i < 198; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source, System.currentTimeMillis(), "event-data".getBytes(), 0 );
		}
		assertThat( currentExecution.getTestEventCount(), is( 198 + 98 ) );
	}

	@Test
	public void testGetTestEventRange()
	{
		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source = mock( TestEvent.Source.class );
		when( source.getLabel() ).thenReturn( "sample-source-label-1" );
		when( source.getHash() ).thenReturn( "-sample-source-hash-1" );
		when( source.getData() ).thenReturn( "source-data".getBytes() );
		when( source.getType() ).thenReturn( TestEvent.class );

		long start = currentExecution.getStartTime();
		for( int i = 0; i < 100; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source, start + i * 1000, "event-data".getBytes(), 0 );
		}

		assertThat( Iterables.size( currentExecution.getTestEventRange( 0, 99 * 1000 ) ), is( 100 ) );
		assertThat( Iterables.size( currentExecution.getTestEventRange( 0, 49 * 1000 ) ), is( 50 ) );
		assertThat( Iterables.size( currentExecution.getTestEventRange( 0, 49 * 1000 - 1 ) ), is( 49 ) );
		assertThat( Iterables.size( currentExecution.getTestEventRange( 1, 49 * 1000 ) ), is( 49 ) );
		assertThat( Iterables.size( currentExecution.getTestEventRange( 1, 49 * 1000 - 1 ) ), is( 48 ) );
	}

	@Test
	public void testGetTestEvents()
	{
		@SuppressWarnings( "unchecked" )
		Source<TestEvent> source = mock( TestEvent.Source.class );
		when( source.getLabel() ).thenReturn( "sample-source-label-1" );
		when( source.getHash() ).thenReturn( "-sample-source-hash-1" );
		when( source.getData() ).thenReturn( "source-data".getBytes() );
		when( source.getType() ).thenReturn( TestEvent.class );

		long start = System.currentTimeMillis();
		for( int i = 0; i < 100; i++ )
		{
			h2.writeTestEvent( "test-event-type-label", source, start + i * 1000, "event-data".getBytes(), 0 );
		}

		assertThat( Iterables.size( currentExecution.getTestEvents( 0, false ) ), is( 100 ) );
		assertThat( Iterables.size( currentExecution.getTestEvents( 99, true ) ), is( 100 ) );
	}

	@After
	public void release()
	{
		try
		{
			h2.delete( EXECUTION_NAME );
		}
		catch( IllegalArgumentException e )
		{
		}

		try
		{
			h2.delete( EXECUTION_NAME_2 );
		}
		catch( IllegalArgumentException e )
		{
		}

		h2.release();
	}

	private class MyTestEvent extends AbstractTestEvent
	{
		public MyTestEvent( long timestamp )
		{
			super( timestamp );
		}
	}

	private class MyTestEventSource implements TestEvent.Source<MyTestEvent>
	{
		@Override
		public String getLabel()
		{
			return "My TestEvent Source";
		}

		@Override
		public Class<MyTestEvent> getType()
		{
			return MyTestEvent.class;
		}

		@Override
		public byte[] getData()
		{
			return new byte[0];
		}

		@Override
		public String getHash()
		{
			return "unique hash";
		}
	}

	private class MyTestEventFactory implements TestEvent.Factory<MyTestEvent>
	{
		@Override
		public String getLabel()
		{
			return "My Test Event";
		}

		@Override
		public Class<MyTestEvent> getType()
		{
			return MyTestEvent.class;
		}

		@Override
		public MyTestEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			return new MyTestEvent( timestamp );
		}

		@Override
		public byte[] getDataForTestEvent( MyTestEvent testEvent )
		{
			return new byte[0];
		}
	}
}
