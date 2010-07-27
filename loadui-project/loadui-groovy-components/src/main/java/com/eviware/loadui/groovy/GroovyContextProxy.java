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
package com.eviware.loadui.groovy;

import groovy.grape.Grape;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.util.MapUtils;

public class GroovyContextProxy extends GroovyObjectSupport implements InvocationHandler
{
	private final static Map<String, String> ALIASES = MapUtils.build( String.class, String.class ) //
			.put( "onTerminalMessage", "onMessage" ) //
			.put( "onTerminalConnect", "onConnect" ) //
			.put( "onTerminalDisconnect", "onDisconnect" ) //
			.put( "onTerminalSignatureChange", "onSignature" ) //
			.getImmutable();

	public static final Logger log = LoggerFactory.getLogger( GroovyContextProxy.class );

	private final Pattern m2Pattern = java.util.regex.Pattern.compile( ".*@m2repo (.*)\\s*" );
	private final Pattern depPattern = java.util.regex.Pattern.compile( ".*@dependency (.*)\\s*" );
	private final Pattern jarPattern = java.util.regex.Pattern.compile( ".*@jar (.*)\\s*" );

	private GroovyShell shell;
	private Binding binding;
	private final ComponentContext context;
	private final Property<String> scriptProperty;
	private Object proxy;
	private Object[] delegates;
	private final Collection<ComponentContext> activeContexts;

	private Script script;

	public GroovyContextProxy( ComponentContext context, String script, Class<?> extraInterface,
			Collection<ComponentContext> activeContexts )
	{
		this.activeContexts = activeContexts;
		this.context = context;
		this.shell = new GroovyShell();
		delegates = new Object[] { context };

		Class<?>[] interfaces = extraInterface == null ? new Class<?>[] { ComponentContext.class,
				ComponentBehavior.class, GroovyObject.class } : new Class<?>[] { ComponentContext.class,
				ComponentBehavior.class, GroovyObject.class, extraInterface };

		ClassLoader cl = GroovyContextProxy.class.getClassLoader();
		// UGLY HACK to make sure the Counter class is loaded by the classloader
		// before creating the Proxy.
		Counter c = null;
		proxy = Proxy.newProxyInstance( cl, interfaces, this );

		scriptProperty = context.createProperty( GroovyBehaviorProvider.SCRIPT_PROPERTY, String.class );

		if( script != null )
			scriptProperty.setValue( script );

		// Add these directly to the component to avoid them being removed on a
		// script change.
		context.getComponent().addEventListener( PropertyEvent.class, new ScriptListener() );
		context.getComponent().addEventListener( BaseEvent.class, new ReleaseListener() );

		activeContexts.add( context );
	}

	public void init()
	{
		// try
		// {
		scriptUpdated();
		// }
		// catch( Exception e )
		// {
		// e.printStackTrace();
		// }
	}

	public Object getProxy()
	{
		return proxy;
	}

