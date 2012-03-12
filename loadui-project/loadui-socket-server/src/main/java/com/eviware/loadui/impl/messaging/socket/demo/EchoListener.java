package com.eviware.loadui.impl.messaging.socket.demo;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.ServerEndpoint;

/**
 * Echos each message back to the sender.
 * 
 * @author dain.nilsson
 */
public class EchoListener implements MessageListener, ConnectionListener
{
	public EchoListener( ServerEndpoint serverEndpoint )
	{
		serverEndpoint.addConnectionListener( this );
	}

	@Override
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		endpoint.sendMessage( channel, data );
	}

	@Override
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
	{
		if( connected )
		{
			endpoint.addMessageListener( "/echo", this );
			endpoint.addConnectionListener( this );
		}
		else
		{
			endpoint.removeMessageListener( this );
			endpoint.removeConnectionListener( this );
		}
	}
}
