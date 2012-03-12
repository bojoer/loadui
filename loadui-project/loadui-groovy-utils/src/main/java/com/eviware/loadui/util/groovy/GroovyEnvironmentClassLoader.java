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

import groovy.grape.Grape;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A GroovyClassLoader which can load dependencies dynamically from a Maven
 * repository using Grape.
 * 
 * @author dain.nilsson
 */
public class GroovyEnvironmentClassLoader extends GroovyClassLoader
{
	public static final Logger log = LoggerFactory.getLogger( GroovyEnvironmentClassLoader.class );

	private final HashSet<Object> loadedDeps = Sets.newHashSet();

	public GroovyEnvironmentClassLoader( ClassLoader classLoader )
	{
		super( classLoader );
	}

	/**
	 * Loads the given dependency, unless it is already loaded. If Grape should
	 * fail, an attempt is made to manually load the JAR file from the file
	 * system. This does not take into consideration any transitive dependencies.
	 * 
	 * @param group
	 * @param module
	 * @param version
	 */
	public synchronized void loadDependency( String group, String module, String version )
	{
		String dependency = Joiner.on( ':' ).join( group, module );
		if( loadedDeps.add( dependency ) )
		{
			log.debug( "Loading dependency: {}", dependency );

			HashMap<String, Object> args = Maps.newHashMap();
			args.put( "group", group );
			args.put( "module", module );
			args.put( "version", version );
			args.put( "classLoader", this );
			try
			{
				if( Boolean.getBoolean( "loadui.grape.disable" ) )
					throw new Exception( "Groovy loading disabled!" );

				Grape.grab( args );
			}
			catch( Exception e )
			{
				log.error( "Error loading dependency: " + dependency + " using Grape, fallback to manual JAR loading.", e );

				File depFile = new File( System.getProperty( "groovy.root" ), "grapes" + File.separator + group
						+ File.separator + module + File.separator + "jars" + File.separator + module + "-" + version
						+ ".jar" );
				if( depFile.exists() )
				{
					try
					{
						addURL( depFile.toURI().toURL() );
					}
					catch( MalformedURLException e2 )
					{
						log.error( "Failed manual JAR loading. Dependency loading failed.", e2 );
					}
				}
				else
				{
					log.error( "File: {} doesn't exist, dependency loading failed.", depFile );
				}
			}
		}
	}
}
