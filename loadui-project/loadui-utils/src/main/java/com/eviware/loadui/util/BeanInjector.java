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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public enum BeanInjector
{
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger( BeanInjector.class );

	private final LoadingCache<Class<?>, Object> beanCache = CacheBuilder.newBuilder().weakValues().build(
			new CacheLoader<Class<?>, Object>()
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
			return getBeanFuture( cls ).get( 8, TimeUnit.SECONDS );
		}
		catch( ExecutionException | InterruptedException | TimeoutException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Nonnull
	public static <T> ListenableFuture<T> getBeanFuture( @Nonnull final Class<T> cls )
	{
		Object cachedObject = INSTANCE.beanCache.getIfPresent( cls );
		if( cachedObject != null )
		{
			return Futures.immediateFuture( cls.cast( cachedObject ) );
		}
		else
		{
			return new BeanFuture<>( cls );
		}
	}

	public static void setBundleContext( BundleContext context )
	{
		INSTANCE.context = context;
		context.addServiceListener( new ServiceListener()
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
		INSTANCE.beanCache.put( BundleContext.class, context );
		log.info( "BundleContext set successfully" );
		INSTANCE.waiterLatch.countDown();
	}

	private final CountDownLatch waiterLatch = new CountDownLatch( 1 );

	private BundleContext context;

	private <T> T doGetBean( @Nonnull Class<T> cls )
	{
		try
		{
			if( !waiterLatch.await( 5, TimeUnit.SECONDS ) )
			{
				throw new RuntimeException( "BundleContext is missing, has BeanInjector been configured?" );
			}
		}
		catch( InterruptedException e )
		{
			Thread.currentThread().interrupt();
		}

		//TODO: We can't use generics here until the OSGi jars stop using compilation flags that are not compatible with Java7.
		ServiceReference/* <T> */ref = context.getServiceReference( cls.getName() );
		if( ref != null )
		{
			T service = ( T )context.getService( ref );
			if( service != null )
			{
				return service;
			}
			else
			{
				log.warn( "Found serviceReference but service itself was null" );
			}
		}
		else
		{
			log.warn( "Could not find serviceReference for " + cls.getName() );
		}

		throw new IllegalArgumentException( "No Bean found for class: " + cls );
	}

	protected static class ContextSetter implements BundleContextAware
	{
		@Override
		public void setBundleContext( BundleContext arg0 )
		{
			log.info( "Setting BundleContext for BeanInjector" );
			BeanInjector.setBundleContext( arg0 );
		}
	}

	public void clearCache()
	{
		beanCache.invalidateAll();
	}

	private static class BeanFuture<T> extends AbstractFuture<T>
	{
		private final ServiceListener serviceListener = new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent event )
			{
				if( event.getType() == ServiceEvent.REGISTERED )
				{
					log.info( "Service has been registered: " + event.getServiceReference().getBundle().getLocation() );
					//TODO: We can't use generics here until the OSGi jars stop using compilation flags that are not compatible with Java7.
					ServiceReference/* <T> */serviceReference = event.getServiceReference();
					set( ( T )INSTANCE.context.getService( serviceReference ) );
				}
			}
		};

		private BeanFuture( Class<T> serviceType )
		{
			try
			{
				if( !INSTANCE.waiterLatch.await( 30, TimeUnit.SECONDS ) )
				{
					throw new RuntimeException( "BundleContext is missing, has BeanInjector been configured?" );
				}

				try
				{
					INSTANCE.context.addServiceListener( serviceListener, "(objectclass=" + serviceType.getName() + ")" );
				}
				catch( InvalidSyntaxException e )
				{
					e.printStackTrace();
				}

				Object cachedObject = INSTANCE.beanCache.get( serviceType );
				if( cachedObject != null )
				{
					set( serviceType.cast( cachedObject ) );
				}
				else
				{
					String msg = "Service of type " + serviceType.getName() + " has been dropped";
					log.warn( msg );
					throw new RuntimeException( msg );
				}
			}
			catch( InterruptedException e )
			{
				Thread.currentThread().interrupt();
			}
			catch( Exception e )
			{
				setException( e );
			}
		}

		@Override
		protected boolean set( T value )
		{
			INSTANCE.context.removeServiceListener( serviceListener );
			return super.set( value );
		}

		@Override
		public boolean cancel( boolean mayInterruptIfRunning )
		{
			INSTANCE.context.removeServiceListener( serviceListener );

			return super.cancel( mayInterruptIfRunning );
		}
	}
}
