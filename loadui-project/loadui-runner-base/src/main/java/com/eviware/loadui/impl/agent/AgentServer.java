package com.eviware.loadui.impl.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.ServerEndpoint;

public class AgentServer implements ConnectionListener
{
	public static final Logger log = LoggerFactory.getLogger( AgentServer.class );

	public AgentServer( ServerEndpoint serverEndpoint )
	{
		serverEndpoint.addConnectionListener( this );
	}

	@Override
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
	{
		if( connected )
			new AgentWorker( endpoint );
	}
}
