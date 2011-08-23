package com.eviware.loadui.impl.execution;

import java.util.Set;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Fires expected actions during test execution.
 * 
 * @author dain.nilsson
 */
public class TestExecutionAddon implements Addon, Releasable
{
	private final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );
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

	private TestExecutionAddon( WorkspaceItem workspace )
	{
		testRunner.registerTask( actionTask, Phase.PRE_START, Phase.START, Phase.STOP );
	}

	@Override
	public void release()
	{
		testRunner.unregisterTask( actionTask, Phase.values() );
	}

	public static class Factory implements Addon.Factory<TestExecutionAddon>
	{
		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>> of( WorkspaceItem.class );

		@Override
		public Class<TestExecutionAddon> getType()
		{
			return TestExecutionAddon.class;
		}

		@Override
		public TestExecutionAddon create( Addon.Context context )
		{
			WorkspaceItem workspace = ( WorkspaceItem )Preconditions.checkNotNull( context.getOwner() );
			return new TestExecutionAddon( workspace );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}