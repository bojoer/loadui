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
package com.eviware.loadui.groovy;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.Map;

import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.impl.component.categories.BaseCategory;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class TotalResolver implements GroovyResolver.Methods, GroovyResolver.Properties
{
	private final Map<String, Value<Number>> totals = Maps.newHashMap();
	private final BaseCategory behavior;

	public TotalResolver( BaseCategory behavior )
	{
		this.behavior = behavior;
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		if( totals.containsKey( propertyName ) )
		{
			return totals.get( propertyName );
		}

		throw new MissingPropertyException( propertyName );
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		if( "total".equals( methodName ) )
		{
			Preconditions.checkArgument( args.length == 2 );
			String name = ( String )args[0];
			@SuppressWarnings( "unchecked" )
			Closure<Number> closure = ( Closure<Number> )args[1];
			Value<Number> total = behavior.createTotal( name, closure );
			totals.put( name, total );

			return total;
		}

		throw new MissingMethodException( methodName, TotalResolver.class, args );
	}
}
