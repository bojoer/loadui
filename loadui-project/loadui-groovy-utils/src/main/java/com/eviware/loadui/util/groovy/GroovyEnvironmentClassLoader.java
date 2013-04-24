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

import groovy.grape.Grape;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.BeanInjector;
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

	private final Set<Object> loadedDeps = Sets.newHashSet();

	public GroovyEnvironmentClassLoader( ClassLoader classLoader )
	{
		super( classLoader );
		File groovyHome = new File( System.getProperty( LoadUI.LOADUI_HOME ), ".groovy" );
		System.setProperty( "grape.root", groovyHome.getAbsolutePath() );
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
					throw new Exception( "Groovy Grapes disabled!" );

				Grape.grab( args );
			}
			catch( Exception e )
			{
				log.error( "Error loading dependency: " + dependency + " using Grape, fallback to manual JAR loading.", e );
				boolean dependencyFound = loadJarFile( group, module, version );
				if( !dependencyFound )
				{
					impossibleToLoad( dependency );
				}
			}
		}
	}

	private void impossibleToLoad( String dependency )
	{
		try
		{
			BeanInjector
					.getBeanFuture( TestEventManager.class )
					.get( 500, TimeUnit.MILLISECONDS )
					.logMessage(
							MessageLevel.WARNING,
							"It was not possible to find the following Groovy component's dependency: " + dependency
									+ ".\nThe component will not work. Please check the documentation at\n"
									+ "http://loadui.org/Developers-Corner/custom-component-reference-new.html" );
		}
		catch( InterruptedException | ExecutionException | TimeoutException e1 )
		{
			log.warn( "Could not load dependency " + dependency, e1 );
		}
	}

	protected boolean loadJarFile( String group, String module, String version )
	{

		Path depFile = FileSystems.getDefault().getPath( System.getProperty( "grape.root" ), "grapes", group, module,
				"jars", module + "-" + version + ".jar" );

		if( depFile.toFile().exists() )
		{
			try
			{
				addURL( depFile.toUri().toURL() );
				return true;
			}
			catch( MalformedURLException e2 )
			{
				log.error( "Failed manual JAR loading. Dependency loading failed.", e2 );
				return false;
			}
		}
		else
		{
			log.error( "File: {} doesn't exist, dependency loading failed.", depFile );
			return false;
		}
	}

}
