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
package com.eviware.loadui.impl.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.discovery.AgentDiscovery;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class AgentDiscoveryImpl implements AgentDiscovery
{
	private final static int DELAY = 30;
	private final static Logger log = LoggerFactory.getLogger( AgentDiscoveryImpl.class );

	private final DatagramSocket socket;
	private final Thread listenerThread;
	private final Set<AgentReference> agents = Sets.newCopyOnWriteArraySet();

	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor( new ThreadFactoryBuilder().setNameFormat( "loadUI Agent discovery" )
					.setDaemon( true ).build() );

	public AgentDiscoveryImpl()
	{
		try
		{
			byte[] discoverBytes = "DISCOVER".getBytes();
			final DatagramPacket sendPacket = new DatagramPacket( discoverBytes, discoverBytes.length,
					InetAddress.getByName( "255.255.255.255" ), BROADCAST_PORT );
			socket = new DatagramSocket();
			socket.setBroadcast( true );

			listenerThread = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						byte[] buf = new byte[1024];
						while( !socket.isClosed() )
						{
							DatagramPacket receivePacket = new DatagramPacket( buf, buf.length );
							socket.receive( receivePacket );

							String received = new String( receivePacket.getData(), 0, receivePacket.getLength() ).replaceAll(
									"127.0.0.1", receivePacket.getAddress().getHostAddress() );
							String[] parts = received.split( " " );
							if( parts.length == 4 && LoadUI.AGENT.equals( parts[0] ) )
								if( agents.add( new AgentRefImpl( parts[1], parts[2], parts[3] ) ) )
									log.debug( "Discovered Agent: " + parts[2] );
						}
					}
					catch( IOException e )
					{
						// ignore
					}
				}
			}, "loadUI Agent discovery 2" );

			if( System.getProperty( LoadUI.DISABLE_DISCOVERY ) != null )
				return;

			listenerThread.start();

			executor.scheduleAtFixedRate( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						socket.send( sendPacket );
					}
					catch( IOException e )
					{
						// Ignore...
					}
				}
			}, 0, DELAY, TimeUnit.SECONDS );
		}
		catch( UnknownHostException e )
		{
			// Should never happen
			throw new RuntimeException( e );
		}
		catch( SocketException e )
		{
			throw new RuntimeException( e );
		}

		log.debug( "AgentDiscovery started, searching on UDP port: {}", BROADCAST_PORT );
	}

	public void release()
	{
		executor.shutdownNow();
		socket.close();
		try
		{
			listenerThread.join();
		}
		catch( InterruptedException e )
		{
			// Ignore
		}
	}

	@Override
	public Collection<AgentReference> getDiscoveredAgents()
	{
		long now = System.currentTimeMillis();
		List<AgentReference> refsToRemove = new LinkedList<>();
		for( AgentReference ref : agents )
		{
			AgentRefImpl refImpl = ( AgentRefImpl )ref;
			if( refImpl.discoveryTime + 2000 * DELAY < now )
			{
				log.debug( "Removing stale entry: {}", ref );
				refsToRemove.add( ref );
			}
		}
		agents.removeAll( refsToRemove );
		return Collections.unmodifiableSet( agents );
	}

	private static class AgentRefImpl implements AgentReference
	{
		private final String label;
		private final String url;
		private final String id;
		private long discoveryTime;

		public AgentRefImpl( String url, String label, String id )
		{
			this.url = url;
			this.label = label;
			this.id = id;
			discoveryTime = System.currentTimeMillis();
		}

		@Override
		public String getDefaultLabel()
		{
			return label;
		}

		@Override
		public String getUrl()
		{
			return url;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( label == null ) ? 0 : label.hashCode() );
			return prime * result + ( ( url == null ) ? 0 : url.hashCode() );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			AgentRefImpl other = ( AgentRefImpl )obj;

			if( label == null )
			{
				if( other.label != null )
					return false;
			}
			else if( !label.equals( other.label ) )
				return false;
			if( url == null )
			{
				if( other.url != null )
					return false;
			}
			else if( !url.equals( other.url ) )
				return false;

			discoveryTime = Math.max( discoveryTime, other.discoveryTime );
			other.discoveryTime = discoveryTime;

			return true;
		}

		@Override
		public String getId()
		{
			return id;
		}
	}
}
