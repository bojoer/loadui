/*
 * Copyright 2011 eviware software ab
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.VersionMismatchException;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.config.AgentItemConfig;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.messaging.MessageEndpointSupport;
import com.google.common.collect.ImmutableMap;

public class AgentItemImpl extends ModelItemImpl<AgentItemConfig> implements AgentItem
{
	private final WorkspaceItem workspace;
	private final MessageEndpointProvider provider;
	private final BroadcastMessageEndpoint broadcastEndpoint;
	private final ScheduledExecutorService executorService;
	private final ScheduledFuture<?> timeSynchronizerFuture;
	private MessageEndpointSupport endpointSupport;
	private boolean connected = false;
	private int utilization = 0;
	private long timeDiff = 0;
	private long fastestTimeCheck = Long.MAX_VALUE;

	public AgentItemImpl( WorkspaceItem workspace, AgentItemConfig config )
	{
		super( config );
		this.workspace = workspace;
		broadcastEndpoint = BeanInjector.getBean( BroadcastMessageEndpoint.class );
		provider = BeanInjector.getBean( MessageEndpointProvider.class );
		executorService = BeanInjector.getBean( ScheduledExecutorService.class );
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

		timeSynchronizerFuture = executorService.scheduleAtFixedRate( new TimeSynchronizer(), 5, 5, TimeUnit.MINUTES );
	}

	private void setupClient()
	{
		if( endpointSupport != null )
			endpointSupport.close();

		endpointSupport = new MessageEndpointSupport( this, provider.createEndpoint( getConfig().getUrl() + "cometd" ) );
		addConnectionListener( new ConnectionListener()
		{
			@Override
			public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
			{
				if( connected )
				{
					broadcastEndpoint.registerEndpoint( AgentItemImpl.this );
				}
				else
				{
					broadcastEndpoint.deregisterEndpoint( AgentItemImpl.this );
					AgentItemImpl.this.connected = false;
					fireBaseEvent( READY );
				}
			}
		} );

		addMessageListener( AgentItem.AGENT_CHANNEL, new MessageListener()
		{
			@Override
			@SuppressWarnings( "unchecked" )
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				Map<String, Object> map = ( Map<String, Object> )data;
				if( map.containsKey( TIME_CHECK ) )
				{
					long currentTime = System.currentTimeMillis();
					long agentTime = Long.parseLong( ( String )map.get( TIME_CHECK ) );
					long roundTripTime = currentTime - Long.parseLong( ( String )map.get( "startTimeCheck" ) );
					if( roundTripTime < fastestTimeCheck )
					{
						fastestTimeCheck = roundTripTime;
						timeDiff = currentTime - ( agentTime + roundTripTime / 2 );
						log.debug( "{} TimeDiff updated to {}. RTT: {}, times: {}, {}, {}", new Object[] {
								AgentItemImpl.this, timeDiff, roundTripTime, currentTime - roundTripTime, agentTime,
								currentTime } );
						sendTimeCheck();
					}
				}
				if( map.containsKey( CONNECTED ) )
				{
					String version = map.get( CONNECTED ) == null ? "0" : map.get( CONNECTED ).toString();
					if( !LoadUI.AGENT_VERSION.equals( version ) )
					{
						endpointSupport.fireError( new VersionMismatchException( version ) );
					}

					String hostName;
					try
					{
						hostName = InetAddress.getLocalHost().getHostName();
					}
					catch( UnknownHostException e )
					{
						hostName = "Unknown Host";
					}
					sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.CONNECTED, hostName ) );
					log.debug( "Agent connected, setting max threads: {}", getProperty( MAX_THREADS_PROPERTY )
							.getStringValue() );
					sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.SET_MAX_THREADS,
							getProperty( MAX_THREADS_PROPERTY ).getStringValue() ) );
					AgentItemImpl.this.connected = true;
					fireBaseEvent( READY );
					fireBaseEvent( UTILIZATION );
					resetTimeDifference();
				}
				else if( map.containsKey( SET_UTILIZATION ) )
				{
					utilization = ( ( Number )map.get( SET_UTILIZATION ) ).intValue();
					fireBaseEvent( UTILIZATION );
				}
			}
		} );

		addMessageListener( MessageEndpoint.ERROR_CHANNEL, new MessageListener()
		{
			@Override
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				if( data instanceof VersionMismatchException )
				{
					log.warn( "Unable to connect to agent {}: {}", getLabel(),
							( ( VersionMismatchException )data ).getMessage() );
					setEnabled( false );
				}
			}
		} );
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
	public long getTimeDifference()
	{
		return timeDiff;
	}

	@Override
	public void resetTimeDifference()
	{
		fastestTimeCheck = Long.MAX_VALUE;
		sendTimeCheck();
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
	final public void open()
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

	@Override
	public void release()
	{
		super.release();
		ReleasableUtils.release( endpointSupport );

		timeSynchronizerFuture.cancel( true );
	}

	private void sendTimeCheck()
	{
		Map<String, String> data = ImmutableMap.of( TIME_CHECK, "", "startTimeCheck",
				String.valueOf( System.currentTimeMillis() ) );

		sendMessage( AGENT_CHANNEL, data );
	}

	private class TimeSynchronizer implements Runnable
	{
		@Override
		public void run()
		{
			if( isReady() )
				resetTimeDifference();
		}
	}
}