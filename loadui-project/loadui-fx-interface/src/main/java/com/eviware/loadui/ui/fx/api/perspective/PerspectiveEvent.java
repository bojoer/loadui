package com.eviware.loadui.ui.fx.api.perspective;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.layout.Region;

@SuppressWarnings( "serial" )
public class PerspectiveEvent extends Event
{
	@SuppressWarnings( "hiding" )
	public static final EventType<PerspectiveEvent> ANY = new EventType<>( Event.ANY, "PERSPECTIVE" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_WORKSPACE = new EventType<>( ANY, "WORKSPACE" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_PROJECT = new EventType<>( ANY, "PROJECT" );
	public static final EventType<PerspectiveEvent> PERSPECTIVE_ANALYSIS = new EventType<>( ANY, "ANALYSIS" );

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
