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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
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
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

@ThreadSafe
public class ClientSocketMessageEndpoint implements MessageEndpoint
{
	private enum TargetState
	{
		OPEN, CLOSED
	}

	private enum State
	{
		DISCONNECTED, CONNECTING, CONNECTED
	}

	protected static final Logger log = LoggerFactory.getLogger( ClientSocketMessageEndpoint.class );
	private static final Message CLOSE_MESSAGE = new Message( "/service/close", null );

	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
	private final Set<ConnectionListener> listeners = Sets.newCopyOnWriteArraySet();
	private final BlockingQueue<Message> messageQueue = Queues.newLinkedBlockingQueue();
	private final SSLClient sslClient;
	private final String host;
	private final int port;

	private volatile TargetState targetState = TargetState.CLOSED;
	private volatile State state = State.DISCONNECTED;
	@GuardedBy( "this" )
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

	private synchronized void nullifyConnectionThread()
	{
		connectionThread = null;
	}

	@Override
	public synchronized void open()
	{
		if( targetState == TargetState.CLOSED )
		{
			targetState = TargetState.OPEN;
			if( state == State.DISCONNECTED )
			{
				state = State.CONNECTING;
				connectionThread = new Thread( new MessageSender() );
				connectionThread.start();
			}
		}
	}

	@Override
	public synchronized void close()
	{
		if( targetState == TargetState.OPEN )
		{
			targetState = TargetState.CLOSED;
			if( state == State.CONNECTED )
			{
				messageQueue.add( CLOSE_MESSAGE );
			}
			else if( state == State.CONNECTING )
			{
				if( connectionThread != null )
				{
					connectionThread.interrupt();
				}
			}
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
						state = State.DISCONNECTED;
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
				if( targetState != TargetState.CLOSED )
					log.error( "Connection closed:", e );

				if( state == State.CONNECTED )
				{
					state = State.DISCONNECTED;
					messageQueue.add( CLOSE_MESSAGE );
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

				if( targetState == TargetState.CLOSED )
				{
					messageQueue.add( CLOSE_MESSAGE );
				}
				state = State.CONNECTED;
				handshakeCompleted.release();

				try
				{
					new Thread( new MessageReceiver( event.getSocket().getInputStream() ) ).start();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run()
		{
			while( targetState == TargetState.OPEN )
			{
				SSLSocket socket = null;

				try
				{
					socket = connect();

					if( state == State.CONNECTED )
					{
						sendMessages( socket );
					}
				}
				finally
				{
					//TODO: If/When we move to Java 7, use closeQuietly instead of the try-catch.
					//Closeables.closeQuietly( socket );
					try
					{
						if( socket != null )
						{
							socket.close();
						}
					}
					catch( IOException e )
					{
						//Ignore
					}
				}

				log.debug( "MessageEndpoint disconnected!" );
				for( ConnectionListener listener : listeners )
				{
					listener.handleConnectionChange( ClientSocketMessageEndpoint.this, false );
				}
			}
		}

		private void sendMessages( SSLSocket socket )
		{
			assert socket != null;

			ObjectOutputStream oos = null;

			try
			{
				sendMessage( "/service/init", LoadUI.AGENT_VERSION );
				for( ConnectionListener listener : listeners )
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
				state = State.DISCONNECTED;
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
				Closeables.closeQuietly( oos );

				log.debug( "MessageEndpoint disconnected!" );
				for( ConnectionListener listener : listeners )
				{
					listener.handleConnectionChange( ClientSocketMessageEndpoint.this, false );
				}
			}
		}

		private SSLSocket connect()
		{
			SSLSocket socket = null;

			while( targetState == TargetState.OPEN && state != State.CONNECTED )
			{
				try
				{
					log.debug( "Attempting connection..." );
					socket = ( SSLSocket )sslClient.createSocket( host, port );

					Semaphore handshakeCompleted = new Semaphore( 0 );
					socket.addHandshakeCompletedListener( new MyHandshakeCompletedListener( handshakeCompleted ) );

					socket.startHandshake();
					handshakeCompleted.acquire();
				}
				catch( IOException e )
				{
					//TODO: If/When we move to Java 7, use closeQuietly instead of the try-catch.
					//Closeables.closeQuietly( socket );
					try
					{
						if( socket != null )
						{
							socket.close();
						}
					}
					catch( IOException e2 )
					{
						//Ignore
					}

					if( targetState == TargetState.OPEN )
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

			nullifyConnectionThread();

			return socket;
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