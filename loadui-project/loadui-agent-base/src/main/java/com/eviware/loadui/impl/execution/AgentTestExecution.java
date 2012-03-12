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
package com.eviware.loadui.impl.execution;

import java.util.concurrent.Future;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.AbstractTestExecution;
import com.google.common.base.Objects;

public class AgentTestExecution extends AbstractTestExecution implements Releasable
{
	private static CanvasItem lookupCanvas( String canvasId )
	{
		return ( CanvasItem )BeanInjector.getBean( AddressableRegistry.class ).lookup( canvasId );
	}

	private final AgentTestRunner runner;
	private final ReleaseListener releaseListener = new ReleaseListener();

	private Phase phase = Phase.PRE_START;

	public AgentTestExecution( AgentTestRunner runner, String canvasId )
	{
		super( lookupCanvas( canvasId ) );

		this.runner = runner;
		getCanvas().addEventListener( BaseEvent.class, releaseListener );
	}

	@Override
	public Future<ExecutionResult> complete()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void release()
	{
		getCanvas().removeEventListener( BaseEvent.class, releaseListener );
	}

	void setPhase( Phase phase )
	{
		this.phase = phase;
	}

	private class ReleaseListener implements WeakEventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( Objects.equal( event.getKey(), RELEASED ) )
			{
				runner.complete( AgentTestExecution.this, phase );
			}
		}
	}
}
