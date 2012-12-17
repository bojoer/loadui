package com.eviware.loadui.util.remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReferenceWrapperImpl implements ReferenceWrapper
{
	private final List<String> implementedClasses;
	private final List<String> implementedInterfaces;
	private final Object reference;

	public ReferenceWrapperImpl( Object reference, List<String> implementedClasses, List<String> implementedInterfaces )
	{
		this.reference = reference;
		this.implementedClasses = Collections.unmodifiableList( implementedClasses );
		this.implementedInterfaces = Collections.unmodifiableList( implementedInterfaces );
	}

	@Override
	public ReferenceWrapper getSelf()
	{
		return this;
	}

	@Override
	public List<String> getImplementedClasses()
	{
		return implementedClasses;
	}

	@Override
	public List<String> getImplementedInterfaces()
	{
		return implementedInterfaces;
	}

	@Override
	public Object invoke( Class<?> declaringClass, String methodName, Object[] args )
	{
		Object[] unwrappedArgs = unwrap( args );
		Exception last = null;
		for( Method method : declaringClass.getMethods() )
		{
			if( matches( method, methodName, unwrappedArgs ) )
			{
				try
				{
					Object retVal = method.invoke( reference, unwrappedArgs );

					if( retVal == null )
					{
						return null;
					}
					else if( Modifier.isFinal( method.getReturnType().getModifiers() ) )
					{
						return retVal;
					}
					else
					{
						Set<String> classes = new LinkedHashSet<>();
						Set<String> interfaces = new LinkedHashSet<>();

						interfaces.add( method.getReturnType().getName() );
						for( Class<?> cls : retVal.getClass().getInterfaces() )
						{
							interfaces.add( cls.getName() );
						}

						Class<?> cls = retVal.getClass();
						while( cls != null )
						{
							classes.add( cls.getName() );
							cls = cls.getSuperclass();
						}

						return new ReferenceWrapperImpl( retVal, new ArrayList<>( classes ), new ArrayList<>( interfaces ) );
					}
				}
				catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
				{
					last = e;
				}
			}
		}

		return last;
	}

	private static Object[] unwrap( Object[] wrapped )
	{
		Object[] unwrapped = new Object[wrapped.length];
		for( int i = 0; i < wrapped.length; i++ )
		{
			final Object wrappedArg = wrapped[i];
			unwrapped[i] = wrappedArg instanceof ReferenceWrapperImpl ? ( ( ReferenceWrapperImpl )wrappedArg ).reference
					: wrappedArg;
		}

		return unwrapped;
	}

	private static boolean matches( Method method, String methodName, Object[] args )
	{
		if( methodName.equals( method.getName() ) )
		{
			if( method.getParameterTypes().length == args.length )
			{
				for( int i = 0; i < args.length; i++ )
				{
					if( !method.getParameterTypes()[i].isPrimitive() && !method.getParameterTypes()[i].isInstance( args[i] ) )
					{
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}
}
