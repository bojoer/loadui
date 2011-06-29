/*
 * Copyright 2011 eviware software ab
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

import javax.annotation.Nonnull;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public enum BeanInjector
{
	INSTANCE;

	@Nonnull
	public static <T> T getBean( @Nonnull Class<T> cls )
	{
		if( INSTANCE.context == null )
		{
			synchronized( INSTANCE.waiter )
			{
				try
				{
					INSTANCE.waiter.wait( 5000 );
				}
				catch( InterruptedException e )
				{
				}
				if( INSTANCE.context == null )
					throw new RuntimeException( "BundleContext is missing, has BeanInjector been configured?" );
			}
		}

		return INSTANCE.doGetBean( cls );
	}

	public static void setBundleContext( BundleContext arg0 )
	{
		synchronized( INSTANCE.waiter )
		{
			INSTANCE.context = arg0;
			INSTANCE.waiter.notifyAll();
		}
	}

	private final Object waiter = new Object();
	private BundleContext context;

	private <T> T doGetBean( @Nonnull Class<T> cls )
	{
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
}
