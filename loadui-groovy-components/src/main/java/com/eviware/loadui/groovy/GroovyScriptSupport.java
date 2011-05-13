package com.eviware.loadui.groovy;

import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.groovy.GroovyBehaviorProvider.ScriptDescriptor;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

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

	private final static Map<String, String> ALIASES = ImmutableMap.of( "onTerminalMessage", "onMessage",
			"onTerminalConnect", "onConnect", "onTerminalDisconnect", "onDisconnect", "onTerminalSignatureChange",
			"onSignature" );

	private final String id;
	private final ClassLoaderRegistry clr;
	private final String classLoaderId;
	private final GroovyComponentClassLoader classLoader;
	private final GroovyShell shell;

	private final Logger log;
	private final PropertyEventListener propertyEventListener;
	private final ScriptChangeListener scriptChangeListener;

	private final ComponentBehavior behavior;
	private final GroovyContextSupport context;
	private final Property<String> scriptProperty;
	private final String scriptName;
	private final String filePath;
	private Binding binding = new Binding();
	private String digest;

	public GroovyScriptSupport( GroovyBehaviorProvider behaviorProvider, ComponentBehavior behavior,
			ComponentContext context )
	{
		id = context.getAttribute( GroovyComponent.ID_ATTRIBUTE,
				context.getAttribute( ComponentItem.TYPE, context.getLabel() ) );
		scriptName = "Groovy" + id.replaceAll( "[^a-zA-Z]", "" );
		log = LoggerFactory.getLogger( "com.eviware.loadui.groovy." + id );

		this.behavior = behavior;
		this.context = new GroovyContextSupport( context, log );

		clr = behaviorProvider.getClassLoaderRegistry();

		classLoaderId = context.getAttribute( GroovyComponent.CLASS_LOADER_ATTRIBUTE, id );
		classLoader = clr.useClassLoader( classLoaderId, this );
		shell = new GroovyShell( classLoader );

		scriptProperty = context.createProperty( GroovyComponent.SCRIPT_PROPERTY, String.class );
		propertyEventListener = new PropertyEventListener();
		context.getComponent().addEventListener( PropertyEvent.class, propertyEventListener );

		digest = context.getAttribute( GroovyComponent.DIGEST_ATTRIBUTE, null );
		filePath = context.getAttribute( GroovyComponent.SCRIPT_FILE_ATTRIBUTE, null );
		if( context.isController() )
		{
			scriptChangeListener = new ScriptChangeListener();
			behaviorProvider.addEventListener( PropertyChangeEvent.class, scriptChangeListener );
		}
		else
		{
			scriptChangeListener = null;
		}

		updateScript( scriptProperty.getValue() );
	}

	public Logger getLog()
	{
		return log;
	}

	public void updateScript( String scriptText )
	{
		invokeClosure( true, false, "onReplace" );
		context.reset();
		shell.resetLoadedClasses();
		loadDependencies( scriptText );

		try
		{
			Script script = shell.parse( scriptText, scriptName );
			binding = new Binding();
			binding.setProperty( "log", log );
			script.setMetaClass( new ScriptMetaClass( script.getMetaClass() ) );

			script.setBinding( binding );
			script.run();
			context.invokeReplaceHandlers();
		}
		catch( Exception e )
		{
			log.error( "Compilation of Groovy script failed: ", e );
		}
	}

	public Object invokeClosure( boolean ignoreMissing, boolean returnException, String name, Object... args )
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
				return invokeClosure( ignoreMissing, returnException, ALIASES.get( name ), args );
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
		ReleasableUtils.release( context );
		shell.resetLoadedClasses();
	}

	private void loadDependencies( final String scriptContent )
	{
		Matcher m2Matcher = m2Pattern.matcher( scriptContent );
		Matcher depMatcher = depPattern.matcher( scriptContent );

		int repos = 0;
		while( m2Matcher.find() )
		{
			String url = m2Matcher.group( 1 );
			Map<String, Object> args = Maps.newHashMap();
			args.put( "name", "repo_" + repos++ );
			args.put( "root", url );
			args.put( "m2compatible", true );
			Grape.addResolver( args );
		}

		while( depMatcher.find() )
		{
			String[] parts = depMatcher.group( 1 ).split( ":" );
			if( parts.length >= 3 )
				classLoader.loadDependency( parts[0], parts[1], parts[2] );
		}
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

	private class ScriptChangeListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( PropertyChangeEvent event )
		{
			if( filePath.equals( event.getPropertyName() ) || id.equals( event.getPropertyName() ) )
			{
				ScriptDescriptor descriptor = ( ScriptDescriptor )event.getSource();
				scriptProperty.setValue( descriptor.getScript() );
				context.setAttribute( GroovyComponent.DIGEST_ATTRIBUTE, descriptor.getDigest() );
			}
		}
	}
}
