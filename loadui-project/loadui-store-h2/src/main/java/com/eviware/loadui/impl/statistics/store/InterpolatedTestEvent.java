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
package com.eviware.loadui.impl.statistics.store;

import java.util.Arrays;

import com.eviware.loadui.api.annotations.Strong;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.util.testevents.AbstractTestEvent;

public class InterpolatedTestEvent extends AbstractTestEvent
{
	public static final byte[] notStrong = new byte[0];

	public InterpolatedTestEvent( Class<? extends TestEvent> type, long timestamp )
	{
		super( type, timestamp );
	}

	@Strong
	private static class Group extends InterpolatedTestEvent
	{
		public Group( Class<? extends TestEvent> type, long timestamp )
		{
			super( type, timestamp );
		}
	}

	public static InterpolatedTestEvent createEvent( Class<? extends TestEvent> type, long timestamp, byte[] data )
	{
		boolean strong = !Arrays.equals( notStrong, data );
		return strong ? new Group( type, timestamp ) : new InterpolatedTestEvent( type, timestamp );
	}
}
