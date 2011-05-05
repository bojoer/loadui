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

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.Releasable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

/**
 * Manages GroovyClassLoaders by id. Creates and disposes of ClassLoaders on
 * demand.
 * 
 * @author dain.nilsson
 */
public class ClassLoaderRegistry implements Releasable
{
	public static final Logger log = LoggerFactory.getLogger( ClassLoaderRegistry.class );

	private final HashMap<String, ClassLoaderEntry> classLoaders = Maps.newHashMap();

	public synchronized GroovyClassLoader useClassLoader( String id, Object user )
	{
		if( !classLoaders.containsKey( id ) )
		{
			classLoaders.put( id, new ClassLoaderEntry() );
			log.debug( "Created Classloader: {}", id );
		}

		ClassLoaderEntry entry = classLoaders.get( id );
		entry.users.add( user );

		return entry.classLoader;
	}

	public synchronized void releaseClassLoader( String id, Object user )
	{
		ClassLoaderEntry entry = classLoaders.get( id );
		if( entry != null )
		{
			entry.users.remove( user );
			if( entry.users.isEmpty() )
			{
				entry.classLoader.clearCache();
				classLoaders.remove( id );
				log.debug( "Removed Classloader: {}", id );
			}
		}
	}

	@Override
	public synchronized void release()
	{
		for( ClassLoaderEntry entry : classLoaders.values() )
			entry.classLoader.clearCache();
		classLoaders.clear();
	}

	private static class ClassLoaderEntry
	{
		private final GroovyClassLoader classLoader;
		private final Set<Object> users = Sets.newHashSet();

		private ClassLoaderEntry()
		{
			this.classLoader = new GroovyClassLoader( GroovyShell.class.getClassLoader() );
		}
	}
}
