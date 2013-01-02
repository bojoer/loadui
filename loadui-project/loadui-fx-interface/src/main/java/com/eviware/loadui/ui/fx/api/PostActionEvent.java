package com.eviware.loadui.ui.fx.api;

import javafx.event.Event;
import javafx.event.EventType;

@SuppressWarnings( "serial" )
public class PostActionEvent<T> extends Event
{
	@SuppressWarnings( "hiding" )
	public static final EventType<PostActionEvent<? extends Object>> ANY = new EventType<>( Event.ANY, "INTENT" );

	public static final EventType<PostActionEvent<? extends Object>> WAS_CREATED = new EventType<>( ANY, "WAS_CREATED" );

	private final T arg;

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static <T> PostActionEvent<T> create( EventType<PostActionEvent<? extends T>> eventType, T arg )
	{
		return new PostActionEvent( eventType, arg );
	}

	private PostActionEvent( EventType<PostActionEvent<T>> eventType, T arg )
	{
		super( NULL_SOURCE_TARGET, NULL_SOURCE_TARGET, eventType );
		this.arg = arg;
	}

	public T getArg()
	{
		return arg;
	}

	@Override
	public String toString()
	{
		return getEventType() + "[arg=" + arg + "]";
	}
}