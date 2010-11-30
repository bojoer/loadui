/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.cmd;

import groovy.ui.Console;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.api.OSGiUtils;

public class CommandRunner
{
	public static final Logger log = LoggerFactory.getLogger( CommandRunner.class );

	private final ExecutorService executor;
	private final WorkspaceProvider workspaceProvider;
	private final Console console;

	public CommandRunner( WorkspaceProvider workspaceProvider )
	{
		this.executor = Executors.newSingleThreadScheduledExecutor( new ThreadFactory()
		{
			@Override
			public Thread newThread( Runnable r )
			{
				return new Thread( r, "CommandRunner" );
			}
		} );
		this.workspaceProvider = workspaceProvider;

		console = new Console();
	}

	public void execute( GroovyCommand command, Map<String, String> properties )
	{
		executor.execute( new CommandRunnable( command ) );
	}

	public void destroy()
	{
		executor.shutdown();
	}

	private class CommandRunnable implements Runnable
	{
		private final GroovyCommand command;

		public CommandRunnable( GroovyCommand command )
		{
			this.command = command;
		}

		public void run()
		{
			console.setVariable( "log", log );
			console.setVariable( "workspaceProvider", workspaceProvider );
			console.setVariable( "workspace", workspaceProvider.getWorkspace() );
			for( Entry<String, Object> entry : command.getAttributes().entrySet() )
				console.setVariable( entry.getKey(), entry.getValue() );

			Object result = 1;
			try
			{
				result = console.getShell().evaluate( command.getScript() );
			}
			catch( CompilationFailedException e )
			{
				log.error( "An error occured when compiling the script", e );
			}
			catch( RuntimeException e )
			{
				log.error( "An error occured when executing the script", e );
			}
			console.clearContext();

			if( command.exitOnCompletion() )
			{
				if( result instanceof Number )
					OSGiUtils.shutdown( ( ( Number )result ).intValue() );
				else if( result instanceof Boolean )
					OSGiUtils.shutdown( ( Boolean )result ? 0 : 1 );
				else
					OSGiUtils.shutdown();
			}
		}
	}
}
