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
package com.eviware.loadui.ui.fx.api.perspective;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.layout.Region;

@SuppressWarnings( "serial" )
public class PerspectiveEvent extends Event
{
	public static final EventType<PerspectiveEvent> ANY = new EventType<>( Event.ANY, "PERSPECTIVE" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_WORKSPACE = new EventType<>( ANY, "WORKSPACE" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_PROJECT = new EventType<>( ANY, "PROJECT" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_ANALYSIS = new EventType<>( PERSPECTIVE_PROJECT,
			"ANALYSIS" );

	private static final Node eventProxy = new Region();

	public static void fireEvent( EventType<PerspectiveEvent> type, Node perspectiveNode )
	{
		eventProxy.fireEvent( new PerspectiveEvent( type, perspectiveNode ) );
	}

	public static void addEventHandler( EventType<PerspectiveEvent> type, EventHandler<PerspectiveEvent> handler )
	{
		eventProxy.addEventHandler( type, handler );
	}

	private final Node perspectiveNode;

	public PerspectiveEvent( EventType<? extends PerspectiveEvent> type, Node perspectiveNode )
	{
		super( NULL_SOURCE_TARGET, NULL_SOURCE_TARGET, type );
		this.perspectiveNode = perspectiveNode;
	}

	public Node getPerspectiveNode()
	{
		return perspectiveNode;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public EventType<? extends PerspectiveEvent> getEventType()
	{
		return ( EventType<? extends PerspectiveEvent> )super.getEventType();
	}

	@Override
	public String toString()
	{
		return "Perspective[" + getEventType().getName() + "]";
	}

	@SuppressWarnings( "unchecked" )
	public static String getPath( EventType<? extends PerspectiveEvent> type )
	{
		if( type == ANY )
		{
			return ANY.getName();
		}
		return getPath( ( EventType<? extends PerspectiveEvent> )type.getSuperType() ) + "." + type.getName();
	}

	public static boolean isChildOf( EventType<? extends PerspectiveEvent> ancestor,
			EventType<? extends PerspectiveEvent> child )
	{
		EventType<?> type = child;
		while( type != null )
		{
			if( type == Event.ANY )
			{
				return false;
			}
			if( type == ancestor )
			{
				return true;
			}

			type = type.getSuperType();
		}
		return false;
	}
}
