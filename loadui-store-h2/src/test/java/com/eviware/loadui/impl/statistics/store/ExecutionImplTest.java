/*
 * Copyright 2011 eviware software ab
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.store.Track;

public class ExecutionImplTest
{

	private static final String EXECUTION_NAME = "executionTestExecution";

	H2ExecutionManager h2;
	ExecutionImpl execution;
	Track track;

	@Before
	public void initialize()
	{
		System.setProperty( LoadUI.LOADUI_HOME, "target" );

		h2 = new H2ExecutionManager();
		h2.delete( EXECUTION_NAME );
		execution = ( ExecutionImpl )h2.startExecution( EXECUTION_NAME, 10 );
		// unload and load execution
		h2.release();
		execution = ( ExecutionImpl )h2.getExecution( EXECUTION_NAME );
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
		execution = ( ExecutionImpl )h2.getExecution( EXECUTION_NAME );
		assertTrue( execution.getLength() == 10 );
		h2.release();
		execution = ( ExecutionImpl )h2.getExecution( EXECUTION_NAME );
		execution.updateLength( 20 );
		execution.flushLength();
		assertTrue( execution.getLength() == 20 );
	}

	@Test
	public void testDelete()
	{
		assertTrue( h2.getExecutions().contains( EXECUTION_NAME ) );
		execution.delete();
		assertFalse( h2.getExecutions().contains( EXECUTION_NAME ) );
	}

	@After
	public void release()
	{
		h2.release();
	}

}
