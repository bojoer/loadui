package com.eviware.loadui.impl.component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.component.ActivityStrategy;
import com.eviware.loadui.util.BeanInjector;

public class ActivityStrategies
{
	public static final ActivityStrategy ON = new AbstractActivityStrategy( true )
	{
	};

	public static final ActivityStrategy OFF = new AbstractActivityStrategy( false )
	{
	};

	public static final ActivityStrategy BLINKING = new BlinkingActivityStrategy( 500 );

	public static class BlinkingActivityStrategy extends AbstractActivityStrategy
	{
		private final ScheduledFuture<?> future;

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
}
