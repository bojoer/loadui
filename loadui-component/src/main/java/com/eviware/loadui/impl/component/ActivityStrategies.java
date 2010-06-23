package com.eviware.loadui.impl.component;

import com.eviware.loadui.api.component.ActivityStrategy;

public class ActivityStrategies
{
	public static final ActivityStrategy ON = new AbstractActivityStrategy( true )
	{
	};

	public static final ActivityStrategy OFF = new AbstractActivityStrategy( false )
	{
	};

	public static final ActivityStrategy BLINKING = new BlinkingActivityStrategy( 500 );
}
