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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.TestEventRegistry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventTypeDescriptor;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.eviware.loadui.util.testevents.AbstractTestEvent;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ExecutionImplTest
{

	private static final String EXECUTION_NAME = "executionTestExecution";
	private static final String EXECUTION_NAME_2 = "executionTestExecution2";

	H2ExecutionManager h2;
	ExecutionImpl execution;
	ExecutionImpl currentExecution;
	TestEventRegistryImpl testEventRegistry;

	@Before
	public void initialize()
	{
		new BeanInjectorMocker().put( TestEventRegistry.class, testEventRegistry = new TestEventRegistryImpl() );
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
		testEventRegistry.factoryAdded( factory, ImmutableMap.<String, String> of() );
		MyTestEventSource source = new MyTestEventSource();

		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 37, new byte[0] );
		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 137, new byte[0] );
		h2.writeTestEvent( factory.getLabel(), source, currentExecution.getStartTime() + 68, new byte[0] );

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
