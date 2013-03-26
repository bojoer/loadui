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
package com.eviware.loadui.impl.component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.util.BeanInjector;

public class BlinkingActivityStrategy extends AbstractActivityStrategy
{
	private final Runnable toggleRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			setActive( !isActive() );
		}
	};
	protected final long blinkLength;
	private ScheduledFuture<?> future;
	private boolean blinking = false;

	public BlinkingActivityStrategy( long blinkLength, boolean blinking )
	{
		super( false );

		this.blinkLength = blinkLength;
		setBlinking( blinking );
	}

	protected final void setBlinking( boolean blinking )
	{
		if( this.blinking != blinking )
		{
			if( future != null )
			{
				future.cancel( true );
			}
			if( blinking )
			{
				future = BeanInjector.getBean( ScheduledExecutorService.class ).scheduleAtFixedRate( toggleRunnable,
						blinkLength, blinkLength, TimeUnit.MILLISECONDS );
			}
			this.blinking = blinking;
		}
	}

	@Override
	public void release()
	{
		super.release();

		if( future != null )
		{
			future.cancel( true );
		}
	}
}
