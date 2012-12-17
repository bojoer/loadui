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

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.SchedulerCategory;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.google.common.collect.ImmutableMap;

public abstract class SchedulerBase extends OnOffBase implements SchedulerCategory
{
	private final OutputTerminal output;

	private final TerminalMessage onMessage;
	private final TerminalMessage offMessage;

	private final PropertyListener listener = new PropertyListener();

	public SchedulerBase( ComponentContext context )
	{
		super( context );

		output = context.createOutput( OUTGOING_TERMINAL, "Scheduling Terminal",
				"Connect to the Component activation terminal of other components to enable/disable them." );
		context.setSignature( output, ImmutableMap.<String, Class<?>> of( ENABLED_MESSAGE_PARAM, Boolean.class ) );

		onMessage = context.newMessage();
		onMessage.put( ENABLED_MESSAGE_PARAM, true );
		offMessage = context.newMessage();
		offMessage.put( ENABLED_MESSAGE_PARAM, false );

		context.getComponent().addEventListener( PropertyEvent.class, listener );
		context.setActivityStrategy( getStateProperty().getValue() ? ActivityStrategies.ON : ActivityStrategies.OFF );
	}

	public final void sendEnabled( boolean status )
	{
		final boolean state = getStateProperty().getValue();

		if( status && !state )
			return;

		if( status && state )
		{
			getContext().send( output, onMessage );
			getContext().setActivityStrategy( ActivityStrategies.BLINKING );
		}
		else if( !status )
		{
			getContext().send( output, offMessage );
			getContext().setActivityStrategy( state ? ActivityStrategies.ON : ActivityStrategies.OFF );
		}
	}

	@Override
	public OutputTerminal getOutputTerminal()
	{
		return output;
	}

	@Override
	public String getCategory()
	{
		return CATEGORY;
	}

	@Override
	public String getColor()
	{
		return COLOR;
	}

	@Override
	public void onRelease()
	{
		super.onRelease();

		getContext().removeEventListener( PropertyEvent.class, listener );
	}

	private class PropertyListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getProperty() == getStateProperty() && PropertyEvent.Event.VALUE == event.getEvent() )
			{
				getContext().setActivityStrategy(
						getStateProperty().getValue() ? ActivityStrategies.ON : ActivityStrategies.OFF );
			}
		}
	}
}