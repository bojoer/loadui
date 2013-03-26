/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Map;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.DualTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;

abstract class DummyTerminal implements DualTerminal
{
	protected final ComponentItemImpl componentItem;

	/**
	 * @param componentItemImpl
	 */
	DummyTerminal( ComponentItemImpl componentItemImpl )
	{
		componentItem = componentItemImpl;
	}

	@Override
	public Connection connectTo( InputTerminal input )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Class<?>> getMessageSignature()
	{
		return Collections.emptyMap();
	}

	@Override
	public Collection<Connection> getConnections()
	{
		return Collections.emptyList();
	}

	@Override
	public TerminalHolder getTerminalHolder()
	{
		return componentItem;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEventListeners()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId()
	{
		return componentItem.getId() + "/" + getName();
	}

	@Override
	public String getLabel()
	{
		return getName();
	}

	@Override
	public String getDescription()
	{
		return "A special Terminal which can be used to send messages to remote instances of the ComponentItem itself.";
	}

	@Override
	public void setDescription( String description )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLabel( String label )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean likes( OutputTerminal outputTerminal )
	{
		return false;
	}

	static class RemoteTerminal extends DummyTerminal
	{
		RemoteTerminal( ComponentItemImpl componentItemImpl )
		{
			super( componentItemImpl );
		}

		@Override
		public String getName()
		{
			return ComponentContext.REMOTE_TERMINAL;
		}
	}

	static class AgentTerminal extends DummyTerminal
	{
		private final AgentItem agent;

		public AgentTerminal( ComponentItemImpl componentItem, AgentItem agent )
		{
			super( componentItem );
			this.agent = agent;
		}

		public AgentItem getAgent()
		{
			return agent;
		}

		@Override
		public String getName()
		{
			return agent.getLabel();
		}

		@Override
		public String getId()
		{
			return componentItem.getId() + "/" + agent.getId();
		}
	}

	static class ControllerTerminal extends DummyTerminal
	{
		ControllerTerminal( ComponentItemImpl componentItem )
		{
			super( componentItem );
		}

		@Override
		public String getName()
		{
			return ComponentContext.CONTROLLER_TERMINAL;
		}
	}

}
