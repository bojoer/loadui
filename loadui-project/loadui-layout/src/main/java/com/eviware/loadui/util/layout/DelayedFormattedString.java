/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.util.layout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DelayedFormattedString extends FormattedString
{
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private UpdateTask updateTask = new UpdateTask();

	private ScheduledFuture<?> future;

	public DelayedFormattedString( String pattern, Object... args )
	{
		this( pattern, 1000, args );
	}

	public DelayedFormattedString( String pattern, int delay, Object... args )
	{
		super( pattern, args );

		update();

		future = scheduler.scheduleAtFixedRate( updateTask, delay, delay, TimeUnit.MILLISECONDS );
	}

	@Override
	public void release()
	{
		super.release();
		future.cancel( true );
	}

	private class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			update();
		}
	}
}
