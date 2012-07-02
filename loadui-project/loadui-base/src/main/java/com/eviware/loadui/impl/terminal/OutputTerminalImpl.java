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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.events.TerminalSignatureEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.terminal.TerminalMessage;

public class OutputTerminalImpl extends TerminalImpl implements OutputTerminal
{
	private Map<String, Class<?>> signature = Collections.emptyMap();

	public OutputTerminalImpl( TerminalHolder owner, String name, String label, String description )
	{
		super( owner, name, label, description );
	}

	public void setMessageSignature( Map<String, Class<?>> signature )
	{
		if( ( this.signature != null && this.signature.equals( signature ) )
				|| ( this.signature == null && signature == null ) )
			return;

		this.signature = Collections.unmodifiableMap( signature );
		fireEvent( new TerminalSignatureEvent( this, signature ) );
	}

	public void sendMessage( TerminalMessage message )
	{
		fireEvent( new TerminalMessageEvent( this, message ) );
	}

	@Override
	public Connection connectTo( InputTerminal input )
	{
		return getTerminalHolder().getCanvas().connect( this, input );
	}

	@Override
	public Map<String, Class<?>> getMessageSignature()
	{
		return signature;
	}

	@Override
	public Collection<Connection> getConnections()
	{
		Set<Connection> connections = new HashSet<>();
		CanvasItem canvas = getTerminalHolder().getCanvas();
		for( Connection connection : new ArrayList<>( canvas.getConnections() ) )
			if( connection.getOutputTerminal() == this )
				connections.add( connection );

		if( canvas instanceof SceneItem && canvas.getProject() != null )
			for( Connection connection : new ArrayList<>( canvas.getProject().getConnections() ) )
				if( connection.getOutputTerminal() == this )
					connections.add( connection );

		return connections;
	}
}
