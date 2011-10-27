package com.eviware.loadui.util.testevents;

import com.eviware.loadui.api.testevents.TestEvent;

public abstract class AbstractTestEvent implements TestEvent
{
	private final long timestamp;

	public AbstractTestEvent( long timestamp )
	{
		this.timestamp = timestamp;
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T extends TestEvent> Class<T> getType()
	{
		return ( Class<T> )getClass();
	}
}
