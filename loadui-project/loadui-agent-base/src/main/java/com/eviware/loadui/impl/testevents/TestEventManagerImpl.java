package com.eviware.loadui.impl.testevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Source;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.util.testevents.AbstractTestEventManager;

public class TestEventManagerImpl extends AbstractTestEventManager
{
	private static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	public TestEventManagerImpl( TestEventRegistry testEventRegistry )
	{
		super( testEventRegistry );
	}

	@Override
	public <T extends TestEvent> void logTestEvent( Source<T> source, T testEvent )
	{
		// TODO Auto-generated method stub
		log.debug( "TODO: Send TestEvent: {}", testEvent );
	}
}
