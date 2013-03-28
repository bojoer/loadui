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
package com.eviware.loadui.api.events;

/**
 * An event signaling that a Collection has changed.
 * 
 * @author dain.nilsson
 */
public class CollectionEvent extends BaseEvent
{
	private static final long serialVersionUID = 5103841571743102137L;

	/**
	 * The type of event which has occurred, such as an element being added or
	 * removed.
	 * 
	 * @author dain.nilsson
	 */
	public static enum Event
	{
		ADDED, REMOVED
	};

	private final Event event;
	private final Object element;

	/**
	 * Constructs a CollectionEvent to be fired.
	 * 
	 * @param source
	 *           The EventFirer which is to fire the event.
	 * @param collection
	 *           The name of the collection property.
	 * @param event
	 *           The type of event which is occurring.
	 * @param element
	 *           The Collection element that this event is in regard to.
	 */
	public CollectionEvent( EventFirer source, String collection, Event event, Object element )
	{
		super( source, collection );
		this.event = event;
		this.element = element;
	}

	/**
	 * Get the type of CollectionEvent.
	 * 
	 * @return The event type.
	 */
	public Event getEvent()
	{
		return event;
	}

	/**
	 * Get the Collection element.
	 * 
	 * @return The element which was modified in the Collection.
	 */
	public Object getElement()
	{
		return element;
	}
}
