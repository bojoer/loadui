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
import java.util.concurrent.atomic.AtomicReference;

import com.eviware.loadui.util.BeanInjector;

public class BlinkOnUpdateActivityStrategy extends BlinkingActivityStrategy
{
	private final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();
	private final long blinkTime;
	private final Runnable blinkTimeout = new Runnable()
	{
		@Override
		public void run()
		{
			long timeLeft = lastUpdate + blinkTime - System.currentTimeMillis();
			ScheduledFuture<?> future;
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
			futureRef.set( future );
		}
	};

	private long lastUpdate = 0;
	private boolean onWhenIdle = true;

	public BlinkOnUpdateActivityStrategy( long blinkLength, long blinkTime )
	{
		super( blinkLength, false );

		this.blinkTime = blinkTime;
		setActive( onWhenIdle );
	}

	private boolean cancelFuture()
	{
		ScheduledFuture<?> future = futureRef.getAndSet( null );
		if( future != null )
		{
			future.cancel( true );
			return true;
		}

		return false;
	}

	private boolean createFuture()
	{
		while( futureRef.get() == null )
		{
			ScheduledFuture<?> future = BeanInjector.getBean( ScheduledExecutorService.class ).schedule( blinkTimeout,
					blinkTime, TimeUnit.MILLISECONDS );
			if( !futureRef.compareAndSet( null, future ) )
			{
				return true;
			}
			else
			{
				future.cancel( true );
			}
		}

		return false;
	}

	public void update()
	{
		lastUpdate = System.currentTimeMillis();
		if( createFuture() )
		{
			setBlinking( true );
		}
	}

	public void setActivity( boolean active )
	{
		cancelFuture();

		if( active )
		{
			setBlinking( true );
		}
		else
		{
			createFuture();
		}
	}

	@Override
	public void release()
	{
		super.release();
		cancelFuture();
		setActive( false );
	}

	public boolean isOnWhenIdle()
	{
		return onWhenIdle;
	}

	public void setOnWhenIdle( boolean onWhenIdle )
	{
		this.onWhenIdle = onWhenIdle;
		if( futureRef.get() == null )
		{
			setActive( onWhenIdle );
		}
	}
}
