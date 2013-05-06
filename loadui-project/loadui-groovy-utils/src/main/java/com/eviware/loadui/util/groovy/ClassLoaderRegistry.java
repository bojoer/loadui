/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.groovy;

import com.eviware.loadui.api.traits.Releasable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutionException;

/**
 * Manages GroovyComponentClassLoaders by id. Creates ClassLoaders on demand,
 * and caches them, but will only use weak references.
 *
 * @author dain.nilsson
 */
public class ClassLoaderRegistry implements Releasable
{
	public static final Logger log = LoggerFactory.getLogger( ClassLoaderRegistry.class );
	private final ClassLoader bundleClassLoader = GroovyShell.class.getClassLoader();

	private final LoadingCache<String, GroovyEnvironmentClassLoader> classLoaders = CacheBuilder.newBuilder()
			.weakValues().build( new CacheLoader<String, GroovyEnvironmentClassLoader>()
			{
				@Override
				public GroovyEnvironmentClassLoader load( String key ) throws Exception
				{
					return AccessController.doPrivileged( new PrivilegedAction<GroovyEnvironmentClassLoader>()
					{
						@Override
						public GroovyEnvironmentClassLoader run()
						{
							return provideClassLoader( bundleClassLoader );
						}
					} );
				}
			} );

	public synchronized GroovyEnvironmentClassLoader useClassLoader( String id )
	{
		try
		{
			return classLoaders.get( id );
		}
		catch( ExecutionException e )
		{
			log.error( "Unable to get GroovyEnvironmentClassLoader", e );
		}

		return null;
	}

	@Override
	public synchronized void release()
	{
		classLoaders.invalidateAll();
	}

	/**
	 * Sub-classes can provide a different class loader by overriding this method
	 * @param bundleClassLoader
	 * @return
	 */
	protected GroovyEnvironmentClassLoader provideClassLoader( ClassLoader bundleClassLoader )
	{
		return new GroovyEnvironmentClassLoader( bundleClassLoader );
	}
}
