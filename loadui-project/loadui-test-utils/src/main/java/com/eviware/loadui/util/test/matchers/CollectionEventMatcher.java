/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.util.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.eviware.loadui.api.events.CollectionEvent;

/**
 * Matches a CollectionEvent against its collection key and event type.
 * 
 * @author dain.nilsson
 */
public class CollectionEventMatcher extends TypeSafeMatcher<CollectionEvent>
{
	private final String collection;
	private final CollectionEvent.Event event;

	public CollectionEventMatcher( String collection, CollectionEvent.Event event )
	{
		this.collection = collection;
		this.event = event;
	}

	@Override
	public void describeTo( Description description )
	{
		description.appendText( "is " + event + " for " + collection );
	}

	@Override
	protected boolean matchesSafely( CollectionEvent item )
	{
		return item.getEvent() == event && collection.equals( item.getKey() );
	}
}
