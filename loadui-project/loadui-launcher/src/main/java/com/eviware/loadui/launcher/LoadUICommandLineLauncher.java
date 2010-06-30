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
package com.eviware.loadui.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.impl.FileGroovyCommand;
import com.eviware.loadui.launcher.impl.ResourceGroovyCommand;

public class LoadUICommandLineLauncher extends LoadUILauncher
{
	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		LoadUICommandLineLauncher launcher = new LoadUICommandLineLauncher( args );
		launcher.init();
		launcher.start();
	}

	private final List<GroovyCommand> commands = new ArrayList<GroovyCommand>();

	public LoadUICommandLineLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected Options createOptions()
	{
		Options options = super.createOptions();
		options.addOption( "W", "workspace", true, "Sets the Workspace file to load" );
		options.addOption( "P", "project", true, "Sets the Project file to run" );
		options.addOption( "T", "testcase", true, "Sets which TestCase to run (leave blank to run the entire Project)" );
		options.addOption( "L", "limits", true, "Sets the limits for the execution (e.g. -L 60;0;200 )" );
		options.addOption( "F", "file", true, "Executes the specified Groovy script file" );

		return options;
	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		super.processCommandLine( cmd );

		Map<String, Object> attributes = new HashMap<String, Object>();

		if( cmd.hasOption( "P" ) )
		{
			attributes.put( "workspaceFile", cmd.hasOption( "W" ) ? new File( cmd.getOptionValue( "W" ) ) : null );
			attributes.put( "projectFile", cmd.hasOption( "P" ) ? new File( cmd.getOptionValue( "P" ) ) : null );
			attributes.put( "testCase", cmd.getOptionValue( "T" ) );
			attributes.put( "limits", cmd.getOptionValue( "L" ) );

			commands.add( new ResourceGroovyCommand( "/RunTest.groovy", attributes ) );
		}
		else if( cmd.hasOption( "F" ) )
		{
			commands.add( new FileGroovyCommand( new File( cmd.getOptionValue( "F" ) ), attributes ) );
		}
		else
		{
			printUsageAndQuit();
		}
	}

	@Override
	protected void start()
	{
		super.start();

		for( GroovyCommand c : commands )
			framework.getBundleContext().registerService( GroovyCommand.class.getName(), c, null );
	}
}
