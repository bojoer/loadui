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
package com.eviware.loadui.impl.component.categories;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.ImmutableMap;

/**
 * Base class for trigger components which defines base behavior which can be
 * extended to fully implement a trigger ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class GeneratorBase extends OnOffBase implements GeneratorCategory
{
	private final OutputTerminal triggerTerminal;
	private final TerminalMessage triggerMessage;

	private final StateListener stateListener = new StateListener();
	private final ActivityTask activityTask = new ActivityTask();

	/**
	 * Constructs an TriggerBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the TriggerBase to.
	 */
	public GeneratorBase( ComponentContext context )
	{
		super( context );

		triggerTerminal = context.createOutput( TRIGGER_TERMINAL, "Trigger Signal",
				"Sends a trigger signal that can trigger e.g. a Runner." );
		triggerMessage = context.newMessage();

		context.setSignature( triggerTerminal,
				ImmutableMap.<String, Class<?>> of( TRIGGER_TIMESTAMP_MESSAGE_PARAM, Long.class ) );

		if( LoadUI.isController() )
		{
			BeanInjector.getBean( TestRunner.class ).registerTask( activityTask, Phase.POST_START, Phase.POST_STOP );
			context.addEventListener( PropertyEvent.class, stateListener );
			fixActivityStrategy( false );
		}
	}

	/**
	 * Outputs a trigger message through the designated trigger OutputTerminal.
	 */
	final public void trigger()
	{
		if( getStateProperty().getValue() )
		{
			triggerMessage.put( TRIGGER_TIMESTAMP_MESSAGE_PARAM, System.currentTimeMillis() );
			getContext().send( triggerTerminal, triggerMessage );
		}
	}

	@Override
	final public OutputTerminal getTriggerTerminal()
	{
		return triggerTerminal;
	}

	@Override
	final public String getCategory()
	{
		return CATEGORY;
	}

	@Override
	final public String getColor()
	{
		return COLOR;
	}

	private void fixActivityStrategy( boolean forceRunning )
	{
		boolean blink = forceRunning || getContext().isRunning();

		getContext().setActivityStrategy(
				getStateProperty().getValue() ? ( blink ? ActivityStrategies.BLINKING : ActivityStrategies.ON )
						: ActivityStrategies.OFF );
	}

	@Override
	public void onRelease()
	{
		super.onRelease();

		if( LoadUI.isController() )
		{
			getContext().removeEventListener( PropertyEvent.class, stateListener );
			BeanInjector.getBean( TestRunner.class ).unregisterTask( activityTask, Phase.values() );
		}
	}

	private class StateListener implements WeakEventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( getStateProperty() == event.getProperty() )
			{
				fixActivityStrategy( false );
			}
		}
	}

	private class ActivityTask implements TestExecutionTask
	{
		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			switch( phase )
			{
			case POST_START :
				if( execution.getCanvas() == getContext().getCanvas()
						|| execution.getCanvas() == getContext().getCanvas().getProject() )
				{
					fixActivityStrategy( true );
				}
				break;
			case POST_STOP :
				fixActivityStrategy( false );
				break;
			}
		}
	}
}
