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
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;

public class ExecutionImplTest
{

	private static final String EXECTUION_NAME = "executionTestExecution";

	H2ExecutionManager h2;
	Execution execution;
	Track track;

	@Before
	public void initialize()
	{
		System.setProperty( LoadUI.LOADUI_HOME, "target" );

		h2 = new H2ExecutionManager();
		h2.delete( EXECTUION_NAME );
		execution = h2.startExecution( EXECTUION_NAME, 10 );
		// unload and load execution
		h2.release();
		execution = h2.getExecution( EXECTUION_NAME );
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
		assertTrue( execution.getLabel() == null );
		execution.setLabel( "testLabel" );
		assertTrue( execution.getLabel().equals( "testLabel" ) );
		execution.setLabel( "" );
		assertTrue( execution.getLabel().equals( "" ) );
		execution.setLabel( null );
		assertTrue( execution.getLabel() == null );
	}

	@Test
	public void testDelete()
	{
		assertTrue( h2.getExecutionNames().contains( EXECTUION_NAME ) );
		execution.delete();
		assertFalse( h2.getExecutionNames().contains( EXECTUION_NAME ) );
	}

	@After
	public void release()
	{
		h2.release();
	}

}
