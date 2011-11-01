/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.util.BeanInjector;

public class BlinkingActivityStrategy extends AbstractActivityStrategy
{
	public ScheduledFuture<?> future;

	public BlinkingActivityStrategy( long blinkLength )
	{
		super( false );

		future = BeanInjector.getBean( ScheduledExecutorService.class ).scheduleAtFixedRate( new Runnable()
		{
			@Override
			public void run()
			{
				setActive( !isActive() );
			}
		}, blinkLength, blinkLength, TimeUnit.MILLISECONDS );
	}

	public void release()
	{
		future.cancel( true );
	}
}