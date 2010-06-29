/*
 * Copyright 2010 eviware software ab
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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanInjector implements ApplicationContextAware
{
	private static ApplicationContext context;
	private static Object waiter = new Object();

	public static <T> T getBean( Class<T> cls )
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
					throw new RuntimeException( "ApplicationContext is missing, has BeanInjector been configured?" );
			}
		}

		String camelCase = cls.getSimpleName().substring( 0, 1 ).toLowerCase() + cls.getSimpleName().substring( 1 );
		Object obj = context.getBean( camelCase, cls );
		if( cls.isInstance( obj ) )
			return cls.cast( obj );
		throw new IllegalArgumentException( "No Bean found for class: " + cls );
	}

	@Override
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		context = arg0;
		synchronized( waiter )
		{
			waiter.notifyAll();
		}
	}
}
