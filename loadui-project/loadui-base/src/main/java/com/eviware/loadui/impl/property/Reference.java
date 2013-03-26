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
package com.eviware.loadui.impl.property;

import com.eviware.loadui.api.messaging.MessageEndpoint;

public class Reference
{
	private final String id;
	private final MessageEndpoint endpoint;

	public Reference( String id, MessageEndpoint endpoint )
	{
		this.id = id;
		this.endpoint = endpoint;
	}

	public MessageEndpoint getEndpoint()
	{
		return endpoint;
	}

	public String getId()
	{
		return id;
	}
}
