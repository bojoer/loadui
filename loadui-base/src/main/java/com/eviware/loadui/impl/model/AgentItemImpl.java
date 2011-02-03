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
package com.eviware.loadui.impl.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.config.AgentItemConfig;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.messaging.MessageEndpointSupport;

public class AgentItemImpl extends ModelItemImpl<AgentItemConfig> implements AgentItem
{
	private final WorkspaceItem workspace;
	private final MessageEndpointProvider provider;
	private final BroadcastMessageEndpoint broadcastEndpoint;
	private MessageEndpointSupport endpointSupport;
	private boolean connected = false;
	private int utilization = 0;

	public AgentItemImpl( WorkspaceItem workspace, AgentItemConfig config )
	{
		super( config );
		this.workspace = workspace;
		broadcastEndpoint = BeanInjector.getBean( BroadcastMessageEndpoint.class );
		provider = BeanInjector.getBean( MessageEndpointProvider.class );
		createProperty( MAX_THREADS_PROPERTY, Long.class, 1000 );
		setupClient();

		if( isEnabled() )
		{
			try
			{
				open();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	private void setupClient()
	{
		if( endpointSupport != null )
			endpointSupport.close();

		endpointSupport = new MessageEndpointSupport( this, provider.createEndpoint( getConfig().getUrl() + "cometd" ) );
		addConnectionListener( new ConnectionListener()
		{
			@Override
			public synchronized void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
			{
				// if state didn't change just exit method
				if( AgentItemImpl.this.connected == connected )
				{
					return;
				}
				// handshake (check if agent is really connected)
				if( connected )
				{
					log.debug( "Check if agent is really connected" );
					// must set to true, to enable sending messages to agent,
					// otherwise isReady() would return false and messages won't be
					// sent at all
					AgentItemImpl.this.connected = true;
					HandshakeAwaiter handshakeAwaiter = new HandshakeAwaiter();
					handshakeAwaiter.await();
					connected = handshakeAwaiter.isSuccess();
					log.debug( "Connected: {}", connected );
				}
				AgentItemImpl.this.connected = connected;
				if( connected )
				{
					broadcastEndpoint.registerEndpoint( AgentItemImpl.this );
					sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.CONNECTED, getHostName() ) );
					log.debug( "Agent connected, setting max threads: {}", getProperty( MAX_THREADS_PROPERTY )
							.getStringValue() );
					sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.SET_MAX_THREADS,
							getProperty( MAX_THREADS_PROPERTY ).getStringValue() ) );
					fireBaseEvent( BECOME_CONNECTED );
				}
				else
				{
					broadcastEndpoint.deregisterEndpoint( AgentItemImpl.this );
					fireBaseEvent( BECOME_DISCONNECTED );
				}
				fireBaseEvent( READY );
				fireBaseEvent( UTILIZATION );

				/*
				 * NOTE 1: added BECOME_CONNECTED and BECOME_DISCONNECTED events
				 * because when using READY listener has to check isReady() and it
				 * happens that other thread change this value in the mean time so
				 * listener will work with wrong value.
				 * 
				 * TODO: sometimes, when user disables agent in GUI, this method
				 * is executed 3 times: first with false, second with true and the
				 * third with false. This is why it needs to be checked if agent is
				 * really up when true is received. In this case timeout will occur
				 * in HandshakeAwaiter.
				 * 
				 * TODO: Quick switching on/off on agent node can make a confusion
				 * and block user interface or at least lead to unexpected
				 * behavior. So maybe some third state should be involved
				 * (connecting/disconnecting) which would prevent user from
				 * disabling agent if it is not enabled fully and vice versa.
				 */
			}
		} );

