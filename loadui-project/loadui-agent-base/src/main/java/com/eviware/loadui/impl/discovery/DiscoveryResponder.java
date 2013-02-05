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
package com.eviware.loadui.impl.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.discovery.AgentDiscovery;
import com.eviware.loadui.api.traits.Releasable;

public final class DiscoveryResponder implements Releasable
{
	private static final Logger log = LoggerFactory.getLogger( DiscoveryResponder.class );

	private static final String ip = "127.0.0.1";

	private final String id;
	private String label;
	private DatagramSocket socket;
	private Thread responderThread;

	public DiscoveryResponder()
	{
		id = UUID.randomUUID().toString();

		try
		{
			label = InetAddress.getLocalHost().getHostName();
			socket = new DatagramSocket( AgentDiscovery.BROADCAST_PORT );
		}
		catch( Exception e )
		{
			log.error( "Failed starting AutoDiscovery, AutoDiscovery of this agent will not work!", e );
			return;
		}

		responderThread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				byte[] buf;
				DatagramPacket packet;

				while( !socket.isClosed() )
				{
					buf = new byte[256];
					packet = new DatagramPacket( buf, buf.length );
					try
					{
						socket.receive( packet );
						String received = new String( packet.getData(), 0, packet.getLength() );
						if( "DISCOVER".equals( received ) )
						{
							buf = ( LoadUI.AGENT + " https://" + ip + ":" + System.getProperty( LoadUI.HTTPS_PORT, "8443" )
									+ "/ " + label + " " + id ).getBytes();
							packet = new DatagramPacket( buf, buf.length, packet.getAddress(), packet.getPort() );
							socket.send( packet );
						}
					}
					catch( IOException e )
					{
						// Ignore.
					}
				}

			}
		}, "loadUI Agent discovery" );

		responderThread.setDaemon( true );
		responderThread.start();
	}

	@Override
	public void release()
	{
		socket.close();
		try
		{
			responderThread.join();
		}
		catch( InterruptedException e )
		{
			// Ignore
		}
	}
}