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
package com.eviware.loadui.ui.fx.api.input;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;

@SuppressWarnings( "serial" )
public class DraggableEvent extends InputEvent
{
	public static final EventType<DraggableEvent> ANY = new EventType<>( InputEvent.ANY, "DRAGGABLE" );

	/**
	 * Called when a Node being dragged enters a potential drop target.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_ENTERED = new EventType<>( ANY, "DRAGGABLE_ENTERED" );

	/**
	 * Called when a Node starts being dragged.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_STARTED = new EventType<>( ANY, "DRAGGABLE_STARTED" );

	/**
	 * Called when a Node being dragged is moved.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_DRAGGED = new EventType<>( ANY, "DRAGGABLE_DRAGGED" );

	/**
	 * Called when a Node being dragged released, regardless if it was accepted
	 * or not.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_STOPPED = new EventType<>( ANY, "DRAGGABLE_STOPPED" );

	/**
	 * Called when a Node being dragged exits a potential drop target, being
	 * moved outside or dropped.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_EXITED = new EventType<>( ANY, "DRAGGABLE_EXITED" );
	/**
	 * Called when a Node has been dropped onto a target, where the
	 * DRAGGABLE_ENTERED event was previously accepted.
	 */
	public static final EventType<DraggableEvent> DRAGGABLE_DROPPED = new EventType<>( ANY, "DRAGGABLE_DROPPED" );

	private final double sceneX;
	private final double sceneY;
	private final Runnable onAccept;
	private final Draggable draggable;
	private boolean accepted = false;

	public DraggableEvent( Runnable onAccept, Node source, EventTarget target,
			EventType<? extends DraggableEvent> eventType, Draggable draggable, double x, double y )
	{
		super( source, target, eventType );

		this.onAccept = onAccept;
		this.draggable = draggable;
		this.sceneX = x;
		this.sceneY = y;
	}

	public double getSceneX()
	{
		return sceneX;
	}

	public double getSceneY()
	{
		return sceneY;
	}

	public Draggable getDraggable()
	{
		return draggable;
	}

	public Object getData()
	{
		return draggable == null ? null : draggable.getData();
	}

	public boolean isAccepted()
	{
		return accepted;
	}

	public void accept()
	{
		if( getEventType() != DRAGGABLE_ENTERED )
		{
			throw new IllegalStateException( "DraggableEvent.accept() can only be called from a DRAGGABLE_ENTERED event" );
		}
		if( !accepted && onAccept != null )
		{
			onAccept.run();
		}
		accepted = true;
	}
}
