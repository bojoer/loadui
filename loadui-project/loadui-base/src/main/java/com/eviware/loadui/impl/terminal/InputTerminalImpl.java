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
package com.eviware.loadui.impl.terminal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.component.ComponentContext.LikeFunction;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;

public class InputTerminalImpl extends TerminalImpl implements InputTerminal
{
	private LikeFunction likeFunction = null;

	public InputTerminalImpl( TerminalHolder owner, String name, String label, String description )
	{
		super( owner, name, label, description );
	}

	@Override
	public Collection<Connection> getConnections()
	{
		Set<Connection> connections = new HashSet<>();
		CanvasItem canvas = getTerminalHolder().getCanvas();
		for( Connection connection : new ArrayList<>( canvas.getConnections() ) )
			if( connection.getInputTerminal() == this )
				connections.add( connection );

		if( canvas instanceof SceneItem && canvas.getProject() != null )
			for( Connection connection : new ArrayList<>( canvas.getProject().getConnections() ) )
				if( connection.getInputTerminal() == this )
					connections.add( connection );

		return connections;
	}

	@Override
	public boolean likes( OutputTerminal outputTerminal )
	{
		return likeFunction != null ? likeFunction.call( outputTerminal ) : false;
	}

	public void setLikeFunction( LikeFunction likeFunction )
	{
		this.likeFunction = likeFunction;
	}
}
