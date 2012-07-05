package com.eviware.loadui.util.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ReferenceProxy
{
	@SuppressWarnings( "rawtypes" )
	public static Object createProxy( final ReferenceWrapper wrapper )
	{
		List<Class<?>> interfaces = classList( wrapper.getImplementedInterfaces() );
		List<Class<?>> classes = classList( wrapper.getImplementedClasses() );

		for( Class<?> cls : classes )
		{
			if( !Modifier.isFinal( cls.getModifiers() ) && implementsAll( cls, interfaces ) )
			{
				return Mockito.mock( cls, new Answer()
				{
					@Override
					public Object answer( InvocationOnMock invocation ) throws Throwable
					{
						final Method method = invocation.getMethod();
						Object result = wrapper.invoke( method.getDeclaringClass(), method.getName(),
								invocation.getArguments() );
						if( result instanceof ReferenceWrapper )
						{
							return createProxy( ( ReferenceWrapper )result );
						}

						return result;
					}
				} );
			}
		}

		return Proxy.newProxyInstance( ReferenceProxy.class.getClassLoader(),
				interfaces.toArray( new Class[interfaces.size()] ), new InvocationHandler()
				{
					@Override
					public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
					{
						Object result = wrapper.invoke( method.getDeclaringClass(), method.getName(),
								args == null ? new Object[0] : args );
						if( result instanceof ReferenceWrapper )
						{
							return createProxy( ( ReferenceWrapper )result );
						}

						return result;
					}
				} );
	}

	private static boolean implementsAll( Class<?> cls, List<Class<?>> interfaces )
	{
		Set<Class<?>> implemented = new HashSet<>();

		implemented.addAll( interfaces );
		implemented.removeAll( Arrays.asList( cls.getInterfaces() ) );

		return implemented.isEmpty();
	}

	private static List<Class<?>> classList( List<String> classNames )
	{
		List<Class<?>> interfaces = new ArrayList<>();
		for( String className : classNames )
		{
			try
			{
				Class<?> cls = Class.forName( className );
				if( cls.isInterface() )
				{
					interfaces.add( cls );
				}
			}
			catch( ClassNotFoundException e )
			{
				//Ignore
			}
		}

		return interfaces;
	}
}
