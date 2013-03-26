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
package com.eviware.loadui.impl.testevents;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.google.common.collect.Maps;

public class TestEventRegistryImpl implements TestEventRegistry
{
	public static final Logger log = LoggerFactory.getLogger( TestEventRegistryImpl.class );

	private final Map<String, TestEvent.Factory<?>> eventFactories = Maps.newHashMap();

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
