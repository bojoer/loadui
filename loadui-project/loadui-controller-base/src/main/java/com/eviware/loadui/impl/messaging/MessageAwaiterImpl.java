package com.eviware.loadui.impl.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageAwaiter;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;

public class MessageAwaiterImpl implements MessageListener, ConnectionListener, MessageAwaiter
{
	private static final int TIMEOUT = 10000;
	public static final String CHANNEL = "/agentTestExecutionAddon";
	private static final Logger log = LoggerFactory.getLogger( MessageAwaiterImpl.class );

	private final AgentItem agent;
	private final String canvasId;
	private final Phase phase;
	private boolean done = false;
	private long deadline;

	public MessageAwaiterImpl( AgentItem agent, String canvasId, Phase phase )
	{
		this.agent = agent;
		this.canvasId = canvasId;
		this.phase = phase;

		agent.addMessageListener( CHANNEL, this );
		agent.addConnectionListener( this );
		agent.sendMessage( CHANNEL, new Object[] { canvasId, phase.toString() } );
	}

	synchronized void complete()
	{
		agent.removeMessageListener( this );
		agent.removeConnectionListener( this );
		done = true;
		notifyAll();
	}

	@Override
	public boolean await()
	{
		synchronized( this )
		{
			deadline = System.currentTimeMillis() + TIMEOUT;
			while( !done )
			{
				try
				{
					if( agent.isReady() )
					{
						wait( deadline - System.currentTimeMillis() );
					}
					if( !done && System.currentTimeMillis() >= deadline )
					{
						log.error( "Timed out waiting for Agent: {}", agent );
						complete();
						return false;
					}
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			return true;
		}
	}

	@Override
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		if( CHANNEL.equals( channel ) )
		{
			Object[] strings = ( Object[] )data;
			if( canvasId.equals( strings[0] ) && phase == Phase.valueOf( ( String )strings[1] ) )
			{
				if( Boolean.parseBoolean( ( String )strings[2] ) )
				{
					complete();
				}
				else
				{
					synchronized( this )
					{
						deadline = System.currentTimeMillis() + TIMEOUT;
					}
				}
			}
		}
	}

	@Override
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
	{
		complete();
	}
	
	
	public static class Factory implements MessageAwaiter.MessageAwaiterFactory {

		@Override
		public MessageAwaiter create( AgentItem agent, String canvasId, Phase phase )
		{
			return new MessageAwaiterImpl( agent, canvasId, phase );
		}
		
	}
	
	
	
}