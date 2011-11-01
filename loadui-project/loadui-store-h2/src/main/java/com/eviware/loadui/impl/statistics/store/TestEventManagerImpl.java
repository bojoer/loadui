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
