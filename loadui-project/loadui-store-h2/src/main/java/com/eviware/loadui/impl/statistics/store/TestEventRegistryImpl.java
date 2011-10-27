package com.eviware.loadui.impl.statistics.store;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.TestEventRegistry;
import com.eviware.loadui.api.testevents.TestEvent;
import com.google.common.collect.Maps;

public class TestEventRegistryImpl implements TestEventRegistry
{
	public static final Logger log = LoggerFactory.getLogger( TestEventRegistryImpl.class );

	private final HashMap<String, TestEvent.Factory<?>> eventFactories = Maps.newHashMap();

	public synchronized void factoryAdded( TestEvent.Factory<?> factory, Map<String, String> properties )
	{
		eventFactories.put( factory.getType().getName(), factory );
		log.debug( "TestEvent.Factory registered for type: {}", factory.getType() );
	}

	public synchronized void factoryRemoved( TestEvent.Factory<?> factory, Map<String, String> properties )
	{
		eventFactories.remove( factory.getType().getName() );
		log.debug( "TestEvent.Factory unregistered for type: {}", factory.getType() );
	}

	@Override
	public TestEvent.Factory<?> lookupFactory( String type )
	{
		return eventFactories.get( type );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T extends TestEvent> TestEvent.Factory<T> lookupFactory( Class<T> type )
	{
		return ( TestEvent.Factory<T> )eventFactories.get( type.getName() );
	}
}
