package com.eviware.loadui.impl.messaging.socket.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.api.messaging.MessageListener;

public class TrafficTester
{
	public static final Logger log = LoggerFactory.getLogger( TrafficTester.class );

	int count = 0;
	long lastOutput = System.currentTimeMillis();
	int lastCount = 0;

	public TrafficTester( MessageEndpointProvider endpointProvider )
	{
		String host = System.getProperty( "trafficTester" );
		if( host == null )
			return;

		MessageEndpoint endpoint = endpointProvider.createEndpoint( host );

		endpoint.addMessageListener( "/echo", new MessageListener()
		{
			@Override
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				int value = ( Integer )data;
				if( count == value )
				{
					long now = System.currentTimeMillis();
					long delta = now - lastOutput;
					if( delta >= 1000 )
					{
						log.info( "At message: {}, {} roundtrips per second...", count, ( double )( count - lastCount )
								/ ( ( double )delta / 1000 ) );
						lastOutput = now;
						lastCount = count;
					}
					endpoint.sendMessage( "/echo", ++count );
				}
				else
				{
					log.error( "Got {}, expecting {}!", value, count );
				}
			}
		} );

		endpoint.addConnectionListener( new ConnectionListener()
		{
			@Override
			public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
			{
				if( connected )
				{
					lastOutput = System.currentTimeMillis();
					count = 0;
					lastCount = 0;
					endpoint.sendMessage( "/echo", 0 );
				}
			}
		} );
		endpoint.open();
	}
}
