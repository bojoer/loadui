package com.eviware.loadui.impl.execution;

import java.util.concurrent.Future;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.AbstractTestExecution;

public class AgentTestExecution extends AbstractTestExecution
{
	private static CanvasItem lookupCanvas( String canvasId )
	{
		return ( CanvasItem )BeanInjector.getBean( AddressableRegistry.class ).lookup( canvasId );
	}

	public AgentTestExecution( String canvasId )
	{
		super( lookupCanvas( canvasId ) );
	}

	@Override
	public Future<ExecutionResult> complete()
	{
		throw new UnsupportedOperationException();
	}
}
