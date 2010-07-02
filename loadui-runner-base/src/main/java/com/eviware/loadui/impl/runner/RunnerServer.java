package com.eviware.loadui.impl.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.ServerEndpoint;

public class RunnerServer implements ConnectionListener
{
	public static final Logger log = LoggerFactory.getLogger( RunnerServer.class );

	public RunnerServer( ServerEndpoint serverEndpoint )
	{
		serverEndpoint.addConnectionListener( this );
	}

	@Override
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
	{
		if( connected )
			new RunnerWorker( endpoint );
	}
}
