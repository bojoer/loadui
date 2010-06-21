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
