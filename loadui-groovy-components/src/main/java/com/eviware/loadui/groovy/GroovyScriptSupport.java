package com.eviware.loadui.groovy;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.groovy.GroovyBehaviorProvider.ScriptDescriptor;
import com.eviware.loadui.util.MapUtils;

import groovy.grape.Grape;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

public class GroovyScriptSupport implements Releasable
{
	private static final Pattern m2Pattern = java.util.regex.Pattern.compile( ".*@m2repo (.*)\\s*" );
	private static final Pattern depPattern = java.util.regex.Pattern.compile( ".*@dependency (.*)\\s*" );

	private final static Map<String, String> ALIASES = MapUtils.build( String.class, String.class ) //
			.put( "onTerminalMessage", "onMessage" ) //
			.put( "onTerminalConnect", "onConnect" ) //
			.put( "onTerminalDisconnect", "onDisconnect" ) //
			.put( "onTerminalSignatureChange", "onSignature" ) //
			.getImmutable();

	private final GroovyShell shell = new GroovyShell();

	private final Logger log;
	private final PropertyEventListener propertyEventListener;
	private final ScriptFileListener scriptFileListener;

	private final ComponentBehavior behavior;
	private final GroovyContextSupport context;
	private final Property<String> scriptProperty;
	private final String scriptName;
	private final String filePath;
	private Binding binding;
	private String digest;

	public GroovyScriptSupport( EventFirer scriptUpdateFirer, ComponentBehavior behavior, ComponentContext context )
	{
		this.behavior = behavior;
		this.context = new GroovyContextSupport( context );

		scriptName = "Groovy" + context.getLabel().replaceAll( "[^a-zA-Z]", "" );
		log = LoggerFactory.getLogger( "com.eviware.loadui.groovy." + scriptName );

		scriptProperty = context.createProperty( GroovyComponent.SCRIPT_PROPERTY, String.class );
		propertyEventListener = new PropertyEventListener();
		context.getComponent().addEventListener( PropertyEvent.class, propertyEventListener );

		digest = context.getAttribute( GroovyComponent.DIGEST_ATTRIBUTE, null );
		filePath = context.getAttribute( GroovyComponent.SCRIPT_FILE_ATTRIBUTE, null );
		if( context.isController() && digest != null && filePath != null )
		{
			scriptFileListener = new ScriptFileListener();
			scriptUpdateFirer.addEventListener( PropertyChangeEvent.class, scriptFileListener );
		}
		else
		{
			scriptFileListener = null;
		}

		updateScript( scriptProperty.getValue() );
	}

	public void updateScript( String scriptText )
	{
		shell.resetLoadedClasses();
		context.reset();
		loadDependencies( scriptText );

		try
		{
			Script script = shell.parse( scriptText, scriptName );
			binding = new Binding();
			binding.setProperty( "log", GroovyComponent.log );
			script.setMetaClass( new ScriptMetaClass( script.getMetaClass() ) );

			script.setBinding( binding );
			script.run();
		}
		catch( Exception e )
		{
			log.error( "Compilation of Groovy script failed: ", e );
		}
	}

	public Object invokeClosure( boolean ignoreMissing, String name, Object... args )
	{
		try
		{
			Object property = binding.getProperty( name );
			if( property instanceof Closure )
				return ( ( Closure<?> )property ).call( args );
		}
		catch( MissingPropertyException e )
		{
			if( ALIASES.containsKey( name ) )
				return invokeClosure( ignoreMissing, ALIASES.get( name ), args );
		}

		if( !ignoreMissing )
			throw new UnsupportedOperationException( "Groovy script is missing the Closure: " + name );

		return null;

	}

	@Override
	public void release()
	{
		invokeClosure( true, "onRelease" );
		context.reset();
		context.clearEventListeners();
	}

	private void loadDependencies( final String scriptContent )
	{
		Matcher m2Matcher = m2Pattern.matcher( scriptContent );
		Matcher depMatcher = depPattern.matcher( scriptContent );

		int repos = 0;
		while( m2Matcher.find() )
		{
			String url = m2Matcher.group( 1 );
			Grape.addResolver( MapUtils.build( String.class, Object.class ).put( "name", "repo_" + repos++ )
					.put( "root", url ).put( "m2compatible", true ).get() );
		}

		while( depMatcher.find() )
		{
			String[] parts = depMatcher.group( 1 ).split( ":" );
			if( parts.length >= 3 )
			{
				if( Boolean.getBoolean( "loadui.grape.disable" ) )
				{
					File depFile = new File( System.getProperty( "groovy.root" ), "grapes" + File.separator + parts[0]
							+ File.separator + parts[1] + File.separator + "jars" + File.separator + parts[1] + "-" + parts[2]
							+ ".jar" );
					if( depFile.exists() )
					{
						try
						{
							log.debug( "Manually loading jar: " + depMatcher.group( 1 ) );
							shell.getClassLoader().addURL( depFile.toURI().toURL() );
						}
						catch( MalformedURLException e )
						{
							e.printStackTrace();
						}
					}
				}
				else
				{
					log.debug( "Loading dependency using Grape: " + depMatcher.group( 1 ) );
					final ClassLoader cl = Thread.currentThread().getContextClassLoader();
					try
					{
						Thread.currentThread().setContextClassLoader( shell.getClassLoader() );

						Grape.grab( MapUtils.build( String.class, Object.class ).put( "group", parts[0] )
								.put( "module", parts[1] ).put( "version", parts[2] )
								.put( "classLoader", shell.getClassLoader() ).get() );
					}
					catch( Exception e )
					{
						log.error( "Failed loading dependencies using Grape, fallback to manual jar loading.", e );
						System.setProperty( "loadui.grape.disable", "true" );
						loadDependencies( scriptContent );
						return;
					}
					finally
					{
						Thread.currentThread().setContextClassLoader( cl );
					}
				}
			}
		}

		log.debug( "Done loading dependencies!" );
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
				try
				{
					return InvokerHelper.invokeMethod( context, methodName, args );
				}
				catch( MissingMethodException e1 )
				{
					return InvokerHelper.invokeMethod( behavior, methodName, args );
				}
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
				try
				{
					return InvokerHelper.getProperty( context, property );
				}
				catch( MissingPropertyException e1 )
				{
					try
					{
						return InvokerHelper.getProperty( behavior, property );
					}
					catch( MissingPropertyException e2 )
					{
						Property<?> prop = context.getProperty( property );
						if( prop != null )
							return prop;
						for( Terminal terminal : context.getTerminals() )
							if( property.equals( terminal.getLabel() ) )
								return terminal;

						throw e2;
					}
				}
			}
		}
	}

	private class PropertyEventListener implements WeakEventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( PropertyEvent.Event.VALUE.equals( event.getEvent() ) && event.getProperty() == scriptProperty )
				updateScript( scriptProperty.getValue() );
		}
	}

	private class ScriptFileListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( PropertyChangeEvent event )
		{
			if( filePath.equals( event.getPropertyName() ) )
			{
				ScriptDescriptor descriptor = ( ScriptDescriptor )event.getSource();
				scriptProperty.setValue( descriptor.getScript() );
				context.setAttribute( GroovyComponent.DIGEST_ATTRIBUTE, descriptor.getDigest() );
			}
		}
	}
}
