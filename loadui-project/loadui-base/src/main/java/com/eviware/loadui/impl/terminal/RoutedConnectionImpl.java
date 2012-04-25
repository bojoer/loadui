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
package com.eviware.loadui.impl.terminal;

import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalRemoteMessageEvent;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.RoutedConnection;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.eviware.loadui.config.RoutedConnectionConfig;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class RoutedConnectionImpl extends ConnectionImpl implements RoutedConnection
{
	private final static Multiset<OutputTerminal> exportedTerminals = ConcurrentHashMultiset.create();

	private final RoutedConnectionConfig config;
	private final TerminalProxy proxy;
	private TerminalEvent initEvent = null;

	public RoutedConnectionImpl( TerminalProxy proxy, RoutedConnectionConfig config, OutputTerminal output,
			InputTerminal input )
	{
		super( config, output, input );
		this.config = config;
		this.proxy = proxy;
		handleTerminalEvent( initEvent );

		export();
	}

	public RoutedConnectionImpl( TerminalProxy proxy, RoutedConnectionConfig config )
	{
		super( config );
		this.config = config;
		this.proxy = proxy;
	}

	private void export()
	{
		OutputTerminal output = getOutputTerminal();
		CanvasItem canvas = output.getTerminalHolder().getCanvas();
		if( canvas instanceof SceneItem )
		{
			synchronized( exportedTerminals )
			{
				if( exportedTerminals.count( output ) == 0 )
				{
					( ( SceneItem )canvas ).exportTerminal( output );
				}
				exportedTerminals.add( output );
			}
		}
	}

	@Override
	public void disconnect()
	{
		super.disconnect();

		OutputTerminal output = getOutputTerminal();
		CanvasItem canvas = output.getTerminalHolder().getCanvas();
		if( canvas instanceof SceneItem )
		{
			synchronized( exportedTerminals )
			{
				exportedTerminals.remove( output );
				if( exportedTerminals.count( output ) == 0 )
				{
					( ( SceneItem )canvas ).unexportTerminal( output );
				}
			}
		}
	}

	@Override
	public RoutedConnectionConfig getConfig()
	{
		return config;
	}

	@Override
	public String getScript()
	{
		return config.getScript();
	}

	@Override
	public void setScript( String script )
	{
		config.setScript( script );
	}

	@Override
	protected void handleTerminalEvent( TerminalEvent event )
	{
		if( event instanceof TerminalRemoteMessageEvent )
		{
			super.handleTerminalEvent( event );
		}
		else
		{
			if( proxy == null )
				initEvent = event;
			else
			{
				InputTerminal input = getInputTerminal();
				CanvasItem canvas = input.getTerminalHolder().getCanvas();
				if( canvas instanceof SceneItem )
				{
					SceneItem scene = ( SceneItem )input.getTerminalHolder().getCanvas();
					for( AgentItem agent : input.getTerminalHolder().getCanvas().getProject().getAgentsAssignedTo( scene ) )
						proxy.sendTerminalEvent( event, agent, input );
				}
			}
		}
	}
}
