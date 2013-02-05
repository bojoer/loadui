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
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.util.StringUtils;

enum MessageSeveritySource implements TestEvent.Source<MessageTestEvent>
{
	NOTIFICATION_SOURCE( MessageLevel.NOTIFICATION ), WARNING_SOURCE( MessageLevel.WARNING ), ERROR_SOURCE( MessageLevel.ERROR );

	static MessageSeveritySource getSource( MessageLevel level )
	{
		switch( level )
		{
		case NOTIFICATION :
			return NOTIFICATION_SOURCE;
		case WARNING :
			return WARNING_SOURCE;
		case ERROR :
			return ERROR_SOURCE;
		}

		throw new IllegalArgumentException();
	}

	private final TestEventSourceSupport sourceSupport;

	MessageSeveritySource( MessageLevel level )
	{
		sourceSupport = new TestEventSourceSupport( StringUtils.capitalize( level.toString() ),
				new byte[] { ( byte )level.ordinal() } );
	}

	@Override
	public String getLabel()
	{
		return sourceSupport.getLabel();
	}

	@Override
	public Class<MessageTestEvent> getType()
	{
		return MessageTestEvent.class;
	}

	@Override
	public byte[] getData()
	{
		return sourceSupport.getData();
	}

	@Override
	public String getHash()
	{
		return sourceSupport.getHash();
	}
}