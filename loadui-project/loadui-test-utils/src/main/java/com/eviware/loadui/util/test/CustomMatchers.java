package com.eviware.loadui.util.test;

import static org.hamcrest.CoreMatchers.not;

import org.hamcrest.Matcher;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.test.matchers.IsMock;
import com.eviware.loadui.util.test.matchers.CollectionEventMatcher;

/**
 * Class for static methods for creating custom Matchers.
 * 
 * @author dain.nilsson
 */
public class CustomMatchers
{
	/**
	 * Verifies that the object to match is a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> mockObject()
	{
		return new IsMock<T>();
	}

	/**
	 * Verifies that the object to match is not a mock.
	 * 
	 * @return
	 */
	public static <T> Matcher<T> notMockObject()
	{
		return not( new IsMock<T>() );
	}

	/**
	 * Matches a CollectionEvent against its collection key and event type.
	 * 
	 * @param collection
	 * @param event
	 * @return
	 */
	public static CollectionEventMatcher matchesCollectionEvent( String collection, CollectionEvent.Event event )
	{
		return new CollectionEventMatcher( collection, event );
	}
}
