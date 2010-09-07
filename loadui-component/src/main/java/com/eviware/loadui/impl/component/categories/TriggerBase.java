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
package com.eviware.loadui.impl.component.categories;

import java.util.Collections;
import java.util.Map;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.TriggerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.impl.component.ActivityStrategies;

/**
 * Base class for trigger components which defines base behavior which can be
 * extended to fully implement a trigger ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class TriggerBase extends BaseCategory implements TriggerCategory
{
	private final Property<Boolean> stateProperty;
	private final OutputTerminal triggerTerminal;
	private final InputTerminal stateTerminal;
	private final TerminalMessage triggerMessage;

	/**
	 * Constructs an TriggerBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the TriggerBase to.
	 */
	public TriggerBase( ComponentContext context )
	{
		super( context );

		stateProperty = context.createProperty( STATE_PROPERTY, Boolean.class, true );
		triggerTerminal = context.createOutput( TRIGGER_TERMINAL, "Trigger Signal" );
		stateTerminal = context.createInput( STATE_TERMINAL, "Activation Terminal" );
		context.addEventListener( PropertyEvent.class, new PropertyListener() );
		context.addEventListener( ActionEvent.class, new ActionListener() );
		context.setActivityStrategy( stateProperty.getValue() ? ( context.isRunning() ? ActivityStrategies.BLINKING
				: ActivityStrategies.ON ) : ActivityStrategies.OFF );

		triggerMessage = context.newMessage();

		Map<String, Class<?>> signature = Collections.emptyMap();
		context.setSignature( triggerTerminal, signature );
	}

	/**
	 * Outputs a trigger message through the designated trigger OutputTerminal.
	 */
	final public void trigger()
	{
		if( stateProperty.getValue() )
		{
			getContext().send( triggerTerminal, triggerMessage );
		}
		else
		{
			getContext().send( triggerTerminal, triggerMessage );
		}
	}

	@Override
	final public Property<Boolean> getStateProperty()
	{
		return stateProperty;
	}

	@Override
	final public InputTerminal getStateTerminal()
	{
		return stateTerminal;
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

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( input == stateTerminal && message.containsKey( ENABLED_MESSAGE_PARAM ) )
		{
			stateProperty.setValue( message.get( ENABLED_MESSAGE_PARAM ) );
		}
	}

	private class PropertyListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getProperty() == stateProperty && PropertyEvent.Event.VALUE == event.getEvent() )
			{
				getContext().setActivityStrategy(
						stateProperty.getValue() ? ( getContext().isRunning() ? ActivityStrategies.BLINKING
								: ActivityStrategies.ON ) : ActivityStrategies.OFF );
			}
		}
	}

	private class ActionListener implements EventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( event.getKey() == CanvasItem.START_ACTION )
				getContext().setActivityStrategy(
						stateProperty.getValue() ? ActivityStrategies.BLINKING : ActivityStrategies.ON );
		}
	}
}
