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
package com.eviware.loadui.impl.terminal;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalRemoteMessageEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.RoutedConnection;
import com.eviware.loadui.api.terminal.TerminalProxy;
import com.eviware.loadui.config.RoutedConnectionConfig;

public class RoutedConnectionImpl extends ConnectionImpl implements RoutedConnection
{
	private final static Map<OutputTerminal, Integer> exportedTerminalCounts = new HashMap<OutputTerminal, Integer>();

	private final RoutedConnectionConfig config;
	private final TerminalProxy proxy;
	private TerminalEvent initEvent;

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
			synchronized( exportedTerminalCounts )
			{
				if( !exportedTerminalCounts.containsKey( output ) )
					exportedTerminalCounts.put( output, 0 );

				int refs = exportedTerminalCounts.get( output );
				if( refs == 0 )
					( ( SceneItem )canvas ).exportTerminal( output );

				exportedTerminalCounts.put( output, refs + 1 );
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
			synchronized( exportedTerminalCounts )
			{
				if( exportedTerminalCounts.put( output, exportedTerminalCounts.get( output ) - 1 ) == 1 )
					( ( SceneItem )canvas ).unexportTerminal( output );
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
