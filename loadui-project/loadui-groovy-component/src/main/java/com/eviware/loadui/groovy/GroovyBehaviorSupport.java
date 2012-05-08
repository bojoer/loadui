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
package com.eviware.loadui.groovy;

import static com.eviware.loadui.util.groovy.resolvers.DelegatingResolver.noRelease;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingPropertyException;

import java.beans.PropertyChangeEvent;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.groovy.GroovyBehaviorProvider.ScriptDescriptor;
import com.eviware.loadui.impl.component.categories.BaseCategory;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.groovy.ClassLoaderRegistry;
import com.eviware.loadui.util.groovy.GroovyEnvironment;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.eviware.loadui.util.groovy.ParsedGroovyScript;
import com.eviware.loadui.util.groovy.resolvers.DelegatingResolver;
import com.eviware.loadui.util.groovy.resolvers.JavaBeanGroovyResolver;
import com.eviware.loadui.util.groovy.resolvers.PropertyHolderResolver;
import com.eviware.loadui.util.groovy.resolvers.ScheduledExecutionResolver;
import com.eviware.loadui.util.groovy.resolvers.StatisticHolderResolver;
import com.eviware.loadui.util.groovy.resolvers.TerminalHolderResolver;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class GroovyBehaviorSupport implements Releasable
{
	public final static String SCRIPT_PROPERTY = "_script";
	public final static String SCRIPT_FILE_ATTRIBUTE = "_scriptFile";
	public final static String DIGEST_ATTRIBUTE = "_digest";
	public final static String ID_ATTRIBUTE = "_id";
	public final static String CLASS_LOADER_ATTRIBUTE = "_classLoader";

	private final String id;
	private final String classLoaderId;
	private final Logger log;
	private final BaseCategory behavior;
	private final ComponentContext context;
	private final GroovyComponentContext groovyContext;
	private final ClassLoaderRegistry clr;
	private final String filePath;
	private final ScriptChangeListener scriptChangeListener;
	private final PropertyEventListener propertyEventListener;
	private final Property<String> scriptProperty;

	private GroovyEnvironment groovyEnv;
	private GroovyResolver resolver;

	public GroovyBehaviorSupport( GroovyBehaviorProvider behaviorProvider, BaseCategory behavior,
			ComponentContext context )
	{
		id = context.getAttribute( ID_ATTRIBUTE, context.getAttribute( ComponentItem.TYPE, context.getLabel() ) );
		log = LoggerFactory.getLogger( "com.eviware.loadui.groovy.component." + id );
		this.behavior = behavior;
		this.context = context;
		groovyContext = new GroovyComponentContext( context, log );
		clr = behaviorProvider.getClassLoaderRegistry();

		classLoaderId = context.getAttribute( GroovyBehaviorSupport.CLASS_LOADER_ATTRIBUTE, id );
		filePath = context.getAttribute( GroovyBehaviorSupport.SCRIPT_FILE_ATTRIBUTE, null );
		if( context.isController() )
		{
			scriptChangeListener = new ScriptChangeListener();
			behaviorProvider.addEventListener( PropertyChangeEvent.class, scriptChangeListener );
		}
		else
		{
			scriptChangeListener = null;
		}

		scriptProperty = context.createProperty( GroovyBehaviorSupport.SCRIPT_PROPERTY, String.class );
		propertyEventListener = new PropertyEventListener();
		context.getComponent().addEventListener( BaseEvent.class, propertyEventListener );

		updateScript( scriptProperty.getValue() );
	}

	public GroovyEnvironment getEnvironment()
	{
		return groovyEnv;
	}

	private void updateScript( String scriptText )
	{
		if( scriptText == null )
			scriptText = "";

		ReleasableUtils.releaseAll( groovyEnv, resolver );
		groovyContext.reset();

		ParsedGroovyScript headers = new ParsedGroovyScript( scriptText );
		PropertyHolderResolver propertyHolderResolver = new PropertyHolderResolver( context, log );

		resolver = new DelegatingResolver( noRelease( new JavaBeanGroovyResolver( groovyContext ) ),
				noRelease( new JavaBeanGroovyResolver( behavior ) ), new TotalResolver( behavior ),
				new ComponentTestExecutionResolver( context ), propertyHolderResolver, new TerminalHolderResolver( context,
						log ), new StatisticHolderResolver( context.getComponent() ), new ScheduledExecutionResolver(),
				noRelease( new JavaBeanGroovyResolver( context ) ) );

		Binding binding = new Binding();
		groovyEnv = GroovyEnvironment.newInstance( headers, id, "com.eviware.loadui.groovy.component", clr,
				classLoaderId, resolver, binding );

		propertyHolderResolver.invokeReplaceHandlers();
		try
		{
			Object defaultStatistics = binding.getVariable( "defaultStatistics" );
			if( defaultStatistics instanceof Iterable )
			{
				Set<Statistic.Descriptor> realDefaultStatistics = context.getDefaultStatistics();
				realDefaultStatistics.clear();
				for( Statistic.Descriptor statistic : Iterables.filter( ( Iterable<?> )defaultStatistics,
						Statistic.Descriptor.class ) )
				{
					realDefaultStatistics.add( statistic );
				}
			}
		}
		catch( MissingPropertyException e )
		{
			//Ignore
		}
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( groovyEnv, resolver, groovyContext );
	}

	private class PropertyEventListener implements WeakEventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof PropertyEvent )
			{
				PropertyEvent pEvent = ( PropertyEvent )event;
				if( PropertyEvent.Event.VALUE == pEvent.getEvent() && pEvent.getProperty() == scriptProperty )
				{
					try
					{
						updateScript( scriptProperty.getValue() );
					}
					catch( GroovyRuntimeException e )
					{
						log.error( "Error running Groovy script: ", e );
					}
				}
			}
			else if( event instanceof ActionEvent )
			{
				if( CanvasItem.START_ACTION.equals( event.getKey() ) )
				{
					groovyEnv.invokeClosure( true, false, "onStart" );
				}
				else if( CanvasItem.STOP_ACTION.equals( event.getKey() ) )
				{
					groovyEnv.invokeClosure( true, false, "onStop" );
				}
			}
		}
	}

	private class ScriptChangeListener implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( PropertyChangeEvent event )
		{
			if( Objects.equal( filePath, event.getPropertyName() ) || id.equals( event.getPropertyName() ) )
			{
				ScriptDescriptor descriptor = ( ScriptDescriptor )event.getSource();
				String digest = descriptor.getDigest();
				if( !digest.equals( context.getAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, null ) ) )
				{
					scriptProperty.setValue( descriptor.getScript() );
					context.setAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, digest );
				}
			}
		}
	}
}
