package com.eviware.loadui.launcher;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class LoadUICommandLineLauncher extends LoadUILauncher
{
	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		LoadUICommandLineLauncher launcher = new LoadUICommandLineLauncher( args );
		launcher.start();
	}

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

		options.addOption( "E", "echo", true, "Echos the argument" );

		return options;
	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		super.processCommandLine( cmd );

		if( cmd.hasOption( "E" ) )
		{
			System.out.println( "Echo " + cmd.getOptionValue( "E" ) );
		}
	}

	@Override
	protected void addJavaFxPackages()
	{
		// Don't add JavaFX packages for the command line runner.
	}
}
