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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;

/**
 * Adds Groovy methods for scheduling tasks with a
 * SingleThreadedExecutorService.
 * 
 * @author dain.nilsson
 */
public class ScheduledExecutionResolver implements GroovyResolver.Methods, Releasable
{
	private ScheduledExecutorService executor;

	@Override
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		if( methodName.equals( "submit" ) )
		{
			if( args.length == 1 )
			{
				Object task = args[0];
				if( task instanceof Runnable )
					return getExecutor().submit( ( Runnable )task );
				else if( task instanceof Callable<?> )
					return getExecutor().submit( ( Callable<?> )task );
			}
			else
			{
				return getExecutor().submit( ( Runnable )args[0], args[1] );
			}
		}
		else if( methodName.equals( "schedule" ) )
		{
			Preconditions.checkArgument( args.length == 3 );
			Object task = args[0];
			if( task instanceof Runnable )
				return getExecutor().schedule( ( Runnable )task, ( ( Number )args[1] ).longValue(), ( TimeUnit )args[2] );
			else if( task instanceof Callable<?> )
				return getExecutor().schedule( ( Callable<?> )task, ( ( Number )args[1] ).longValue(), ( TimeUnit )args[2] );
		}
		else if( methodName.equals( "scheduleWithFixedDelay" ) )
		{
			Preconditions.checkArgument( args.length == 4 );
			return getExecutor().scheduleWithFixedDelay( ( Runnable )args[0], ( ( Number )args[1] ).longValue(),
					( ( Number )args[2] ).longValue(), ( TimeUnit )args[3] );
		}
		else if( methodName.equals( "scheduleAtFixedRate" ) )
		{
			Preconditions.checkArgument( args.length == 4 );
			return getExecutor().scheduleAtFixedRate( ( Runnable )args[0], ( ( Number )args[1] ).longValue(),
					( ( Number )args[2] ).longValue(), ( TimeUnit )args[3] );
		}
		else if( methodName.equals( "cancelTasks" ) )
		{
			cancelTasks();
		}
		else
		{
			throw new MissingMethodException( methodName, ScheduledExecutionResolver.class, args );
		}

		return null;
	}

	protected synchronized ScheduledExecutorService getExecutor()
	{
		if( executor == null )
		{
			executor = Executors.newSingleThreadScheduledExecutor();
		}

		return executor;
	}

	private synchronized void cancelTasks()
	{
		if( executor != null )
		{
			executor.shutdownNow();
			executor = null;
		}
	}

	@Override
	public void release()
	{
		cancelTasks();
	}
}
