package com.eviware.loadui.impl.lifecycle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.lifecycle.LifecycleScheduler;
import com.eviware.loadui.api.lifecycle.LifecycleTask;
import com.eviware.loadui.api.lifecycle.Phase;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Hooks into the Lifecycle at each phase, and lets each Agent run its own tasks
 * before reporting back and continuing with the next phase.
 * 
 * @author dain.nilsson
 */
public class AgentLifecycleAddon implements Addon, Releasable
{
	private static final Logger log = LoggerFactory.getLogger( AgentLifecycleAddon.class );

	private final ProjectItem project;
	private final DistributePhaseTask task = new DistributePhaseTask();

	private AgentLifecycleAddon( ProjectItem project )
	{
		this.project = project;

		BeanInjector.getBean( LifecycleScheduler.class ).registerTask( task, Phase.values() );
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( LifecycleScheduler.class ).unregisterTask( task, Phase.values() );
	}

	private class DistributePhaseTask implements LifecycleTask
	{
		@Override
		public void invoke( final ConcurrentMap<String, Object> phaseContext, final Phase phase )
		{
			if( project.getWorkspace().isLocalMode() )
				return;

			HashSet<AgentItem> agents = Sets.newHashSet();
			for( SceneItem scene : project.getScenes() )
			{
				for( AgentItem agent : project.getAgentsAssignedTo( scene ) )
				{
					agents.add( agent );
				}
			}

			HashSet<MessageAwaiter> waiters = Sets.newHashSet();
			for( AgentItem agent : agents )
			{
				waiters.add( new MessageAwaiter( agent, phase ) );
			}

			long waitUntil = System.currentTimeMillis() + 10000;
			for( MessageAwaiter waiter : waiters )
			{
				synchronized( waiter )
				{
					while( !waiter.done )
					{
						try
						{
							long waitTime = waitUntil - System.currentTimeMillis();
							if( waitTime > 0 )
							{
								waiter.wait( waitTime );
							}
							if( !waiter.done )
							{
								log.error( "Timed out waiting for Agent: {}", waiter.agent );
								waiter.complete();
							}
						}
						catch( InterruptedException e )
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private static class MessageAwaiter implements MessageListener, ConnectionListener
	{
		private static final String CHANNEL = "/lifecycleAddon";

		private final AgentItem agent;
		private final Phase phase;
		private boolean done = false;

		private MessageAwaiter( AgentItem agent, Phase phase )
		{
			this.agent = agent;
			this.phase = phase;

			agent.addMessageListener( CHANNEL, this );
			agent.addConnectionListener( this );
			agent.sendMessage( CHANNEL, phase );
		}

		private synchronized void complete()
		{
			agent.removeMessageListener( this );
			agent.removeConnectionListener( this );
			done = true;
			notifyAll();
		}

		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			if( CHANNEL.equals( channel ) && phase == Phase.valueOf( String.valueOf( data ) ) )
			{
				complete();
			}
		}

		@Override
		public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
		{
			complete();
		}
	}

	public static class Factory implements Addon.Factory<AgentLifecycleAddon>
	{
		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( ProjectItem.class );

		@Override
		public Class<AgentLifecycleAddon> getType()
		{
			return AgentLifecycleAddon.class;
		}

		@Override
		public AgentLifecycleAddon create( Addon.Context context )
		{
			ProjectItem project = ( ProjectItem )Preconditions.checkNotNull( context.getOwner() );
			return new AgentLifecycleAddon( project );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
