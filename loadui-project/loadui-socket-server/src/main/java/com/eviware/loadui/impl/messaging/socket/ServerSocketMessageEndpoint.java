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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;

public class ServerSocketMessageEndpoint implements MessageEndpoint
{
	public static final Logger log = LoggerFactory.getLogger( ServerSocketMessageEndpoint.class );

	private static final Message CLOSE_MESSAGE = new Message( null, null );

	private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	private final Set<ConnectionListener> connectionListeners = Collections
			.synchronizedSet( new HashSet<ConnectionListener>() );
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
	private final SocketServerEndpoint socketServerEndpoint;
	private final SSLSocket socket;

	public ServerSocketMessageEndpoint( SocketServerEndpoint socketServerEndpoint, SSLSocket socket ) throws IOException
	{
		this.socketServerEndpoint = socketServerEndpoint;
		this.socket = socket;

		new Thread( new MessageReceiver( socket ) ).start();
		new Thread( new MessageSender( new ObjectOutputStream( socket.getOutputStream() ) ) ).start();
	}

	@Override
	public void sendMessage( String channel, Object data )
	{
		messageQueue.add( new Message( channel, data ) );
	}

	@Override
	public void addMessageListener( String channel, MessageListener listener )
	{
		routingSupport.addMessageListener( channel, listener );
	}

	@Override
	public void removeMessageListener( MessageListener listener )
	{
		routingSupport.removeMessageListener( listener );
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
	public void open()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close()
	{
		socketServerEndpoint.removeSession( this );
		messageQueue.add( CLOSE_MESSAGE );
	}

	private class MessageReceiver implements Runnable
	{
		private final SSLSocket socket;

		public MessageReceiver( SSLSocket socket )
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			ObjectInputStream ois = null;
			try
			{
				ois = new ObjectInputStream( socket.getInputStream() );
				String channel = null;
				Object data = null;

				//Initialization.
				channel = ois.readUTF();
				data = ois.readObject();
				if( "/service/init".equals( channel ) && LoadUI.AGENT_VERSION.equals( data ) )
				{
					socketServerEndpoint.addSession( ServerSocketMessageEndpoint.this );
					log.debug( "Session initialized!" );
				}
				else
				{
					log.warn( "Client attempted to connect with invalid version string. Mine: {}, Theirs: {}",
							LoadUI.AGENT_VERSION, data );
					sendMessage( channel, LoadUI.AGENT_VERSION );
					close();
					return;
				}

				while( ( channel = ois.readUTF() ) != null && ( data = ois.readObject() ) != null )
				{
					log.debug( "Got message: {}: {}", channel, data );
					routingSupport.fireMessage( channel, ServerSocketMessageEndpoint.this, data );
				}
			}
			catch( ClassNotFoundException e )
			{
				log.error( "Error parsing message:", e );
			}
			catch( IOException e )
			{
				log.error( "Connection closed:", e );
			}
			finally
			{
				for( ConnectionListener listener : ImmutableSet.copyOf( connectionListeners ) )
				{
					listener.handleConnectionChange( ServerSocketMessageEndpoint.this, false );
				}
			}

			Closeables.closeQuietly( ois );

			log.debug( "Connection closed: {}", socket.getRemoteSocketAddress() );
			socketServerEndpoint.removeSession( ServerSocketMessageEndpoint.this );

			try
			{
				socket.close();
			}
			catch( IOException e )
			{
			}
		}
	}

	private class MessageSender implements Runnable
	{
		private final ObjectOutputStream oos;

		public MessageSender( ObjectOutputStream oos )
		{
			this.oos = oos;
		}

		@Override
		public void run()
		{
			Message message = null;
			try
			{
				while( ( message = messageQueue.take() ) != CLOSE_MESSAGE )
				{
					oos.writeUTF( message.channel );
					oos.writeObject( message.data );
					oos.flush();
				}
			}
			catch( InterruptedException e )
			{
				log.error( "Sending of messages failed:", e );
			}
			catch( IOException e )
			{
				log.error( "Sending of messages failed:", e );
			}

			try
			{
				socket.close();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	private static class Message
	{
		public final String channel;
		public final Object data;

		public Message( String channel, Object data )
		{
			this.channel = channel;
			this.data = data;
		}
	}
}
