/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.testevents;

import com.eviware.loadui.api.testevents.TestEvent;

/**
 * Utility class for implementing different types of TestEvents.
 * 
 * @author dain.nilsson
 */
public abstract class AbstractTestEvent implements TestEvent
{
	private final Class<? extends TestEvent> type;
	private final long timestamp;

	public AbstractTestEvent( Class<? extends TestEvent> type, long timestamp )
	{
		this.type = type;
		this.timestamp = timestamp;
	}

	public AbstractTestEvent( long timestamp )
	{
		type = getClass();
		this.timestamp = timestamp;
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	public Class<? extends TestEvent> getType()
	{
		return type;
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
