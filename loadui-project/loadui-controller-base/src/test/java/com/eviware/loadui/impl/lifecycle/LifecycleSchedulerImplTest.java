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
package com.eviware.loadui.impl.lifecycle;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.eviware.loadui.api.lifecycle.IllegalLifecycleStateException;
import com.eviware.loadui.api.lifecycle.LifecycleTask;
import com.eviware.loadui.api.lifecycle.Phase;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class LifecycleSchedulerImplTest
{
	private LifecycleSchedulerImpl scheduler;

	@Before
	public void setup()
	{
		scheduler = new LifecycleSchedulerImpl( Executors.newCachedThreadPool() );
	}

	@Test
	public void shouldCompleteLifecycle() throws Exception
	{
		Future<ConcurrentMap<String, Object>> future = scheduler.requestStart( Collections.<String, Object> emptyMap() );
		future.get( 1, TimeUnit.SECONDS );

		future = scheduler.requestStop();
		future.get( 1, TimeUnit.SECONDS );
	}

	@Test( expected = IllegalLifecycleStateException.class )
	public void shouldOnlyStartOneLifecycle() throws Exception
	{
		final LifecycleTask task = new LifecycleTask()
		{
			@Override
			@SuppressWarnings( value = { "UW_UNCOND_WAIT", "WA_NOT_IN_LOOP" } )
			public void invoke( ConcurrentMap<String, Object> context, Phase phase )
			{
				try
				{
					synchronized( this )
					{
						this.wait();
					}
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		};
		scheduler.registerTask( task, Phase.PRE_START );

		scheduler.requestStart( Collections.<String, Object> emptyMap() );
		scheduler.requestStart( Collections.<String, Object> emptyMap() );
	}

	@Test
	public void shouldHandleMultipleStopRequests() throws Exception
	{
		scheduler.requestStart( Collections.<String, Object> emptyMap() );

		Future<ConcurrentMap<String, Object>> future1 = scheduler.requestStop();
		Future<ConcurrentMap<String, Object>> future2 = scheduler.requestStop();
		Future<ConcurrentMap<String, Object>> future3 = scheduler.requestStop();

		future3.get( 1, TimeUnit.SECONDS );
		future1.get( 1, TimeUnit.SECONDS );
		future2.get( 1, TimeUnit.SECONDS );
	}
}
