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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton event queue which is shared between all LoadUI {@link EventSupport} instances.
 * This centralizes the management of events in LoadUI.
 * @author renato
 *
 */
public class EventQueue
{

	private static final EventQueue instance = new EventQueue();

	private static final Logger log = LoggerFactory.getLogger( EventQueue.class );

	private final BlockingQueue<Runnable> eventQueue = new LinkedBlockingQueue<Runnable>();
	protected Thread thread;

	public static final EventQueue getInstance()
	{
		return instance;
	}

	private EventQueue()
	{
		// singleton cannot be instantiated externally
		spawnEventThread();
	}

	private void spawnEventThread()
	{
		thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{

				try
				{
					while( true )
					{
						try
						{
							Runnable action = eventQueue.poll( Integer.MAX_VALUE, TimeUnit.MINUTES );
							if( action != null )
							{
								action.run();
							}
						}
						catch( Exception e )
						{
							log.error( "Problem running event action", e );
						}
					}
				}
				finally
				{
					spawnEventThread();
				}

			}
		}, "EventQueueThread" );

		thread.setDaemon( true );
		thread.start();
	}

	public boolean offer( Runnable runnable )
	{
		return eventQueue.offer( runnable );
	}

}
