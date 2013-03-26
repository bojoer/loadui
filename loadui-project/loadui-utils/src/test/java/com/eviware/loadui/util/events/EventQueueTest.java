/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.events;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class EventQueueTest
{

	@Test
	public void testQueueThreadStarts() throws InterruptedException
	{
		EventQueue queue = EventQueue.getInstance();
		Thread.sleep( 100 );
		assertTrue( queue.thread.isAlive() );
	}

	@Test
	public void testQueuedRunnablesRunInRightOrder() throws Throwable
	{

		final BlockingQueue<String> errors = new ArrayBlockingQueue<String>( 1, true );
		EventQueue queue = EventQueue.getInstance();

		final AtomicInteger aint = new AtomicInteger( 0 );
		final int MAX = 1000;

		class RunAfterTest
		{
			void run()
			{
				if( MAX != aint.get() )
				{
					errors.offer( "Value " + aint.get() + " is not equal to MAX (" + MAX + ")" );
				}
			}
		}

		final RunAfterTest runAfter = new RunAfterTest();
		class TestRunnable implements Runnable
		{
			final int n;

			TestRunnable( int n )
			{
				this.n = n;
			}

			@Override
			public void run()
			{
				int newVal = aint.incrementAndGet();
				if( newVal != n )
				{
					errors.offer( "New value is " + newVal + " but expected is " + n );
				}
				else if( n == MAX )
				{
					runAfter.run();
				}

			}
		}
		;

		for( int i = 0; i <= MAX; i++ )
		{
			assertTrue( queue.offer( new TestRunnable( i + 1 ) ) );
		}

		String error = errors.poll( 1, TimeUnit.SECONDS );
		if( error != null )
		{
			fail( error );
		}

	}

}
