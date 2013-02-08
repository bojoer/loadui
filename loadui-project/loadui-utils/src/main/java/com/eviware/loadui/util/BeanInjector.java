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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BeanInjector
{
	private static final Logger log = LoggerFactory.getLogger( BeanInjector.class );
	private static final Object LOCK = new Object();

	private static BundleContext context;
	private static Map<String, ServiceReference> serviceRefs = Maps.newHashMap();
	private static Map<String, List<ServiceRefFutureTask>> beanRequests = Maps.newHashMap();

	protected static ServiceListener serviceListener = new LoaduiServiceListener();
	protected static long defaultTimeout = 8000L;

	@Nonnull
	public static <T> T getBean( @Nonnull final Class<T> cls )
	{
		return getBean( cls, defaultTimeout );
	}

	@Nonnull
	public static <T> T getBean( @Nonnull final Class<T> cls, long timeout )
	{
		log.info( "Making request for Bean: " + cls.getName() );
		ServiceRefFutureTask future = null;
		synchronized( LOCK )
		{
			ServiceReference serviceRef = serviceRefs.get( cls.getName() );
			if( serviceRef != null )
			{
				return ( T )context.getService( serviceRef );
			}
			else
			{
				future = waitFor( cls );
			}
		}
		// program flow will only get here if service does not exist or during start-up, while the service
		// has not been published yet
		try
		{
			ServiceReference serviceRef = future.get( timeout, TimeUnit.MILLISECONDS );
			return ( T )context.getService( serviceRef );
		}
		catch( InterruptedException | ExecutionException | TimeoutException e )
		{
			log.warn( "Could not find a Service of class " + cls, e );
			throw new RuntimeException( "Could not find a Service of class " + cls, e );
		}

	}

	public static void reset()
	{
		serviceRefs = Maps.newHashMap();
		beanRequests = Maps.newHashMap();
	}

	private static ServiceRefFutureTask waitFor( final Class<?> cls )
	{
		ServiceRefFutureTask future = new ServiceRefFutureTask( new ServiceAvailableCallable() );
		List<ServiceRefFutureTask> tasks = beanRequests.get( cls.getName() );
		if( tasks == null )
		{
			tasks = Lists.newArrayList();
			beanRequests.put( cls.getName(), tasks );
		}
		tasks.add( future );
		return future;
	}

	public static void setBundleContext( BundleContext context )
	{
		BeanInjector.context = context;
		context.addServiceListener( serviceListener );
		log.info( "BundleContext set successfully" );

	}

	private static String[] register( ServiceReference serviceReference )
	{
		try
		{
			synchronized( LOCK )
			{
				// DO NOT KEEP A REFERENCE TO THE SERVICE ITSELF AS IT IS A DYNAMIC SERVICE WHICH MAY BE UNREGISTERED
				log.info( "ObjectClass: " + Arrays.toString( ( String[] )serviceReference.getProperty( "objectClass" ) ) );
				String[] interfaceNames = ( String[] )serviceReference.getProperty( "objectClass" );
				if( interfaceNames != null )
				{
					for( String interfaceName : interfaceNames )
					{
						log.info( "Registering service for " + interfaceName );
						serviceRefs.put( interfaceName, serviceReference );
					}
				}
				return interfaceNames;
			}
		}
		catch( Exception e )
		{
			log.warn( "Problem registering service", e );
		}
		return null;
	}

	private static void unregister( ServiceReference serviceReference )
	{
		try
		{
			synchronized( LOCK )
			{
				String[] interfaceNames = ( String[] )serviceReference.getProperty( "objectClass" );
				if( interfaceNames != null )
				{
					for( String interfaceName : interfaceNames )
					{
						ServiceReference ref = serviceRefs.get( interfaceName );
						if( ref == serviceReference )
						{
							log.info( "Unregistering service for " + interfaceName );
							serviceRefs.remove( interfaceName );
						}
					}
				}
			}
		}
		catch( Exception e )
		{
			log.warn( "Problem unregistering service", e );
		}

	}

	private static void notifyWaitingRequests( String interfaceName, ServiceReference serviceReference )
	{
		for( ServiceRefFutureTask task : popRequestsFor( interfaceName ) )
		{
			task.callable.serviceRef = serviceReference;
			task.run();
		}
	}

	private static List<ServiceRefFutureTask> popRequestsFor( String interfaceName )
	{
		synchronized( LOCK )
		{
			List<ServiceRefFutureTask> result = beanRequests.remove( interfaceName );
			if( result == null )
				return Collections.emptyList();
			else
				return result;
		}
	}

	private static class ServiceAvailableCallable implements Callable<ServiceReference>
	{

		protected ServiceReference serviceRef; // will be set when the service becomes available

		@Override
		public ServiceReference call() throws Exception
		{
			return serviceRef;
		}

	}

	private static class ServiceRefFutureTask extends FutureTask<ServiceReference>
	{

		private final ServiceAvailableCallable callable;

		public ServiceRefFutureTask( ServiceAvailableCallable callable )
		{
			super( callable );
			this.callable = callable;
		}

	}

	protected static class LoaduiServiceListener implements ServiceListener
	{

		@Override
		public void serviceChanged( ServiceEvent event )
		{
			switch( event.getType() )
			{
			case ServiceEvent.UNREGISTERING :
				unregister( event.getServiceReference() );
				break;
			default :
				String[] interfaces = register( event.getServiceReference() );
				for( String interf : interfaces )
				{
					notifyWaitingRequests( interf, event.getServiceReference() );
				}
			}
		}

	}

}
