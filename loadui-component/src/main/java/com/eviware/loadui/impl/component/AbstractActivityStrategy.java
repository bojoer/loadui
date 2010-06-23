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
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
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
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}
}
