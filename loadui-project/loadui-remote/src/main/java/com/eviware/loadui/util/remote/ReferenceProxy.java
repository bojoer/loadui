package com.eviware.loadui.util.remote;

import gnu.cajo.invoke.Remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class ReferenceProxy
{
	public static Object getItem( String url )
	{
		ReferenceWrapper wrapper;
		try
		{
			Object target = Remote.getItem( url );
			wrapper = ( ReferenceWrapper )Remote.invoke( target, "getSelf", null );
		}
		catch( RuntimeException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

		return createProxy( wrapper );
	}

	public static Object createProxy( final ReferenceWrapper wrapper )
	{
		List<Class<?>> interfaces = classList( wrapper.getImplementedInterfaces() );
		List<Class<?>> classes = classList( wrapper.getImplementedClasses() );

		for( Class<?> cls : classes )
		{
			if( !Modifier.isFinal( cls.getModifiers() ) && implementsAll( cls, interfaces ) )
			{
				ProxyFactory factory = new ProxyFactory();
				factory.setSuperclass( cls );
				factory.setInterfaces( interfaces.toArray( new Class[interfaces.size()] ) );

				MethodHandler handler = new MethodHandler()
				{
					@Override
					public Object invoke( Object self, Method thisMethod, Method proceed, Object[] args ) throws Throwable
					{
						Object result = wrapper.invoke( thisMethod.getDeclaringClass(), thisMethod.getName(), args );
						if( result instanceof ReferenceWrapper )
						{
							return createProxy( ( ReferenceWrapper )result );
						}

						return result;
					}
				};

				try
				{
					return factory.create( new Class[0], new Object[0], handler );
				}
				catch( ReflectiveOperationException e )
				{
					e.printStackTrace();
				}
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
