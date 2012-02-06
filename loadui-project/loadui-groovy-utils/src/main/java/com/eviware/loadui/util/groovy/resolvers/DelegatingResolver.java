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

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.ArrayList;
import java.util.HashSet;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

	private final ArrayList<GroovyResolver.Properties> propertyDelegates = Lists.newArrayList();
	private final ArrayList<GroovyResolver.Methods> methodDelegates = Lists.newArrayList();
	private final HashSet<GroovyResolver> releasables = Sets.newHashSet();

	public DelegatingResolver( GroovyResolver... delegates )
	{
		for( GroovyResolver resolver : delegates )
			addResolver( resolver );
	}

	public void addResolver( GroovyResolver resolver )
	{
		if( resolver instanceof Releasable )
		{
			releasables.add( resolver );
		}
		else if( resolver instanceof NonReleasingResolver )
		{
			resolver = ( ( NonReleasingResolver )resolver ).resolver;
		}

		if( resolver instanceof GroovyResolver.Methods )
		{
			methodDelegates.add( ( GroovyResolver.Methods )resolver );
		}
		if( resolver instanceof GroovyResolver.Properties )
		{
			propertyDelegates.add( ( GroovyResolver.Properties )resolver );
		}
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		for( GroovyResolver.Methods resolver : methodDelegates )
		{
			try
			{
				return resolver.invokeMethod( methodName, args );
			}
			catch( MissingMethodException e )
			{
				//Do nothing, try next.
			}
		}

		throw new MissingMethodException( methodName, DelegatingResolver.class, args );
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		for( GroovyResolver.Properties resolver : propertyDelegates )
		{
			try
			{
				return resolver.getProperty( propertyName );
			}
			catch( MissingPropertyException e )
			{
				//Do nothing, try next.
			}
		}

		throw new MissingPropertyException( "No such property [" + propertyName + "] in any delegates", propertyName,
				DelegatingResolver.class );
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( releasables );
		methodDelegates.clear();
		propertyDelegates.clear();
	}

	private static final class NonReleasingResolver implements GroovyResolver.Properties, GroovyResolver.Methods
	{
		private final GroovyResolver resolver;
		private final Methods methodResolver;
		private final Properties propertyResolver;

		private NonReleasingResolver( GroovyResolver delegate )
		{
			resolver = delegate;
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