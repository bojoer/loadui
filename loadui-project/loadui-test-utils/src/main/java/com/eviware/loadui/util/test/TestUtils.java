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
package com.eviware.loadui.util.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.util.events.EventFuture;

/**
 * Utilities to help with writing unit tests.
 * 
 * @author dain.nilsson
 */
public class TestUtils
{
	private static final String AWAIT_EVENTS = TestUtils.class.getName() + "@awaitEvents";

	/**
	 * Inserts an event into the EventFirers event queue, and waits for it to be
	 * triggered, causing all previously queued events to also have been
	 * triggered.
	 * 
	 * @param eventFirer
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void awaitEvents( EventFirer eventFirer ) throws InterruptedException, ExecutionException,
			TimeoutException
	{
		awaitEvents( eventFirer, 1 );
	}

	/**
	 * Like AwaitEvents, but runs multiple times to ensure waiting for events
	 * triggered by other event handlers.
	 * 
	 * @param eventFirer
	 * @param times
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void awaitEvents( EventFirer eventFirer, int times ) throws InterruptedException, ExecutionException,
			TimeoutException
	{
		for( int i = 0; i < times; i++ )
		{
			EventFuture<BaseEvent> eventFuture = EventFuture.forKey( eventFirer, AWAIT_EVENTS );
			eventFirer.fireEvent( new BaseEvent( eventFirer, AWAIT_EVENTS ) );
			eventFuture.get( 5, TimeUnit.SECONDS );
		}
	}

	public static void awaitCondition( Callable<Boolean> condition ) throws Exception
	{
		awaitCondition( condition, 5 );
	}

	public static void awaitCondition( Callable<Boolean> condition, int timeoutInSeconds ) throws Exception
	{
		long timeout = System.currentTimeMillis() + timeoutInSeconds * 1000;
		while( !condition.call() )
		{
			Thread.sleep( 10 );
			if( System.currentTimeMillis() > timeout )
			{
				throw new TimeoutException();
			}
		}
	}
}
