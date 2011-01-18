/*
 * Copyright 2010 eviware software ab
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.discovery.AgentDiscovery;

public class AgentDiscoveryImpl implements AgentDiscovery
{
	private final static int DELAY = 30;
	private final static Logger log = LoggerFactory.getLogger( AgentDiscoveryImpl.class );

	private final DatagramSocket socket;
	private final DatagramPacket packet;
	private final Thread listenerThread;
	private final Set<AgentReference> agents = Collections.synchronizedSet( new HashSet<AgentReference>() );

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor( new ThreadFactory()
	{
		@Override
		public Thread newThread( Runnable r )
		{
			return new Thread( r, "loadUI Agent discovery" );
		}
	} );

	public AgentDiscoveryImpl()
	{
		byte[] buf = "DISCOVER".getBytes();

		try
		{
			packet = new DatagramPacket( buf, buf.length, InetAddress.getByName( "255.255.255.255" ), BROADCAST_PORT );
			socket = new DatagramSocket();
			socket.setBroadcast( true );

			listenerThread = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					byte[] buf;
					DatagramPacket packet;
					try
					{
						while( !socket.isClosed() )
						{
							buf = new byte[1024];
							packet = new DatagramPacket( buf, buf.length );
							socket.receive( packet );

							String received = new String( packet.getData(), 0, packet.getLength() ).replaceAll( "127.0.0.1",
									packet.getAddress().getHostAddress() );
							String[] parts = received.split( " " );
							if( parts.length == 4 && parts[0].equals( LoadUI.AGENT ) )
								if( agents.add( new AgentRefImpl( parts[1], parts[2], parts[3] ) ) )
									log.debug( "Discovered Agent: " + parts[2] );
						}
					}
					catch( Exception e )
					{
						// ignore
					}
				}
			}, "loadUI Agent discovery 2" );
			listenerThread.start();

			executor.scheduleAtFixedRate( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						socket.send( packet );
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
		Iterator<AgentReference> it = agents.iterator();
		long now = System.currentTimeMillis();
		while( it.hasNext() )
		{
			AgentRefImpl ref = ( AgentRefImpl )it.next();
			if( ref.discoveryTime + 2000 * DELAY < now )
			{
				log.debug( "Removing stale entry: {}", ref );
				it.remove();
			}
		}
		return Collections.unmodifiableSet( agents );
	}

	private class AgentRefImpl implements AgentReference
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
			result = prime * result + ( ( url == null ) ? 0 : url.hashCode() );
			return result;
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
