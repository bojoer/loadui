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
package com.eviware.loadui.impl.messaging.socket;

import java.io.IOException;
import java.util.Set;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.commons.ssl.SSLServer;
import org.apache.commons.ssl.TrustMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.api.traits.Releasable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

final public class SocketServerEndpoint implements ServerEndpoint, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( SocketServerEndpoint.class );

	private final Set<ServerSocketMessageEndpoint> sessions = Sets.newCopyOnWriteArraySet();
	private final Set<ConnectionListener> connectionListeners = Sets.newCopyOnWriteArraySet();
	private final SSLServerSocket serverSocket;

	public SocketServerEndpoint() throws Exception
	{
		SSLServer server = new SSLServer();

		server.setKeyMaterial( new KeyMaterial( System.getProperty( LoadUI.KEY_STORE ), System.getProperty(
				LoadUI.KEY_STORE_PASSWORD ).toCharArray() ) );

		server.addTrustMaterial( new TrustMaterial( System.getProperty( LoadUI.TRUST_STORE ), System.getProperty(
				LoadUI.TRUST_STORE_PASSWORD ).toCharArray() ) );

		server.setNeedClientAuth( true );
		server.setCheckHostname( false );

		int port = Integer.parseInt( System.getProperty( LoadUI.HTTPS_PORT, "8443" ) );
		serverSocket = ( SSLServerSocket )server.createServerSocket( port );

		log.info( "*** Agent listening for incoming connections on port {} ***", port );

		new Thread( new ConnectionAccepter(), "SockerServerEndpoint connection accepter" ).start();
	}

	@Override
	public Set<MessageEndpoint> getConnectedEndpoints()
	{
		return ImmutableSet.<MessageEndpoint> copyOf( sessions );
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		connectionListeners.add( listener );
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		connectionListeners.remove( listener );
	}

	@Override
	public void release()
	{
		try
		{
			serverSocket.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		for( ServerSocketMessageEndpoint endpoint : sessions )
		{
			endpoint.close();
		}
	}

	void addSession( ServerSocketMessageEndpoint endpoint )
	{
		sessions.add( endpoint );
		for( ConnectionListener listener : ImmutableSet.copyOf( connectionListeners ) )
		{
			listener.handleConnectionChange( endpoint, true );
		}
	}

	void removeSession( ServerSocketMessageEndpoint endpoint )
	{
		sessions.remove( endpoint );
		for( ConnectionListener listener : ImmutableSet.copyOf( connectionListeners ) )
		{
			listener.handleConnectionChange( endpoint, false );
		}
	}

	private class ConnectionAccepter implements Runnable
	{
		@Override
		public void run()
		{
			while( !serverSocket.isClosed() )
			{
				try
				{
					SSLSocket socket = ( SSLSocket )serverSocket.accept();
					ServerSocketMessageEndpoint.newInstance( SocketServerEndpoint.this, socket );
				}
				catch( IOException e )
				{
					log.error( "Error in SSLSocket", e );
				}
			}
		}
	}
}