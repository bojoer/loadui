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

public class BlinkOnUpdateActivityStrategy extends BlinkingActivityStrategy
{
	private final long blinkTime;
	private final Runnable blinkTimeout = new Runnable()
	{
		@Override
		public void run()
		{
			long timeLeft = lastUpdate + blinkTime - System.currentTimeMillis();
			if( timeLeft > 0 )
			{
				future = BeanInjector.getBean( ScheduledExecutorService.class ).schedule( this, timeLeft,
						TimeUnit.MILLISECONDS );
			}
			else
			{
				setBlinking( false );
				setActive( onWhenIdle );
				future = null;
			}
		}
	};

	private long lastUpdate = 0;
	private ScheduledFuture<?> future = null;
	private boolean onWhenIdle = true;

	public BlinkOnUpdateActivityStrategy( long blinkLength, long blinkTime )
	{
		super( blinkLength, false );

		this.blinkTime = blinkTime;
		setActive( onWhenIdle );
	}

	public synchronized void update()
	{
		lastUpdate = System.currentTimeMillis();
		if( future == null )
		{
			setBlinking( true );
			future = BeanInjector.getBean( ScheduledExecutorService.class ).schedule( blinkTimeout, blinkTime,
					TimeUnit.MILLISECONDS );
		}
	}

	public synchronized void setActivity( boolean active )
	{
		if( future != null )
		{
			future.cancel( true );
		}

		if( active )
		{
			setBlinking( true );
		}
		else
		{
			future = BeanInjector.getBean( ScheduledExecutorService.class ).schedule( blinkTimeout, blinkTime,
					TimeUnit.MILLISECONDS );
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
		setActive( false );
	}

	public boolean isOnWhenIdle()
	{
		return onWhenIdle;
	}

	public void setOnWhenIdle( boolean onWhenIdle )
	{
		this.onWhenIdle = onWhenIdle;
		if( future == null )
		{
			setActive( onWhenIdle );
		}
	}
}
