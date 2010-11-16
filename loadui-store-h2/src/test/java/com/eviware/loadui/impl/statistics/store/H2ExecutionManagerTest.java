package com.eviware.loadui.impl.statistics.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Execution;

public class H2ExecutionManagerTest
{

	@Before
	public void initialize()
	{
		new H2ExecutionManager();
		( ( ExecutionManagerImpl )ExecutionManagerImpl.getInstance() ).clearMetaDatabase();
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
}
