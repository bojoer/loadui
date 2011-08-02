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
package com.eviware.loadui.util.groovy.resolvers;

import java.util.ArrayList;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.collect.Lists;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

/**
 * Combines multiple PropertyAndMethodResolvers into a single one, which tries
 * them in order until a property or method is resolved, failing only if all
 * delegates fail.
 * 
 * @author dain.nilsson
 */
public class DelegatingResolver implements GroovyResolver.Methods, GroovyResolver.Properties, Releasable
{
	/**
	 * Wrap a delegate with this method when constructing a new
	 * DelegatingResolver to prevent the DelegatingResolver to release the
	 * delegate when being released.
	 * 
	 * @param resolver
	 * @return
	 */
	public static GroovyResolver noRelease( GroovyResolver resolver )
	{
		return resolver instanceof Releasable ? new NonReleasingResolver( resolver ) : resolver;
	}

	private final ArrayList<GroovyResolver> delegates;

	public DelegatingResolver( GroovyResolver... delegates )
	{
		this.delegates = Lists.newArrayList( delegates );
	}

	public void addResolver( GroovyResolver resolver )
	{
		delegates.add( resolver );
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		for( GroovyResolver resolver : delegates )
		{
			if( resolver instanceof GroovyResolver.Methods )
			{
				try
				{
					return ( ( GroovyResolver.Methods )resolver ).invokeMethod( methodName, args );
				}
				catch( MissingMethodException e )
				{
					//Do nothing, try next.
				}
			}
		}

		throw new MissingMethodException( methodName, DelegatingResolver.class, args );
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		for( GroovyResolver resolver : delegates )
		{
			if( resolver instanceof GroovyResolver.Properties )
			{
				try
				{
					return ( ( GroovyResolver.Properties )resolver ).getProperty( propertyName );
				}
				catch( MissingPropertyException e )
				{
					//Do nothing, try next.
				}
			}
		}

		throw new MissingPropertyException( "No such property [" + propertyName + "] in any delegates", propertyName,
				DelegatingResolver.class );
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( delegates );
	}

	private static final class NonReleasingResolver implements GroovyResolver.Methods, GroovyResolver.Properties
	{
		private final Methods methodResolver;
		private final Properties propertyResolver;

		private NonReleasingResolver( GroovyResolver delegate )
		{
			methodResolver = ( Methods )( delegate instanceof Methods ? delegate : NULL_RESOLVER );
			propertyResolver = ( Properties )( delegate instanceof Properties ? delegate : NULL_RESOLVER );
		}

		@Override
		public Object getProperty( String propertyName ) throws MissingPropertyException
		{
			return propertyResolver.getProperty( propertyName );
		}

		@Override
		public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
		{
			return methodResolver.invokeMethod( methodName, args );
		}
	}
}