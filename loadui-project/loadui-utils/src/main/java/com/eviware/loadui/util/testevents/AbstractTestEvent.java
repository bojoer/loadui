package com.eviware.loadui.util.testevents;

import com.eviware.loadui.api.testevents.TestEvent;

/**
 * Utility class for implementing different types of TestEvents.
 * 
 * @author dain.nilsson
 */
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

	public static abstract class Factory<T extends TestEvent> implements TestEvent.Factory<T>
	{
		private final Class<T> type;
		private final String typeName;

		public Factory( Class<T> type, String typeName )
		{
			this.type = type;
			this.typeName = typeName;
		}

		public Factory( Class<T> type )
		{
			this( type, type.getSimpleName() );
		}

		@Override
		public String getLabel()
		{
			return typeName;
		}

		@Override
		public Class<T> getType()
		{
			return type;
		}
	}
}
