package com.eviware.loadui.impl.execution;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.events.EventFuture;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

/**
 * Fires expected events during test execution, and keeps execution in sync with
 * existing events.
 * 
 * @author dain.nilsson
 */
public class TestExecutionAddon implements Addon
{
	private TestExecutionAddon()
	{
	}

	private static abstract class AbstractTestExecutionAddon extends TestExecutionAddon
	{
		protected static final Logger log = LoggerFactory.getLogger( TestExecutionAddon.class );

		protected final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );
	}

	private static class WorkspaceTestExecutionAddon extends AbstractTestExecutionAddon implements Releasable
	{
		private final TestExecutionTask actionTask = new TestExecutionTask()
		{
			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				final CanvasItem canvas = execution.getCanvas();
				switch( phase )
				{
				case PRE_START :
					canvas.triggerAction( CanvasItem.COUNTER_RESET_ACTION );
					break;
				case START :
					canvas.triggerAction( CanvasItem.START_ACTION );
					break;
				case STOP :
					canvas.triggerAction( CanvasItem.STOP_ACTION );
					canvas.triggerAction( CanvasItem.COMPLETE_ACTION );
					break;
				}
			}
		};

		private WorkspaceTestExecutionAddon( WorkspaceItem workspace )
		{
			testRunner.registerTask( actionTask, Phase.PRE_START, Phase.START, Phase.STOP );
		}

		@Override
		public void release()
		{
			testRunner.unregisterTask( actionTask, Phase.values() );
		}
	}

	private static class CanvasTestExecutionAddon extends AbstractTestExecutionAddon implements Releasable
	{
		private final CanvasItem canvas;
		private static final Predicate<BaseEvent> isReadyAction = new Predicate<BaseEvent>()
		{
			@Override
			public boolean apply( BaseEvent event )
			{
				return CanvasItem.READY_ACTION.equals( event.getKey() );
			}
		};
		private final TestExecutionTask readyWaiterTask = new TestExecutionTask()
		{
			private EventFuture<BaseEvent> readyFuture;

			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				if( execution.getCanvas() == canvas || execution.getCanvas() == canvas.getProject() )
				{
					if( phase == Phase.PRE_STOP )
					{
						readyFuture = new EventFuture<BaseEvent>( canvas, BaseEvent.class, isReadyAction );
					}
					else if( phase == Phase.POST_STOP )
					{
						try
						{
							readyFuture.get( 10, TimeUnit.SECONDS );
						}
						catch( InterruptedException e )
						{
							log.error( "Failed waiting for READY event", e );
						}
						catch( ExecutionException e )
						{
							log.error( "Failed waiting for READY event", e );
						}
						catch( TimeoutException e )
						{
							log.error( "Failed waiting for READY event", e );
						}
					}
				}
			}
		};

		private CanvasTestExecutionAddon( CanvasItem canvas )
		{
			this.canvas = canvas;
			testRunner.registerTask( readyWaiterTask, Phase.PRE_STOP, Phase.POST_STOP );
		}

		@Override
		public void release()
		{
			testRunner.unregisterTask( readyWaiterTask, Phase.values() );
		}
	}

	public static class Factory implements Addon.Factory<TestExecutionAddon>
	{
		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( WorkspaceItem.class, CanvasItem.class );

		@Override
		public Class<TestExecutionAddon> getType()
		{
			return TestExecutionAddon.class;
		}

		@Override
		public TestExecutionAddon create( Addon.Context context )
		{
			Object owner = Preconditions.checkNotNull( context.getOwner() );
			if( owner instanceof WorkspaceItem )
			{
				return new WorkspaceTestExecutionAddon( ( WorkspaceItem )owner );
			}
			else if( owner instanceof CanvasItem )
			{
				return new CanvasTestExecutionAddon( ( CanvasItem )owner );
			}
			throw new IllegalArgumentException();
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
