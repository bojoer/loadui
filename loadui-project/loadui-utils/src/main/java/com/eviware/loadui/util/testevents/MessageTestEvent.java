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
package com.eviware.loadui.util.testevents;

import com.eviware.loadui.api.testevents.MessageLevel;
import com.google.common.base.Charsets;

public class MessageTestEvent extends AbstractTestEvent
{
	private final MessageLevel level;
	private final String message;

	public MessageTestEvent( long timestamp, MessageLevel level, String message )
	{
		super( timestamp );

		this.level = level;
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public MessageLevel getLevel()
	{
		return level;
	}

	@Override
	public String toString()
	{
		return message;
	}

	public static class Factory extends AbstractTestEvent.Factory<MessageTestEvent>
	{
		public Factory()
		{
			super( MessageTestEvent.class, "MessageEvent" );
		}

		@Override
		public MessageTestEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			MessageLevel level = MessageLevel.values()[sourceData[0]];

			return new MessageTestEvent( timestamp, level, new String( entryData, Charsets.UTF_8 ) );
		}

		@Override
		public byte[] getDataForTestEvent( MessageTestEvent testEvent )
		{
			return testEvent.message.getBytes( Charsets.UTF_8 );
		}

	}
}
