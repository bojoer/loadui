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
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.ActivityStrategies;

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

	private final ActivityListener listener = new ActivityListener();

	/**
	 * Constructs an TriggerBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the TriggerBase to.
	 */
	public GeneratorBase( ComponentContext context )
	{
		super( context );

		triggerTerminal = context.createOutput( TRIGGER_TERMINAL, "Trigger Signal" );
		triggerMessage = context.newMessage();

		Map<String, Class<?>> signature = Collections.emptyMap();
		context.setSignature( triggerTerminal, signature );

		context.addEventListener( BaseEvent.class, listener );
		fixActivityStrategy();
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

	@Override
	public void onRelease()
	{
		super.onRelease();

		getContext().removeEventListener( BaseEvent.class, listener );
	}

	private void fixActivityStrategy()
	{
		getContext().setActivityStrategy(
				getStateProperty().getValue() ? ( getContext().isRunning() ? ActivityStrategies.BLINKING
						: ActivityStrategies.ON ) : ActivityStrategies.OFF );
	}

	private class ActivityListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof ActionEvent
					&& ( event.getKey() == CanvasItem.START_ACTION || event.getKey() == CanvasItem.STOP_ACTION )
					|| event instanceof PropertyEvent && ( ( PropertyEvent )event ).getProperty() == getStateProperty() )
				fixActivityStrategy();
		}
	}
}