	public void setDelegates( Object[] delegates )
	{
		this.delegates = delegates;
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
	{
		if( method.getDeclaringClass().isAssignableFrom( ComponentContext.class ) )
			return method.invoke( context, args );
		else if( method.getDeclaringClass().isAssignableFrom( GroovyObject.class ) )
			return method.invoke( this, args );

		return invokeMethod( method.getName(), args );
	}

	@Override
	public Object getProperty( String property )
	{
		Property<?> prop = context.getProperty( property );
		if( prop != null )
			return prop;

		try
		{
			return super.getProperty( property );
		}
		catch( MissingPropertyException e )
		{
			RuntimeException last = e;

			for( Object delegate : delegates )
			{
				try
				{
					return InvokerHelper.getProperty( delegate, property );
				}
				catch( MissingPropertyException e2 )
				{
					last = e2;
				}
				catch( InvokerInvocationException e2 )
				{
					last = e2;
				}
			}

			throw last;
		}
	}

	@Override
	public void setProperty( String property, Object value )
	{
		try
		{
			super.setProperty( property, value );
		}
		catch( MissingPropertyException e )
		{
			MissingPropertyException last = e;

			for( Object delegate : delegates )
			{
				try
				{
					InvokerHelper.setProperty( delegate, property, value );
					return;
				}
				catch( MissingPropertyException e2 )
				{
					last = e2;
				}
			}

			throw last;
		}
	}

	@Override
	public Object invokeMethod( String name, Object args )
	{
		Object[] argv = ( Object[] )( args == null ? new Object[] {} : args );

		if( binding != null && !"onRelease".equals( name ) )
		{
			try
			{
				Object prop = binding.getProperty( name );
				if( prop instanceof Closure )
				{
					Closure closure = ( Closure )prop;
					closure.setDelegate( this );
					// log.debug( "Invoking closure {} for Component {}", name,
					// context.getLabel() );
					try
					{
						return closure.call( argv );
					}
					catch( RuntimeException e1 )
					{
						throw e1;
					}
				}
			}
			catch( MissingPropertyException e )
			{
				if( ALIASES.containsKey( name ) )
				{
					try
					{
						Object prop = binding.getProperty( ALIASES.get( name ) );
						if( prop instanceof Closure )
						{
							Closure closure = ( Closure )prop;
							closure.setDelegate( this );
							try
							{
								return closure.call( argv );
							}
							catch( RuntimeException e1 )
							{
								throw e1;
							}
						}
					}
					catch( Exception e1 )
					{
						// Ignore
					}
				}
			}

			try
			{
				return super.invokeMethod( name, args );
			}
			catch( MissingMethodException e )
			{
				RuntimeException last = e;

				for( Object delegate : delegates )
				{
					try
					{
						return InvokerHelper.invokeMethod( delegate, name, args );
					}
					catch( InvokerInvocationException e2 )
					{
						last = e2;
					}
					catch( MissingMethodException e2 )
					{
						last = e2;
					}
				}

				log.error( "Missing method {} in delegates {}", name, delegates );
				throw last;
			}
		}

		return null;
	}

	private void scriptUpdated()
	{
		String scriptContent = scriptProperty.getValue();
		binding = new Binding();
		binding.setProperty( "log", log );

		if( scriptContent != null )
		{
			int headerStart = scriptContent.indexOf( "/**" );
			int headerEnd = scriptContent.indexOf( "*/" );
			if( headerStart > -1 && headerEnd > -1 )
			{
				String scriptHeader = scriptContent.substring( headerStart, headerEnd );
				loadDependencies( scriptHeader );
				context.setNonBlocking( scriptHeader.contains( "@nonBlocking true" ) );
			}
			else
			{
				context.setNonBlocking( false );
			}

			// try
			{
				if( script != null )
					InvokerHelper.removeClass( script.getClass() );

				script = shell.parse( scriptContent, "GroovyComponent" );
				script.setBinding( binding );

				HandleMetaClass hmc = new HandleMetaClass( script.getMetaClass() );
				hmc.setProperty( "methodMissing", new MethodClosure( this, "methodIsMissing" ) );
				hmc.setProperty( "propertyMissing", new MethodClosure( this, "propertyIsMissing" ) );
				script.setMetaClass( hmc );

				script.run();
			}
			// catch( Exception e )

			// log.error( "Error running component script", e );
			// }
		}
	}

	public Object methodIsMissing( String method, Object args )
	{
		Object[] argv = ( Object[] )args;
		RuntimeException last = new MissingMethodException( method, getClass(), argv );

		for( Object delegate : delegates )
		{
			try
			{
				return InvokerHelper.invokeMethod( delegate, method, args );
			}
			catch( InvokerInvocationException e )
			{
				last = e;
			}
			catch( MissingMethodException e )
			{
				last = e;
			}
		}

		throw last;
	}

	public Object propertyIsMissing( String name )
	{
		Property<?> property = context.getProperty( name );
		if( property != null )
			return property;

		for( Terminal terminal : context.getTerminals() )
			if( terminal.getLabel().equals( name ) )
				return terminal;

		if( ALIASES.containsKey( name ) )
			return binding.getProperty( ALIASES.get( name ) );

		for( Object delegate : delegates )
		{
			try
			{
				return InvokerHelper.getProperty( delegate, name );
			}
			catch( InvokerInvocationException e )
			{
			}
			catch( MissingPropertyException e )
			{
			}
		}

		throw new MissingPropertyException( name, getClass() );
	}

	private void loadDependencies( String scriptContent )
	{
		Matcher m2Matcher = m2Pattern.matcher( scriptContent );
		Matcher depMatcher = depPattern.matcher( scriptContent );
		Matcher jarMatcher = jarPattern.matcher( scriptContent );

		// StringBuilder sb = new StringBuilder( "import groovy.grape.Grape\n" );

		// boolean deps = false;
		// boolean grapes = false;
		int repos = 0;
		while( m2Matcher.find() )
		{
			// deps = true;
			// grapes = true;
			String url = m2Matcher.group( 1 );
			// sb.append( "Grape.addResolver(name:'repo_" + repos++ + "', root:'" +
			// url + "', m2compatible:true)\n" );
			Grape.addResolver( MapUtils.build( String.class, Object.class ).put( "name", "repo_" + repos++ ).put( "root",
					url ).put( "m2compatible", true ).get() );
		}

		while( depMatcher.find() )
		{
			String[] parts = depMatcher.group( 1 ).split( ":" );
			if( parts.length >= 3 )
			{
				log.debug( "Loading dependency using Grape: " + depMatcher.group( 1 ) );
				Grape.grab( MapUtils.build( String.class, Object.class ).put( "group", parts[0] ).put( "module", parts[1] )
						.put( "version", parts[2] ).put( "classLoader", shell.getClassLoader() ).get() );
			}
		}

		while( jarMatcher.find() )
		{
			String[] parts = jarMatcher.group( 1 ).split( ":" );

			if( parts.length >= 3 )
			{
				File depFile = new File( System.getProperty( "groovy.root" ), "grapes" + File.separator + parts[0]
						+ File.separator + parts[1] + File.separator + "jars" + File.separator + parts[1] + "-" + parts[2]
						+ ".jar" );
				if( depFile.exists() )
				{
					try
					{
						log.debug( "Manually loading jar: " + jarMatcher.group( 1 ) );
						shell.getClassLoader().addURL( depFile.toURI().toURL() );
					}
					catch( MalformedURLException e )
					{
						e.printStackTrace();
					}
				}
			}
		}

		// if( deps )
		// {
		// Binding binding = new Binding();
		// binding.setVariable( "classloader", shell.getClassLoader() );
		// Script s = shell.parse( sb.toString() );
		// s.setBinding( binding );
		// log.debug( "Loading dependencies..." );
		// s.run();
		// }
		log.debug( "Done loading dependencies!" );
	}

	private class ScriptListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( PropertyEvent.Event.VALUE == event.getEvent() && scriptProperty == event.getProperty() )
			{
				if( event.getPreviousValue() != null )
				{
					log.debug( "Invoking onRelease since script changed for {}", event.getSource() );
					invokeMethod( "onRelease", new Object[] {} );
					context.clearEventListeners();
				}

				scriptUpdated();
				context.getComponent().fireEvent( new BaseEvent( context.getComponent(), CanvasObjectItem.RELOADED ) );
			}
		}
	}

	private class ReleaseListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( ModelItem.RELEASED.equals( event.getKey() ) )
			{
				activeContexts.remove( context );

				if( script != null )
					InvokerHelper.removeClass( script.getClass() );
				script = null;
				shell.resetLoadedClasses();
				shell = null;
				if( binding != null )
				{
					try
					{
						Object prop = binding.getProperty( "onRelease" );
						if( prop instanceof Closure )
						{
							Closure closure = ( Closure )prop;
							closure.setDelegate( this );
							closure.call( new Object[] {} );
						}
					}
					catch( MissingPropertyException e )
					{
						// Ignore
					}
					binding = null;
				}
			}
		}
	}
}
