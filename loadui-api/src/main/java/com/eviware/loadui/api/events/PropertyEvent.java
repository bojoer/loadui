/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.api.events;

import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;

/**
 * An event signaling that a Property has changed for a PropertyHolder.
 * 
 * When constructing a PropertyEvent, an Object argument may be given which has
 * a different relevance depending on the type of event occurring. For the
 * different event types, the argument is the following:
 * 
 * <li>CREATED - null
 * 
 * <li>RENAMED - The previous name of the Property.
 * 
 * <li>DELETED - null
 * 
 * <li>VALUE - The new value of the Property.
 * 
 * @author dain.nilsson
 */
public class PropertyEvent extends BaseEvent
{
	private static final long serialVersionUID = 1844846978773184987L;

	/**
	 * The type of change that is occurring.
	 * 
	 * @author dain.nilsson
	 */
	public static enum Event
	{
		CREATED, RENAMED, DELETED, VALUE, /* VISIBILITY */
	}

	private final Property<?> property;
	private final Event event;
	private final Object previous;

	/**
	 * Constructs a PropertyEvent to be fired.
	 * 
	 * @param source
	 *           The PropertyHolder of the Property.
	 * @param property
	 *           The Property.
	 * @param event
	 *           The event occurring with the Property.
	 * @param previous
	 *           The previous value, depending on the event type. For VALUE this
	 *           is the old value, for RENAMED, this is the old name.
	 */
	public PropertyEvent( PropertyHolder source, Property<?> property, Event event, Object previous )
	{
		super( source, property.getKey() );
		if( property == null )
			throw new IllegalArgumentException( "null property" );
		this.property = property;
		this.event = event;
		this.previous = previous;
	}

	@Override
	public PropertyHolder getSource()
	{
		return ( PropertyHolder )super.getSource();
	}

	/**
	 * Get the type of PropertyEvent.
	 * 
	 * @return The event type.
	 */
	public Event getEvent()
	{
		return event;
	}

	/**
	 * Get the Property for which the event is fired.
	 * 
	 * @return The Property which was changed.
	 */
	public Property<?> getProperty()
	{
		return property;
	}

	/**
	 * Get the argument for the event.
	 * 
	 * @see PropertyEvent
	 * @return The previous value, if applicable.
	 */
	public Object getPreviousValue()
	{
		return previous;
	}
}
