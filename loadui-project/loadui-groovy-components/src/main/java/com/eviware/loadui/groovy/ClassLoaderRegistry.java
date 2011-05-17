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
package com.eviware.loadui.groovy;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.Releasable;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

import groovy.lang.GroovyShell;

/**
 * Manages GroovyComponentClassLoaders by id. Creates ClassLoaders on demand,
 * and caches them, but will only use weak references.
 * 
 * @author dain.nilsson
 */
public class ClassLoaderRegistry implements Releasable
{
	public static final Logger log = LoggerFactory.getLogger( ClassLoaderRegistry.class );

	private final ConcurrentMap<String, GroovyComponentClassLoader> classLoaders = new MapMaker().weakValues()
			.evictionListener( new MapEvictionListener<String, GroovyComponentClassLoader>()
			{
				@Override
				public void onEviction( String key, GroovyComponentClassLoader value )
				{
					log.debug( "GroovyClassLoader evicted: {}", key );
				}
			} ).makeMap();

	public synchronized GroovyComponentClassLoader useClassLoader( String id, Object user )
	{
		GroovyComponentClassLoader classLoader = classLoaders.get( id );
		if( classLoader == null )
		{
			classLoader = new GroovyComponentClassLoader( GroovyShell.class.getClassLoader() );
			classLoaders.put( id, classLoader );
			log.debug( "GroovyClassLoader created: {}", id );
		}

		return classLoader;
	}

	@Override
	public synchronized void release()
	{
		classLoaders.clear();
	}
}