		addMessageListener( AgentItem.AGENT_CHANNEL, new MessageListener()
		{
			@Override
			@SuppressWarnings( "unchecked" )
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				Map<String, Object> map = ( Map<String, Object> )data;
				if( map.containsKey( SET_UTILIZATION ) )
				{
					utilization = ( ( Number )map.get( SET_UTILIZATION ) ).intValue();
					fireBaseEvent( UTILIZATION );
				}
			}
		} );
	}

	private String getHostName()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch( UnknownHostException e )
		{
			return "Unknown Host";
		}
	}

	@Override
	public String getUrl()
	{
		return getConfig().getUrl();
	}

	@Override
	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@Override
	public final boolean isEnabled()
	{
		return getConfig().getEnabled();
	}

	@Override
	public boolean isReady()
	{
		return connected;
	}

	@Override
	public int getUtilization()
	{
		return isReady() ? utilization : 0;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		if( enabled != isEnabled() )
		{
			getConfig().setEnabled( enabled );
			fireBaseEvent( ENABLED );
			try
			{
				if( enabled )
					open();
				else
					close();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setUrl( String url )
	{
		if( !getConfig().getUrl().equals( url ) )
		{
			getConfig().setUrl( url );
			setupClient();
			fireBaseEvent( URL );
		}
	}

	@Override
	public void addMessageListener( String channel, MessageListener listener )
	{
		endpointSupport.addMessageListener( channel, listener );
	}

	@Override
	public void removeMessageListener( MessageListener listener )
	{
		endpointSupport.removeMessageListener( listener );
	}

	@Override
	public void sendMessage( String channel, Object data )
	{
		// if( !isReady() )
		// throw new RuntimeException(
		// "Message can't be sent unless Agent is ready." );
		if( isReady() )
			endpointSupport.sendMessage( channel, data );
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		endpointSupport.addConnectionListener( listener );
	}

	@Override
	public void open()
	{
		endpointSupport.open();
	}

	@Override
	public void close()
	{
		endpointSupport.close();
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		endpointSupport.removeConnectionListener( listener );
	}

	/**
	 * Blocks current thread until agent is ready for communication. Locks
	 * current thread while another thread sends handshake request to agent in
	 * specified intervals of time and waits for response. When first response is
	 * received it cancels handshake task and releases thread lock.
	 * 
	 * @author predrag.vucetic
	 * 
	 */
	private class HandshakeAwaiter implements MessageListener
	{
		// scheduler to schedule handshakeTask
		private ScheduledExecutorService scheduler = BeanInjector.getBean( ScheduledExecutorService.class );

		// runnable which sends handshake request to agent in specified interval
		private ScheduledFuture<?> handshakeTask;

		// task which releases lock after timeout period if handshake fails
		private ScheduledFuture<?> timeoutTask;

		// latch which waits until handshake response is received from agent
		private CountDownLatch countDownLatch = new CountDownLatch( 1 );

		// set to true on handshake success, false otherwise
		private boolean success;

		public boolean isSuccess()
		{
			return success;
		}

		public void await()
		{
			// set success initial value to false
			success = false;
			// add this as AgentItem message listener
			addMessageListener( AgentItem.AGENT_CHANNEL, this );
			// start timeout task
			startTimeoutSchedulerTask();
			// start task which will send handshake requests to agent
			startHandshakeSchedulerTask();
			try
			{
				// wait until response is received
				countDownLatch.await();
			}
			catch( InterruptedException e )
			{
				// do nothing
			}
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Map<String, Object> map = ( Map<String, Object> )data;
			if( map.containsKey( HANDSHAKE ) )
			{
				// this means that agent has received handshake request and
				// responded which means that agent is ready for communication, so
				// remove listener, cancel handshakeTask and release latch.
				log.debug( "Handshake OK" );
				success = true;
				release();
				timeoutTask.cancel( true );
			}
		}

		private void release()
		{
			removeMessageListener( this );
			handshakeTask.cancel( true );
			countDownLatch.countDown();
		}

		private void startHandshakeSchedulerTask()
		{
			// send handshake message in specified intervals. when agent is ready
			// it will return response which will cancel this task and release
			// latch.
			handshakeTask = scheduler.scheduleAtFixedRate( new Runnable()
			{
				Map<String, String> messageData = Collections.singletonMap( AgentItem.HANDSHAKE, getHostName() );

				@Override
				public void run()
				{
					sendMessage( AgentItem.AGENT_CHANNEL, messageData );
				}
			}, 500, 1000, TimeUnit.MILLISECONDS );
		}

		private void startTimeoutSchedulerTask()
		{
			// starts timeout task which will release current thread lock after
			// a specified time if handshake fails
			timeoutTask = scheduler.schedule( new Runnable()
			{
				@Override
				public void run()
				{
					log.debug( "Handshake timeout" );
					release();
				}
			}, 7, TimeUnit.SECONDS );
		}

	}
}
