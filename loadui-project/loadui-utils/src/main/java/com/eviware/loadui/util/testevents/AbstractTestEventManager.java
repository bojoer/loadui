package com.eviware.loadui.util.testevents;

import java.util.Collections;
import java.util.Set;

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

}
