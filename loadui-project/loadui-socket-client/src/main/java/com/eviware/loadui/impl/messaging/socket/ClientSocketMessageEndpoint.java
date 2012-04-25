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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;

import org.apache.commons.ssl.SSLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.VersionMismatchException;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

public class ClientSocketMessageEndpoint implements MessageEndpoint
{
	private enum State
	{
		CLOSING, CLOSED, CONNECTING, CONNECTED, DISCONNECTED
	}

	public static final Logger log = LoggerFactory.getLogger( ClientSocketMessageEndpoint.class );
	private static final Message CLOSE_MESSAGE = new Message( "/service/close", null );

	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
	private final Set<ConnectionListener> listeners = Sets.newHashSet();
	private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	private final SSLClient sslClient;
	private final String host;
	private final int port;

	private volatile State state = State.CLOSED;
	private Thread connectionThread = null;

	public ClientSocketMessageEndpoint( SSLClient sslClient, String host, int port )
	{
		this.sslClient = sslClient;
		this.host = host;
		this.port = port;
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
		listeners.add( listener );
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		listeners.remove( listener );
	}

	@Override
	public synchronized void open()
	{
		if( state == State.CLOSED )
		{
			state = State.CONNECTING;
			connectionThread = new Thread( new MessageSender() );
			connectionThread.start();
		}
	}

	@Override
	public synchronized void close()
	{
		if( state == State.CONNECTED )
		{
			messageQueue.add( CLOSE_MESSAGE );
		}
		else if( state == State.CONNECTING )
		{
			state = State.CLOSING;
			if( connectionThread != null )
			{
				connectionThread.interrupt();
			}
		}
		else
		{
			state = State.CLOSED;
		}
	}

	private class MessageReceiver implements Runnable
	{
		private final ObjectInputStream ois;

		public MessageReceiver( InputStream inputStream ) throws IOException
		{
			ois = new ObjectInputStream( inputStream );
		}

		@Override
		public void run()
		{
			try
			{
				String channel = null;
				Object data = null;
				while( state == State.CONNECTED && ( channel = ois.readUTF() ) != null
						&& ( data = ois.readObject() ) != null )
				{
					if( "/service/init".equals( channel ) )
					{
						if( !LoadUI.AGENT_VERSION.equals( data ) )
						{
							log.warn( "Cannot connect to server with different version number than the client: {} != {}",
									LoadUI.AGENT_VERSION, data );
							routingSupport.fireMessage( ERROR_CHANNEL, ClientSocketMessageEndpoint.this,
									new VersionMismatchException( data.toString() ) );
						}
					}
					else if( CLOSE_MESSAGE.channel.equals( channel ) )
					{
						synchronized( ClientSocketMessageEndpoint.this )
						{
							state = State.CLOSED;
						}
					}
					//log.debug( "Got message: {}: {}", channel, data );
					routingSupport.fireMessage( channel, ClientSocketMessageEndpoint.this, data );
				}
			}
			catch( ClassNotFoundException e )
			{
				log.error( "Error parsing message:", e );
			}
			catch( IOException e )
			{
				if( state != State.CLOSED )
					log.error( "Connection closed:", e );

				synchronized( ClientSocketMessageEndpoint.this )
				{
					if( state == State.CONNECTED )
					{
						state = State.DISCONNECTED;
						messageQueue.add( CLOSE_MESSAGE );
					}
				}
			}
			finally
			{
				Closeables.closeQuietly( ois );
			}
		}
	}

	private class MessageSender implements Runnable
	{
		private final class MyHandshakeCompletedListener implements HandshakeCompletedListener
		{
			private final Semaphore handshakeCompleted;

			private MyHandshakeCompletedListener( Semaphore handshakeCompleted )
			{
				this.handshakeCompleted = handshakeCompleted;
			}

			@Override
			public void handshakeCompleted( HandshakeCompletedEvent event )
			{
				log.debug( "Handshake completed! {}", event );

				synchronized( ClientSocketMessageEndpoint.this )
				{
					if( state == State.CLOSING )
					{
						messageQueue.add( CLOSE_MESSAGE );
					}

					state = State.CONNECTED;
				}
				handshakeCompleted.release();

				try
				{
					new Thread( new MessageReceiver( socket.getInputStream() ) ).start();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		private SSLSocket socket;

		@Override
		public void run()
		{
			final Semaphore handshakeCompleted = new Semaphore( 0 );

			while( state != State.CLOSED )
			{
				while( state == State.CONNECTING )
				{
					try
					{
						log.debug( "Attempting connection..." );
						socket = ( SSLSocket )sslClient.createSocket( host, port );

						socket.addHandshakeCompletedListener( new MyHandshakeCompletedListener( handshakeCompleted ) );

						socket.startHandshake();
						handshakeCompleted.acquire();
					}
					catch( IOException e )
					{
						if( state != State.CLOSED )
						{
							log.error( "Error connecting socket: {}", e.getMessage() );
							try
							{
								log.debug( "Sleeping for 5s before retrying..." );
								Thread.sleep( 5000 );
							}
							catch( InterruptedException e1 )
							{
							}
						}
					}
					catch( InterruptedException e )
					{
					}
				}

				synchronized( ClientSocketMessageEndpoint.this )
				{
					connectionThread = null;
					if( state == State.CLOSING )
					{
						state = State.CLOSED;
					}
				}

				ObjectOutputStream oos = null;
				try
				{
					if( state == State.CONNECTED )
					{
						sendMessage( "/service/init", LoadUI.AGENT_VERSION );
						for( ConnectionListener listener : ImmutableSet.copyOf( listeners ) )
						{
							listener.handleConnectionChange( ClientSocketMessageEndpoint.this, true );
						}
						oos = new ObjectOutputStream( socket.getOutputStream() );

						Message message = null;
						do
						{
							message = messageQueue.take();
							oos.writeUTF( message.channel );
							oos.writeObject( message.data );
							oos.flush();
						}
						while( message != CLOSE_MESSAGE );
						synchronized( ClientSocketMessageEndpoint.this )
						{
							state = State.CLOSED;
						}
					}
				}
				catch( IOException e )
				{
					log.error( "Sending of messages failed:", e );
				}
				catch( InterruptedException e )
				{
					log.error( "Sending of messages failed:", e );
				}
				finally
				{
					log.debug( "MessageEndpoint disconnected!" );
					for( ConnectionListener listener : ImmutableSet.copyOf( listeners ) )
					{
						listener.handleConnectionChange( ClientSocketMessageEndpoint.this, false );
					}
					Closeables.closeQuietly( oos );
					synchronized( ClientSocketMessageEndpoint.this )
					{
						switch( state )
						{
						case CONNECTED :
							state = State.CLOSED;
							break;
						case DISCONNECTED :
							state = State.CONNECTING;
							break;
						}
					}
				}
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