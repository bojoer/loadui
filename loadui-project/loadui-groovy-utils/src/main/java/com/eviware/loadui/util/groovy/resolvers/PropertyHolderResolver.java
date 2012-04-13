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
package com.eviware.loadui.util.groovy.resolvers;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.slf4j.Logger;

import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;

/**
 * Adds convenient Groovy methods for dealing with Properties for a
 * PropertyHolder, and provides these properties to the script.
 * 
 * @author dain.nilsson
 */
public class PropertyHolderResolver implements GroovyResolver.Methods, GroovyResolver.Properties, Releasable
{
	private final PropertyHolder propertyHolder;
	private final Logger log;
	private final PropertyEventHandler handler = new PropertyEventHandler();
	private final Set<PropertyHolder> holders = Collections.newSetFromMap( new WeakHashMap<PropertyHolder, Boolean>() );

	public PropertyHolderResolver( PropertyHolder propertyHolder, Logger log )
	{
		this.propertyHolder = propertyHolder;
		this.log = log;

		propertyHolder.addEventListener( PropertyEvent.class, handler );
		holders.add( propertyHolder );
	}

	/**
	 * Invokes all registered onReplace handlers with the current property value
	 * (may be needed for initialization).
	 */
	public void invokeReplaceHandlers()
	{
		for( Property<?> property : new HashSet<Property<?>>( handler.replaceHandlers.keySet() ) )
			handler.handleEvent( new PropertyEvent( property.getOwner(), property, PropertyEvent.Event.VALUE, null ) );
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		Preconditions.checkNotNull( methodName );

		if( methodName.equals( "createProperty" ) )
		{
			String propertyName = ( String )Preconditions.checkNotNull( args[0] );
			Class<?> propertyType = ( Class<?> )Preconditions.checkNotNull( args[1] );
			Closure<?> replaceHandler = null;

			int arrLength = args.length;

			Object initialValue = null;
			boolean propagates = true;

			if( arrLength > 2 )
			{
				Object lastArg = args[args.length - 1];
				if( lastArg instanceof Closure<?> )
				{
					replaceHandler = ( Closure<?> )args[ --arrLength];
				}

				if( arrLength > 2 )
					initialValue = args[2];
				if( arrLength > 3 )
					propagates = ( Boolean )args[3];
			}

			Property<?> property = propertyHolder.createProperty( propertyName, propertyType, initialValue, propagates );
			if( replaceHandler != null )
			{
				this.handler.replaceHandlers.put( property, replaceHandler );
			}

			return property;
		}
		else if( methodName.equals( "deleteProperty" ) )
		{
			propertyHolder.deleteProperty( ( String )Preconditions.checkNotNull( args[0] ) );
		}
		else if( methodName.equals( "onReplace" ) )
		{
			Property<?> property = ( Property<?> )Preconditions.checkNotNull( args[0] );
			Closure<?> replaceHandler = ( Closure<?> )Preconditions.checkNotNull( args[1] );

			this.handler.replaceHandlers.put( property, replaceHandler );
			PropertyHolder owner = property.getOwner();
			if( holders.add( owner ) )
			{
				owner.addEventListener( PropertyEvent.class, handler );
			}

			return property;
		}
		else
			throw new MissingMethodException( methodName, PropertyHolderResolver.class, args );

		return null;
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		Property<?> property = propertyHolder.getProperty( propertyName );
		if( property == null )
			throw new MissingPropertyException( "No such Property", propertyName, PropertyHolderResolver.class );

		return property;
	}

	@Override
	public void release()
	{
		handler.replaceHandlers.clear();
		for( PropertyHolder holder : holders )
		{
			holder.removeEventListener( PropertyEvent.class, handler );
		}
	}

	private class PropertyEventHandler implements WeakEventHandler<PropertyEvent>
	{
		private final HashMap<Property<?>, Closure<?>> replaceHandlers = new HashMap<Property<?>, Closure<?>>();

		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( PropertyEvent.Event.VALUE == event.getEvent() )
			{
				Closure<?> replaceHandler = replaceHandlers.get( event.getProperty() );
				if( replaceHandler != null )
				{
					try
					{
						switch( replaceHandler.getMaximumNumberOfParameters() )
						{
						case 0 :
							replaceHandler.call();
							break;
						case 1 :
							replaceHandler.call( event.getProperty().getValue() );
							break;
						case 2 :
						default :
							replaceHandler.call( event.getProperty().getValue(), event.getPreviousValue() );
						}
					}
					catch( Exception e )
					{
						log.error( "Exception caught when calling onReplace handler for " + event.getProperty().getKey(), e );
					}
				}
			}
		}
	}
}
