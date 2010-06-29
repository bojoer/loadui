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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.eviware.loadui.launcher.api.Command;
import com.eviware.loadui.launcher.impl.StringCommand;

public class LoadUICommandLineLauncher extends LoadUILauncher
{
	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		LoadUICommandLineLauncher launcher = new LoadUICommandLineLauncher( args );
		launcher.init();
		launcher.start();
	}

	private final List<Command> commands = new ArrayList<Command>();

	public LoadUICommandLineLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected Options createOptions()
	{
		Options options = super.createOptions();
		options.addOption( "P", "project", true, "Sets the Project file to run" );
		options.addOption( "T", "testcase", true, "Sets which TestCase to run (leave blank to run the entire Project)" );
		options.addOption( "L", "limits", true, "Sets the limits for the execution (e.g. -L 60;0;200 )" );

		return options;
	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		super.processCommandLine( cmd );

		if( cmd.hasOption( "T" ) )
			commands.add( new StringCommand( "println \"The workspace is ${workspace}\"" ) );

		for( int i = 0; i < 10; i++ )
			commands.add( new StringCommand( "println " + i ) );
	}

	@Override
	protected void start()
	{
		super.start();

		for( Command c : commands )
			framework.getBundleContext().registerService( Command.class.getName(), c, null );
	}
}
