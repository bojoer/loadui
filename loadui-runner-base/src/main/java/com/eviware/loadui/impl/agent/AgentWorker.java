package com.eviware.loadui.impl.agent;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.model.AgentItem;

public class AgentWorker implements ConnectionListener, MessageListener
{
	private final MessageEndpoint endpoint;

	public AgentWorker( MessageEndpoint endpoint )
	{
		this.endpoint = endpoint;

		endpoint.addConnectionListener( this );
		endpoint.addMessageListener( AgentItem.AGENT_CHANNEL, this );
		endpoint.addMessageListener( SceneCommunication.CHANNEL, this );
	}

	@Override
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
	{
		if( !connected )
		{
			System.out.println( "Stopping AgentWorker..." );
		}
	}

	@Override
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		// TODO Auto-generated method stub

	}

}
