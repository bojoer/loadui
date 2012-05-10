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

import java.util.Map;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Provides a Groovy DSL for assigning TestExecutionTasks to be run as part of
 * the TestExecution. Also allows the creation of MessageTestEvents using the
 * "notify" and "warn" methods.
 * 
 * @author dain.nilsson
 */
public class TestExecutionResolver implements GroovyResolver.Properties, GroovyResolver.Methods, Releasable
{
	private static void logTestEventMessage( MessageLevel level, Object[] args )
	{
		if( args.length == 2 )
		{
			Preconditions.checkArgument( args[1] instanceof Number, "%s must be numeric!", args[1] );

			BeanInjector.getBean( TestEventManager.class ).logMessage( level, String.valueOf( args[0] ),
					( ( Number )args[1] ).longValue() );
		}
		else
		{
			BeanInjector.getBean( TestEventManager.class ).logMessage( level, String.valueOf( args[0] ) );
		}
	}

	private final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );
	private ClosureTestExecutionTask task;

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		if( "notify".equals( methodName ) )
		{
			logTestEventMessage( MessageLevel.NOTIFICATION, args );
		}
		else if( "warn".equals( methodName ) )
		{
			logTestEventMessage( MessageLevel.WARNING, args );
		}
		else if( "duringPhase".equals( methodName ) )
		{
			if( args.length >= 2 && args[args.length - 1] instanceof Closure )
			{
				if( task == null )
				{
					task = createTask();
				}

				Closure<?> closure = ( Closure<?> )args[args.length - 1];
				for( int i = 0; i < args.length - 1; i++ )
				{
					Phase phase = args[i] instanceof Phase ? ( Phase )args[i] : Phase.valueOf( String.valueOf( args[i] )
							.toUpperCase() );
					task.phaseTasks.put( phase, closure );
					testRunner.registerTask( task, phase );
				}
			}
			else if( args.length == 1 )
			{
				Phase phase = args[0] instanceof Phase ? ( Phase )args[0] : Phase.valueOf( String.valueOf( args[0] )
						.toUpperCase() );
				if( task != null && task.phaseTasks.containsKey( phase ) )
				{
					task.phaseTasks.remove( phase );
					if( task.phaseTasks.isEmpty() )
					{
						release();
					}
				}
			}
		}
		else
		{
			throw new MissingMethodException( methodName, TestExecutionResolver.class, args );
		}

		return null;
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		try
		{
			return Phase.valueOf( propertyName );
		}
		catch( Exception e )
		{
			throw new MissingPropertyException( propertyName, TestExecutionResolver.class );
		}
	}

	@Override
	public void release()
	{
		if( task != null )
		{
			testRunner.unregisterTask( task, Phase.values() );
			task.phaseTasks.clear();
			task = null;
		}
	}

	protected ClosureTestExecutionTask createTask()
	{
		return new ClosureTestExecutionTask();
	}

	protected static class ClosureTestExecutionTask implements TestExecutionTask
	{
		private final Map<Phase, Closure<?>> phaseTasks = Maps.newHashMap();

		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( phaseTasks.containsKey( phase ) )
			{
				Closure<?> closure = phaseTasks.get( phase );
				switch( closure.getMaximumNumberOfParameters() )
				{
				case 0 :
					closure.call();
					break;
				case 1 :
					closure.call( execution );
					break;
				default :
					closure.call( execution, phase );
				}
			}
		}
	}
}
