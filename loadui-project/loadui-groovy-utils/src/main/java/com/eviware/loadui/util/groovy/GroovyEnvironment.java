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
package com.eviware.loadui.util.groovy;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.grape.Grape;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import com.eviware.loadui.api.traits.Releasable;
import com.google.common.collect.Maps;

/**
 * A runtime environment for a ParsedGroovyScript. The script is compiled and
 * run using the given parameters, such as classloader, GroovyResolver, etc. The
 * script can be interacted with by calling invokeClosure, which attempts to run
 * a Closure within the scripts binding.
 * 
 * @author dain.nilsson
 */
public class GroovyEnvironment implements Releasable
{
	private final ParsedGroovyScript script;
	private final String scriptName;
	private final Logger log;
	private final GroovyEnvironmentClassLoader classLoader;
	private final GroovyShell shell;
	private final GroovyResolver.Methods methodResolver;
	private final GroovyResolver.Properties propertyResolver;
	private final Binding binding = new Binding();

	public GroovyEnvironment( @Nonnull ParsedGroovyScript script, @Nonnull String id, @Nonnull String basePackage,
			@Nonnull ClassLoaderRegistry classLoaderRegistry, @Nonnull String classLoaderId, GroovyResolver resolver )
	{
		this.script = script;
		scriptName = "Groovy" + id.replaceAll( "[^a-zA-Z]", "" );
		log = LoggerFactory.getLogger( basePackage + "." + id );

		classLoader = classLoaderRegistry.useClassLoader( classLoaderId, this );
		shell = new GroovyShell( classLoader );

		methodResolver = resolver instanceof GroovyResolver.Methods ? ( GroovyResolver.Methods )resolver
				: GroovyResolver.NULL_RESOLVER;
		propertyResolver = resolver instanceof GroovyResolver.Properties ? ( GroovyResolver.Properties )resolver
				: GroovyResolver.NULL_RESOLVER;
	}

	/**
	 * Initializes the script, loading any required dependencies and running the
	 * script.
	 */
	public void init()
	{
		int repos = 0;
		for( String repo : script.getHeaders( "m2repo" ) )
		{
			Map<String, Object> args = Maps.newHashMap();
			args.put( "name", "repo_" + repos++ );
			args.put( "root", repo );
			args.put( "m2compatible", true );
			Grape.addResolver( args );
		}

		for( String dependency : script.getHeaders( "dependency" ) )
		{
			String[] parts = dependency.split( ":" );
			if( parts.length >= 3 )
				classLoader.loadDependency( parts[0], parts[1], parts[2] );
		}

		try
		{
			Script groovyScript = shell.parse( script.getBody(), scriptName );
			binding.setProperty( "log", log );
			groovyScript.setMetaClass( new ScriptMetaClass( groovyScript.getMetaClass() ) );

			groovyScript.setBinding( binding );
			groovyScript.run();
		}
		catch( Exception e )
		{
			log.error( "Compilation of Groovy script failed: ", e );
		}
	}

	public Binding getBinding()
	{
		return binding;
	}

	/**
	 * Returns a Logger object which is specific to this script.
	 * 
	 * @return
	 */
	public Logger getLog()
	{
		return log;
	}

	/**
	 * Attempts to call a Closure in the scripts binding with the given
	 * arguments.
	 * 
	 * @param ignoreMissing
	 *           If set to true, null will be returned if the Closure does not
	 *           exist. Otherwise an UpsupportedOperationException will be
	 *           thrown.
	 * @param returnException
	 *           If set to true, any Exception thrown by the invoked Closure will
	 *           be returned as the result. Otherwise the exception will be
	 *           logged and null will be returned.
	 * @param name
	 *           The name of the Closure to call.
	 * @param args
	 *           The arguments to pass to the Closure being called.
	 * @return The result of the Closure, if successful.
	 */
	@Nullable
	public Object invokeClosure( boolean ignoreMissing, boolean returnException, @Nonnull String name, Object... args )
	{
		Object property = null;
		try
		{
			property = binding.getProperty( name );
		}
		catch( MissingPropertyException e )
		{
		}

		try
		{
			if( property instanceof Closure )
				return ( ( Closure<?> )property ).call( args );
		}
		catch( Throwable e )
		{
			if( returnException )
				return e;

			log.error( "Exception in closure " + name + " of " + scriptName + ":", e );
			return null;
		}

		if( !ignoreMissing )
			throw new UnsupportedOperationException( "Groovy script is missing the Closure: " + name );

		return null;
	}

	@Override
	public void release()
	{
		invokeClosure( true, false, "onRelease" );
		shell.resetLoadedClasses();
	}

	private class ScriptMetaClass extends DelegatingMetaClass
	{
		public ScriptMetaClass( MetaClass delegate )
		{
			super( delegate );

			initialize();
		}

		@Override
		public Object invokeMethod( Object object, String methodName, Object arguments )
		{
			Object[] args = arguments == null ? null : arguments instanceof Object[] ? ( Object[] )arguments
					: new Object[] { arguments };
			return doInvokeMethod( object, methodName, args );
		}

		@Override
		public Object invokeMethod( Object object, String methodName, Object[] arguments )
		{
			return doInvokeMethod( object, methodName, arguments );
		}

		private Object doInvokeMethod( Object object, String methodName, Object[] args )
		{
			try
			{
				return super.invokeMethod( object, methodName, args );
			}
			catch( MissingMethodException e )
			{
				return methodResolver.invokeMethod( methodName, args );
			}
		}

		@Override
		public Object getProperty( Object object, String property )
		{
			try
			{
				return super.getProperty( object, property );
			}
			catch( MissingPropertyException e )
			{
				return propertyResolver.getProperty( property );
			}
		}
	}
}
