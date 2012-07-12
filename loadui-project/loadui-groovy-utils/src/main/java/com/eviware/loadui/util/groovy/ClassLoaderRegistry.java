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
package com.eviware.loadui.util.groovy;

import groovy.lang.GroovyShell;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutionException;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;

import com.eviware.loadui.api.traits.Releasable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Manages GroovyComponentClassLoaders by id. Creates ClassLoaders on demand,
 * and caches them, but will only use weak references.
 * 
 * @author dain.nilsson
 */
public class ClassLoaderRegistry implements Releasable, BundleContextAware
{
	public static final Logger log = LoggerFactory.getLogger( ClassLoaderRegistry.class );
	private ClassLoader bundleClassLoader = GroovyShell.class.getClassLoader();

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
							return new GroovyEnvironmentClassLoader( bundleClassLoader );
						}
					} );
				}
			} );

	@Override
	public void setBundleContext( BundleContext bundleContext )
	{
		bundleClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Class<?> cls = bundleContext.getBundle().loadClass( "org.codehaus.groovy.runtime.GeneratedClosure" );
			log.debug( "Loaded class from bundle: {}", cls );
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			Class<?> cls = bundleClassLoader.loadClass( "org.codehaus.groovy.runtime.GeneratedClosure" );
			log.debug( "Loaded class from classloader: {}", cls );
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			Class<?> cls = Class.forName( "org.codehaus.groovy.runtime.GeneratedClosure" );
			log.debug( "Loaded class from class.forName: {}", cls );
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug( "Parent classloader: {}", bundleClassLoader.getParent() );
	}

	public synchronized GroovyEnvironmentClassLoader useClassLoader( String id, Object user )
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

}
