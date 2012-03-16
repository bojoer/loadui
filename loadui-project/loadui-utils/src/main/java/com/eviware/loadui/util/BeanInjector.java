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
package com.eviware.loadui.util;

import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public enum BeanInjector
{
	INSTANCE;

	private final LoadingCache<Class<?>, Object> beanCache = CacheBuilder.newBuilder().weakValues()
			.build( new CacheLoader<Class<?>, Object>()
			{
				@Override
				public Object load( Class<?> key ) throws Exception
				{
					return INSTANCE.doGetBean( key );
				}
			} );

	@Nonnull
	public static <T> T getBean( @Nonnull final Class<T> cls )
	{
		try
		{
			return cls.cast( INSTANCE.beanCache.get( cls ) );
		}
		catch( ExecutionException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static void setBundleContext( BundleContext arg0 )
	{
		synchronized( INSTANCE.waiter )
		{
			INSTANCE.context = arg0;
			arg0.addServiceListener( new ServiceListener()
			{
				@Override
				public void serviceChanged( ServiceEvent event )
				{
					String[] objectClasses = ( String[] )event.getServiceReference().getProperty( "objectClass" );
					try
					{
						for( String objectClass : objectClasses )
						{
							Class<?> key = Class.forName( objectClass );
							INSTANCE.beanCache.invalidate( key );
						}
					}
					catch( ClassNotFoundException e )
					{
						// Ignore
					}
				}
			} );
			INSTANCE.clearCache();
			INSTANCE.beanCache.put( BundleContext.class, arg0 );
			INSTANCE.waiter.notifyAll();
		}
	}

	private final Object waiter = new Object();
	private BundleContext context;

	private <T> T doGetBean( @Nonnull Class<T> cls )
	{
		if( context == null )
		{
			synchronized( waiter )
			{
				try
				{
					waiter.wait( 5000 );
				}
				catch( InterruptedException e )
				{
				}
				if( context == null )
					throw new RuntimeException( "BundleContext is missing, has BeanInjector been configured?" );
			}
		}

		ServiceReference ref = context.getServiceReference( cls.getName() );
		if( ref != null )
		{
			Object service = context.getService( ref );
			if( service != null )
				return cls.cast( service );
		}

		throw new IllegalArgumentException( "No Bean found for class: " + cls );
	}

	protected static class ContextSetter implements BundleContextAware
	{
		@Override
		public void setBundleContext( BundleContext arg0 )
		{
			BeanInjector.setBundleContext( arg0 );
		}
	}

	public void clearCache()
	{
		beanCache.invalidateAll();
	}
}
