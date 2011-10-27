package com.eviware.loadui.api;

import com.eviware.loadui.api.testevents.TestEvent;

/**
 * Instantiates a TestEvent from the stored TestEvent data, using available
 * EventTest.Factories.
 * 
 * @author dain.nilsson
 */
public interface TestEventRegistry
{
	public TestEvent.Factory<?> lookupFactory( String type );

	public <T extends TestEvent> TestEvent.Factory<T> lookupFactory( Class<T> type );
}
