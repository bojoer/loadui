package com.eviware.loadui.impl.statistics.store.testevents;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable data structure holding the values of a TestEvent. Technically the
 * byte[] isn't immutable, but it has been protected with a no-modification
 * charm. Anyone that modifies it will trigger the curse, beware!
 * 
 * @author dain.nilsson
 */
@Immutable
public final class TestEventData
{
	private final long timestamp;
	private final String type;
	private final TestEventSourceConfig sourceConfig;
	private final byte[] data;

	public TestEventData( long timestamp, @Nonnull String type, @Nonnull TestEventSourceConfig sourceConfig,
			@Nonnull byte[] data )
	{
		this.timestamp = timestamp;
		this.type = type;
		this.sourceConfig = sourceConfig;
		this.data = data;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	@Nonnull
	public String getType()
	{
		return type;
	}

	@Nonnull
	public TestEventSourceConfig getTestEventSourceConfig()
	{
		return sourceConfig;
	}

	@Nonnull
	public byte[] getData()
	{
		return data;
	}
}
