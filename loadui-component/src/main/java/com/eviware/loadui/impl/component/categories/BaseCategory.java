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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.summary.MutableChapter;

/**
 * Base class for component Categories which defines default implementations of
 * all required ComponentBehavior methods (which do nothing).
 * 
 * @author dain.nilsson
 */
public abstract class BaseCategory implements ComponentBehavior
{
	public final static Logger log = LoggerFactory.getLogger( BaseCategory.class );

	private final ComponentContext context;
	private final CancelActionListener listener = new CancelActionListener();

	/**
	 * Constructs a new BaseCategory.
	 * 
	 * @param context
	 *           A ComponentContext to bind the BaseCategory to.
	 */
	public BaseCategory( ComponentContext context )
	{
		this.context = context;
		context.addEventListener( ActionEvent.class, listener );
	}

	/**
	 * Returns the bound ComponentContext.
	 * 
	 * @return The ComponentContext.
	 */
	protected ComponentContext getContext()
	{
		return context;
	}

	/**
	 * Invoked when a request is made to cancel the current action and bring the
	 * Component into a non-busy state.
	 */
	protected void cancel()
	{
	}

	@Override
	public void onRelease()
	{
		context.removeEventListener( ActionEvent.class, listener );
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
	}

	@Override
	public Object collectStatisticsData()
	{
		return null;
	}

	@Override
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData )
	{
	}

	@Override
	public void generateSummary( MutableChapter summary )
	{
	}

	private class CancelActionListener implements EventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( ComponentItem.CANCEL_ACTION.equals( event.getKey() ) )
				cancel();
		}
	}
}
