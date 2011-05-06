package com.eviware.loadui.groovy;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import groovy.grape.Grape;
import groovy.lang.GroovyClassLoader;

public class GroovyComponentClassLoader extends GroovyClassLoader
{
	public static final Logger log = LoggerFactory.getLogger( GroovyComponentClassLoader.class );

	private final HashSet<Object> loadedDeps = Sets.newHashSet();

	public GroovyComponentClassLoader( ClassLoader classLoader )
	{
		super( classLoader );
	}

	public synchronized void loadDependency( String group, String module, String version )
	{
		String dependency = Joiner.on( ':' ).join( group, module );
		if( loadedDeps.add( dependency ) )
		{
			log.info( "Loading dependency: {}", dependency );

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
