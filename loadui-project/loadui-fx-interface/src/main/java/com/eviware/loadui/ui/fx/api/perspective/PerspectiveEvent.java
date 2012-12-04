package com.eviware.loadui.ui.fx.api.perspective;

import javafx.event.Event;
import javafx.event.EventType;

@SuppressWarnings( "serial" )
public class PerspectiveEvent extends Event
{
	@SuppressWarnings( "hiding" )
	public static final EventType<PerspectiveEvent> ANY = new EventType<>( Event.ANY, "PERSPECTIVE" );

	public static final EventType<PerspectiveEvent> PERSPECTIVE_WORKSPACE = new EventType<>( ANY, "WORKSPACE" );

	public static final EventType<PerspectiveEvent> PERSPECTIVE_PROJECT = new EventType<>( ANY, "PROJECT" );

	public PerspectiveEvent( EventType<? extends PerspectiveEvent> type )
	{
		super( NULL_SOURCE_TARGET, NULL_SOURCE_TARGET, type );
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
