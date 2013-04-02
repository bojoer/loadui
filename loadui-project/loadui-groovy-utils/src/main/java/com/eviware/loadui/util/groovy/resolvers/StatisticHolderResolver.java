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
package com.eviware.loadui.util.groovy.resolvers;

import groovy.lang.MissingMethodException;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.eviware.loadui.util.statistics.StatisticDescriptorImpl;

public class StatisticHolderResolver implements GroovyResolver.Methods
{
	private final StatisticHolder statisticHolder;

	public StatisticHolderResolver( StatisticHolder statisticHolder )
	{
		this.statisticHolder = statisticHolder;
	}

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		if( methodName.equals( "statistic" ) )
		{
			return new StatisticDescriptorImpl( statisticHolder, ( String )args[0], ( String )args[1],
					args.length >= 3 ? ( String )args[2] : StatisticVariable.MAIN_SOURCE );
		}

		throw new MissingMethodException( methodName, StatisticHolderResolver.class, args );
	}
}
