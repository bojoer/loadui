package com.eviware.loadui.impl.execution;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
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
 * Hooks into the TestExecution at each phase, and lets each Agent run its own
 * tasks before reporting back and continuing with the next phase.
 * 
 * @author dain.nilsson
 */
public class AgentTestExecutionAddon implements Addon, Releasable
{
	private static final Logger log = LoggerFactory.getLogger( AgentTestExecutionAddon.class );

	private final ProjectItem project;
	private final DistributePhaseTask task = new DistributePhaseTask();

	private AgentTestExecutionAddon( ProjectItem project )
	{
		this.project = project;

		BeanInjector.getBean( TestRunner.class ).registerTask( task, Phase.values() );
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( TestRunner.class ).unregisterTask( task, Phase.values() );
	}

	private class DistributePhaseTask implements TestExecutionTask
	{
		@Override
		public void invoke( final TestExecution execution, final Phase phase )
		{
			if( project.getWorkspace().isLocalMode() )
				return;

			HashSet<AgentItem> agents = Sets.newHashSet();
			HashSet<MessageAwaiter> waiters = Sets.newHashSet();
			for( SceneItem scene : project.getScenes() )
			{
				for( AgentItem agent : project.getAgentsAssignedTo( scene ) )
				{
					if( agent.isEnabled() && agents.add( agent ) )
					{
						waiters.add( new MessageAwaiter( agent, execution, phase ) );
					}
				}
			}

			for( MessageAwaiter waiter : waiters )
			{
				waiter.await();
			}
		}
	}

	private static class MessageAwaiter implements MessageListener, ConnectionListener
	{
		private static final String CHANNEL = "/agentTestExecutionAddon";
		private static final int TIMEOUT = 10000;

		private final AgentItem agent;
		private final String canvasId;
		private final Phase phase;
		private boolean done = false;
		private long deadline;

		private MessageAwaiter( AgentItem agent, TestExecution execution, Phase phase )
		{
			this.agent = agent;
			this.canvasId = execution.getCanvas().getId();
			this.phase = phase;

			agent.addMessageListener( CHANNEL, this );
			agent.addConnectionListener( this );
			agent.sendMessage( CHANNEL, new Object[] { canvasId, phase.toString() } );
		}

		private synchronized void complete()
		{
			agent.removeMessageListener( this );
			agent.removeConnectionListener( this );
			done = true;
			notifyAll();
		}

		private boolean await()
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
	}

	public static class Factory implements Addon.Factory<AgentTestExecutionAddon>
	{
		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( ProjectItem.class );

		@Override
		public Class<AgentTestExecutionAddon> getType()
		{
			return AgentTestExecutionAddon.class;
		}

		@Override
		public AgentTestExecutionAddon create( Addon.Context context )
		{
			ProjectItem project = ( ProjectItem )Preconditions.checkNotNull( context.getOwner() );
			return new AgentTestExecutionAddon( project );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
