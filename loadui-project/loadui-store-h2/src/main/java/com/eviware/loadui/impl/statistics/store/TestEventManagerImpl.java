package com.eviware.loadui.impl.statistics.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.TestEventRegistry;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.BeanInjector;

public class TestEventManagerImpl implements TestEventManager
{
	public static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	private final ExecutionManagerImpl manager;
	private final TestEventRegistry testEventRegistry;

	public TestEventManagerImpl( ExecutionManagerImpl manager )
	{
		this.manager = manager;
		testEventRegistry = BeanInjector.getBean( TestEventRegistry.class );
	}

	@Override
	public <T extends TestEvent> void logTestEvent( TestEvent.Source<T> source, T testEvent )
	{
		TestEvent.Factory<T> factory = testEventRegistry.lookupFactory( testEvent.<T> getType() );

		if( factory != null )
		{
			manager.writeTestEvent( factory.getLabel(), source, testEvent.getTimestamp(),
					factory.getDataForTestEvent( testEvent ) );
		}
		else
		{
			log.warn( "No TestEvent.Factory capable of storing TestEvent: {}, of type: {} has been registered!",
					testEvent, testEvent.getType() );
		}
	}
}
