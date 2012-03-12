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
package com.eviware.loadui.impl.testevents;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.testevents.AbstractTestEventManager;

public class TestEventManagerImpl extends AbstractTestEventManager
{
	private static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	private static final String CHANNEL = "/" + TestEventManager.class.getSimpleName();

	public TestEventManagerImpl( TestEventRegistry testEventRegistry )
	{
		super( testEventRegistry );
	}

	@Override
	public <T extends TestEvent> void logTestEvent( TestEvent.Source<T> source, T testEvent )
	{
		if( source instanceof Addressable )
		{
			@SuppressWarnings( "unchecked" )
			TestEvent.Factory<T> factory = testEventRegistry.lookupFactory( ( Class<T> )testEvent.getType() );

			if( factory != null )
			{
				sendEvent( source, Arrays.asList( factory.getLabel(), ( ( Addressable )source ).getId(),
						testEvent.getTimestamp(), factory.getDataForTestEvent( testEvent ) ) );
			}
			else
			{
				log.warn( "No TestEvent.Factory capable of storing TestEvent: {}, of type: {} has been registered!",
						testEvent, testEvent.getType() );
			}
		}
	}

	private void sendEvent( TestEvent.Source<?> source, Object data )
	{
		Object target = source;

		if( target instanceof ComponentItem )
		{
			target = ( ( ComponentItem )target ).getCanvas();
		}

		if( target instanceof SceneItem )
		{
			( ( SceneItem )target ).broadcastMessage( CHANNEL, data );
		}
		else
		{
			BeanInjector.getBean( BroadcastMessageEndpoint.class ).sendMessage( CHANNEL, data );
		}
	}

	@Override
	public void registerObserver( TestEventObserver observer )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void unregisterObserver( TestEventObserver observer )
	{
		throw new UnsupportedOperationException();
	}

}
