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
package com.eviware.loadui.util.testevents;

import java.util.Collections;
import java.util.Set;

import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Factory;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.google.common.collect.MapMaker;

public abstract class AbstractTestEventManager implements TestEventManager
{
	protected final TestEventRegistry testEventRegistry;
	protected final Set<TestEventObserver> observers = Collections.newSetFromMap( new MapMaker().weakKeys()
			.<TestEventObserver, Boolean> makeMap() );

	public AbstractTestEventManager( TestEventRegistry testEventRegistry )
	{
		this.testEventRegistry = testEventRegistry;
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

	@Override
	public void logMessage( MessageLevel level, String message )
	{
		MessageTestEvent messageEvent = new MessageTestEvent( System.currentTimeMillis(), level, message );
		logTestEvent( MessageSeveritySource.getSource( level ), messageEvent );
	}

	@Override
	public void logMessage( MessageLevel level, String message, long timestamp )
	{
		MessageTestEvent messageEvent = new MessageTestEvent( timestamp, level, message );
		logTestEvent( MessageSeveritySource.getSource( level ), messageEvent );
	}
}
