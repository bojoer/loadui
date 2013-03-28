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
package com.eviware.loadui.util.test;

import static org.hamcrest.CoreMatchers.not;

import org.hamcrest.Matcher;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.test.matchers.CollectionEventMatcher;
import com.eviware.loadui.util.test.matchers.IsMock;

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
		return new IsMock<>();
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
