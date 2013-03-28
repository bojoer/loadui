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
package com.eviware.loadui.ui.fx.api;

import javafx.event.Event;
import javafx.event.EventType;

@SuppressWarnings( "serial" )
public class PostActionEvent<T> extends Event
{
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
