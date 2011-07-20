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
