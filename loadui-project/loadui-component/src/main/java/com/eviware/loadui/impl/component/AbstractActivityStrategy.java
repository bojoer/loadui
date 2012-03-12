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
package com.eviware.loadui.impl.component;

import java.util.EventObject;

import com.eviware.loadui.api.component.ActivityStrategy;
import com.eviware.loadui.api.events.ActivityEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.util.events.EventSupport;

public abstract class AbstractActivityStrategy implements ActivityStrategy
{
	private final EventSupport eventSupport = new EventSupport();
	private boolean active = false;

	protected AbstractActivityStrategy( boolean active )
	{
		this.active = active;
	}

	protected final void setActive( boolean active )
	{
		if( this.active != active )
		{
			this.active = active;
			fireEvent( new ActivityEvent( this ) );
		}
	}

	@Override
	public boolean isActive()
	{
		return active;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}
}
