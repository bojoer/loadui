package com.eviware.loadui.api.events;

import java.util.EventObject;

import com.eviware.loadui.api.component.ActivityStrategy;

public class ActivityEvent extends EventObject
{
	private static final long serialVersionUID = -3481011357076103726L;

	public ActivityEvent( ActivityStrategy source )
	{
		super( source );
	}

	public boolean isActive()
	{
		return ( ( ActivityStrategy )getSource() ).isActive();
	}
}
