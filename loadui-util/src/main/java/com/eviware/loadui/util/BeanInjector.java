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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class BeanInjector implements BundleContextAware
{
	private static BeanInjector instance;
	private static Object waiter = new Object();

	public static <T> T getBean( Class<T> cls )
	{
		if( instance == null )
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
				if( instance == null )
					throw new RuntimeException( "BundleContext is missing, has BeanInjector been configured?" );
			}
		}

		return instance.doGetBean( cls );
	}

	private BundleContext context;

	public BeanInjector()
	{
		instance = this;
	}

	private <T> T doGetBean( Class<T> cls )
	{
		// String camelCase = cls.getSimpleName().substring( 0, 1 ).toLowerCase()
		// + cls.getSimpleName().substring( 1 );

		ServiceReference ref = context.getServiceReference( cls.getName() );
		if( ref != null )
		{
			Object service = context.getService( ref );
			if( service != null )
				return cls.cast( service );
		}

		throw new IllegalArgumentException( "No Bean found for class: " + cls );
	}

	@Override
	public void setBundleContext( BundleContext arg0 )
	{
		context = arg0;
		synchronized( waiter )
		{
			waiter.notifyAll();
		}
	}
}
