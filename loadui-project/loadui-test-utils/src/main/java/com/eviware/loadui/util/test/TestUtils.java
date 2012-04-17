package com.eviware.loadui.util.test;

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
		EventFuture<BaseEvent> eventFuture = EventFuture.forKey( eventFirer, AWAIT_EVENTS );
		eventFirer.fireEvent( new BaseEvent( eventFirer, AWAIT_EVENTS ) );
		eventFuture.get( 5, TimeUnit.SECONDS );
	}
}
