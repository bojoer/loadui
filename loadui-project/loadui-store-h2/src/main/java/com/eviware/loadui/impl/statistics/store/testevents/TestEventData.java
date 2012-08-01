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
package com.eviware.loadui.impl.statistics.store.testevents;

import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

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
	private final int interpolationLevel;

	public TestEventData( long timestamp, @Nonnull String type, @Nonnull TestEventSourceConfig sourceConfig,
			@Nonnull byte[] data, int interpolationLevel )
	{
		this.timestamp = timestamp;
		this.type = type;
		this.sourceConfig = sourceConfig;
		this.data = data;
		this.interpolationLevel = interpolationLevel;
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

	public int getInterpolationLevel()
	{
		return interpolationLevel;
	}
}
