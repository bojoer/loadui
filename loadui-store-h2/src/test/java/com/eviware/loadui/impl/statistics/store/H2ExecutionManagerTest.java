package com.eviware.loadui.impl.statistics.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public class H2ExecutionManagerTest
{

	@Before
	public void initialize()
	{
		H2ExecutionManager h2 = new H2ExecutionManager();
		h2.clearMetaDatabase();
	}

	@Test
	public void testStartExecution()
	{
		long time = System.currentTimeMillis();
		Execution e = ExecutionManagerImpl.getInstance().startExecution( "test1", time );
		assertEquals( time, e.getStartTime() );

		try
		{
			ExecutionManagerImpl.getInstance().startExecution( "test1", time );
			fail( "Should throw IllegalArgumentException when execution was already started." );
		}
		catch( IllegalArgumentException ex )
		{

		}
	}

	@Test
	public void testGetCurrentExecution()
	{
		long time = System.currentTimeMillis();
		Execution e = ExecutionManagerImpl.getInstance().startExecution( "test2", time );
		assertEquals( e, ExecutionManagerImpl.getInstance().getCurrentExecution() );
	}

	@Test
	public void testGetExecutionNames()
	{
		( ( ExecutionManagerImpl )ExecutionManagerImpl.getInstance() ).clearMetaDatabase();
		ExecutionManagerImpl.getInstance().startExecution( "test1", 10 );
		ExecutionManagerImpl.getInstance().startExecution( "test2", 20 );
		ExecutionManagerImpl.getInstance().startExecution( "test3", 30 );
		ExecutionManagerImpl.getInstance().startExecution( "test4", 40 );
		ExecutionManagerImpl.getInstance().startExecution( "test5", 50 );
		Collection<String> l = ExecutionManagerImpl.getInstance().getExecutionNames();
		assertTrue( l.contains( "test1" ) );
		assertTrue( l.contains( "test2" ) );
		assertTrue( l.contains( "test3" ) );
		assertTrue( l.contains( "test4" ) );
		assertTrue( l.contains( "test5" ) );
	}

	@Test
	public void testGetExecution()
	{
		( ( ExecutionManagerImpl )ExecutionManagerImpl.getInstance() ).clearMetaDatabase();
		ExecutionManagerImpl.getInstance().startExecution( "test1", 10 );

		ExecutionManagerImpl.getInstance().getExecution( "test1" );
		assertTrue( ExecutionManagerImpl.getInstance().getExecution( "test1" ).getStartTime() == 10 );

		try
		{
			ExecutionManagerImpl.getInstance().getExecution( "testX" );
			fail( "Should throw IllegalArgumentException when non existing execution is requested." );
		}
		catch( IllegalArgumentException e )
		{

		}
	}

	@Test
	public void testGetTrack()
	{
		try
		{
			ExecutionManagerImpl.getInstance().getTrack( "t1" );
			fail( "Should throw IllegalArgumentException when no execution is started." );
		}
		catch( IllegalArgumentException ex )
		{

		}

		ExecutionManagerImpl.getInstance().startExecution( "test1", 10 );

		try
		{
			ExecutionManagerImpl.getInstance().getTrack( "t1" );
			fail( "Should throw IllegalArgumentException when track desriptor does not exist." );
		}
		catch( IllegalArgumentException ex )
		{

		}

		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( "a", Long.class );
		types.put( "b", Long.class );
		types.put( "c", Integer.class );
		types.put( "d", Double.class );

		Map<String, Number> values = new HashMap<String, Number>();
		values.put( "a", 1 );
		values.put( "b", 2 );
		values.put( "c", 3 );
		values.put( "d", 4 );

		TrackDescriptorImpl td = new TrackDescriptorImpl( "t1", types );
		ExecutionManagerImpl.getInstance().registerTrackDescriptor( td );
		Track t = ExecutionManagerImpl.getInstance().getTrack( "t1" );

		EntryImpl entry = new EntryImpl( ( int )( System.currentTimeMillis() / 10000 ), values );
		ExecutionManagerImpl.getInstance().writeEntry( t.getId(), entry, "local1" );

	}
}
