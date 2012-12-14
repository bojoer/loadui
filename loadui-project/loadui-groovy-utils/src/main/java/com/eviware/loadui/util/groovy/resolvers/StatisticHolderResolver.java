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
