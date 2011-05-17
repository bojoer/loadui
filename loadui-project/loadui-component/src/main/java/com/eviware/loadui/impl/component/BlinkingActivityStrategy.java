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