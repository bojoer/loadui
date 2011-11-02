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

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.TestEventRegistry;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Factory;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventEntryImpl;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.MapMaker;

public class TestEventManagerImpl implements TestEventManager
{
	public static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	private final ExecutionManagerImpl manager;
	private final TestEventRegistry testEventRegistry;
	private final Set<TestEventObserver> observers = Collections.newSetFromMap( new MapMaker().weakKeys()
			.<TestEventObserver, Boolean> makeMap() );

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

			TestEvent.Entry entry = new TestEventEntryImpl( testEvent, source.getLabel(), factory.getLabel() );
			for( TestEventObserver observer : observers )
			{
				observer.onTestEvent( entry );
			}
		}
		else
		{
			log.warn( "No TestEvent.Factory capable of storing TestEvent: {}, of type: {} has been registered!",
					testEvent, testEvent.getType() );
		}
	}

	@Override
	public String getLabelForType( Class<? extends TestEvent> type )
	{
		Factory<? extends TestEvent> factory = testEventRegistry.lookupFactory( type );
		return factory == null ? type.getSimpleName() : factory.getLabel();
	}

	@Override
	public void registerObserver( TestEventObserver observer )
	{
		observers.add( observer );
	}

	@Override
	public void unregisterObserver( TestEventObserver observer )
	{
		observers.remove( observer );
	}
}
