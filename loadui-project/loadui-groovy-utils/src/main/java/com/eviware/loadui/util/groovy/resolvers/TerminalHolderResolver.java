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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import com.eviware.loadui.api.component.ComponentContext.LikeFunction;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.MutableTerminalHolder;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;

/**
 * Adds Groovy methods for creating Terminals, as well as assigning likes
 * behavior to InputTerminals using Closures. It also provides accessing the
 * contained Terminals as properties.
 * 
 * @author dain.nilsson
 */
public class TerminalHolderResolver implements GroovyResolver.Properties, GroovyResolver.Methods
{
	private final MutableTerminalHolder terminalHolder;
	private final Logger log;

	public TerminalHolderResolver( @Nonnull MutableTerminalHolder terminalHolder, @Nullable Logger log )
	{
		this.terminalHolder = terminalHolder;
		this.log = log != null ? log : LoggerFactory.getLogger( TerminalHolderResolver.class );
	}

	private InputTerminal likes( final InputTerminal input, final Closure<Boolean> handler )
	{
		terminalHolder.setLikeFunction( input, new LikeFunction()
		{
			@Override
			public boolean call( OutputTerminal output )
			{
				try
				{
					return handler.call( output );
				}
				catch( Exception e )
				{
					log.error( "Exception caught when calling like function for " + input, e );
					return false;
				}
			}
		} );

		return input;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public Object invokeMethod( String methodName, Object... args ) throws MissingMethodException
	{
		if( methodName.equals( "createInput" ) )
		{
			int argLen = args.length;
			String name = Preconditions.checkNotNull( ( String )args[0] );
			Closure<Boolean> likesClosure = null;

			if( argLen > 1 )
			{
				Object lastArg = args[args.length - 1];
				if( lastArg instanceof Closure<?> )
				{
					likesClosure = ( Closure<Boolean> )args[ --argLen];
				}
			}

			String label = argLen > 1 ? ( String )args[1] : name;
			String description = argLen > 2 ? ( String )args[2] : null;

			InputTerminal terminal = terminalHolder.createInput( name, label, description );
			return likesClosure == null ? terminal : likes( terminal, likesClosure );
		}
		else if( methodName.equals( "createOutput" ) )
		{
			String name = Preconditions.checkNotNull( ( String )args[0] );
			String label = args.length > 1 ? ( String )args[1] : name;
			String description = args.length > 2 ? ( String )args[2] : null;

			return terminalHolder.createOutput( name, label, description );
		}
		else if( methodName.equals( "likes" ) )
		{
			return likes( Preconditions.checkNotNull( ( InputTerminal )args[0] ),
					Preconditions.checkNotNull( ( Closure<Boolean> )args[1] ) );
		}

		throw new MissingMethodException( methodName, TerminalHolderResolver.class, args );
	}

	@Override
	public Object getProperty( String propertyName ) throws MissingPropertyException
	{
		Terminal terminal = terminalHolder.getTerminalByName( propertyName );
		if( terminal != null )
			return terminal;

		throw new MissingPropertyException( propertyName, TerminalHolderResolver.class );
	}
}
