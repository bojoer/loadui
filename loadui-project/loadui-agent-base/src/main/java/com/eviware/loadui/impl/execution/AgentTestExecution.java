package com.eviware.loadui.impl.execution;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Future;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.AbstractTestExecution;
import com.google.common.base.Objects;

public class AgentTestExecution extends AbstractTestExecution
{
	public static CanvasItem ALL_TEST_CASES = ( CanvasItem )Proxy.newProxyInstance(
			AgentTestExecution.class.getClassLoader(), new Class[] { CanvasItem.class }, new InvocationHandler()
			{
				@Override
				public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
				{
					// TODO Auto-generated method stub
					return null;
				}
			} );

	private static CanvasItem lookupCanvas( String canvasId )
	{
		CanvasItem canvas = ( CanvasItem )BeanInjector.getBean( AddressableRegistry.class ).lookup( canvasId );
		return Objects.firstNonNull( canvas, ALL_TEST_CASES );
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
