package com.eviware.loadui.ui.fx.api.intent;

import javafx.event.Event;
import javafx.event.EventType;

import com.eviware.loadui.api.traits.Labeled;

@SuppressWarnings( "serial" )
public class IntentEvent<T> extends Event
{
	@SuppressWarnings( "hiding" )
	public static final EventType<IntentEvent<? extends Object>> ANY = new EventType<>( Event.ANY, "INTENT" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_OPEN = new EventType<>( ANY, "INTENT_OPEN" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CLOSE = new EventType<>( ANY, "INTENT_CLOSE" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CREATE = new EventType<>( ANY, "INTENT_CREATE" );

	public static final EventType<IntentEvent<? extends Labeled.Mutable>> INTENT_RENAME = new EventType<>( ANY,
			"INTENT_RENAME" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CLONE = new EventType<>( ANY, "INTENT_CLONE" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_DELETE = new EventType<>( ANY, "INTENT_DELETE" );

	public static final EventType<IntentEvent<? extends Runnable>> INTENT_RUN_BLOCKING = new EventType<>( ANY,
			"INTENT_RUN_BLOCKING" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_SAVE = new EventType<>( ANY, "INTENT_SAVE" );

	private final T arg;

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static <T> IntentEvent<T> create( EventType<IntentEvent<? extends T>> eventType, T arg )
	{
		return new IntentEvent( eventType, arg );
	}

	private IntentEvent( EventType<IntentEvent<T>> eventType, T arg )
	{
		super( NULL_SOURCE_TARGET, NULL_SOURCE_TARGET, eventType );
		this.arg = arg;
	}

	public T getArg()
	{
		return arg;
	}
}
