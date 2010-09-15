package com.eviware.loadui.api.component;

import com.eviware.loadui.api.events.EventFirer;

public interface ActivityStrategy extends EventFirer
{
	public static final String ACTIVITY_EVENT = ActivityStrategy.class.getName() + "@active";

	public boolean isActive();
}
