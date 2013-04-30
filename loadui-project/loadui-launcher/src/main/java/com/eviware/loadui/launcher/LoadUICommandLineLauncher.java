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
package com.eviware.loadui.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.impl.FileGroovyCommand;
import com.eviware.loadui.launcher.impl.ResourceGroovyCommand;

public class LoadUICommandLineLauncher extends LoadUILauncher
{
	protected static final String LOCAL_OPTION = "l";
	protected static final String FILE_OPTION = "f";
	protected static final String AGENT_OPTION = "a";
	protected static final String LIMITS_OPTION = "L";
	@Deprecated
	protected static final String TESTCASE_OPTION = "t";
	protected static final String VU_SCENARIO_OPTION = "v";
	protected static final String PROJECT_OPTION = "p";
	protected static final String WORKSPACE_OPTION = "w";
	protected static final String REPORT_DIR_OPTION = "r";
	protected static final String RETAIN_SAVED_ZOOM_LEVELS = "z";
	protected static final String REPORT_FORMAT_OPTION = "F";
	protected static final String STATISTICS_REPORT_OPTION = "S";
	protected static final String STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION = "s";
	protected static final String STATISTICS_REPORT_COMPARE_OPTION = "c";
	protected static final String ABORT_ONGOING_REQUESTS_OPTION = "A";

	public static void main( String[] args )
	{
		Application.launch( CommandApplication.class, args );
	}

	public LoadUICommandLineLauncher( String[] args )
	{
		super( args );
	}

	private static GroovyCommand command;

	@Override
	protected void beforeBundlesStart( Bundle[] bundles )
	{
		final Set<String> doNotStart = new HashSet<>( Arrays.asList( "loadui-pro-fx" ) );

		for( Bundle bundle : bundles )
		{
			if( is( bundle ).in( doNotStart ) )
			{
				try
				{
					System.out.println( "Uninstalling bundle: " + bundle.getSymbolicName() );
					bundle.uninstall();
				}
				catch( BundleException e )
				{
					e.printStackTrace();
				}
			}
		}

	}

	private ContainsSimilarItem is( Bundle bundle )
	{
		return new ContainsSimilarItem( bundle.getSymbolicName() );
	}

	private class ContainsSimilarItem
	{

		String name;

		public ContainsSimilarItem( String name )
		{
			this.name = name;
		}

		boolean in( Set<String> set )
		{
			for( String item : set )
				if( name.contains( item ) )
					return true;
			return false;
		}

	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		try (InputStream is = getClass().getResourceAsStream( "/packages-extra.txt" ))
		{
			if( is != null )
			{
				StringBuilder out = new StringBuilder();
				byte[] b = new byte[4096];
				for( int n; ( n = is.read( b ) ) != -1; )
					out.append( new String( b, 0, n ) );

				String extra = configProps.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "" );
				if( !extra.isEmpty() )
					out.append( "," ).append( extra );

				configProps.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, out.toString() );
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		Map<String, Object> attributes = new HashMap<>();

		if( cmd.hasOption( PROJECT_OPTION ) )
		{
			attributes.put( "workspaceFile",
					cmd.hasOption( WORKSPACE_OPTION ) ? new File( cmd.getOptionValue( WORKSPACE_OPTION ) ) : null );
			attributes.put( "projectFile",
					cmd.hasOption( PROJECT_OPTION ) ? new File( cmd.getOptionValue( PROJECT_OPTION ) ) : null );
			attributes.put( "testCase", cmd.getOptionValue( TESTCASE_OPTION ) );
			if( cmd.getOptionValue( VU_SCENARIO_OPTION ) != null )
				attributes.put( "testCase", cmd.getOptionValue( VU_SCENARIO_OPTION ) );
			attributes.put( "limits", cmd.hasOption( LIMITS_OPTION ) ? cmd.getOptionValue( LIMITS_OPTION ).split( ":" )
					: null );
			attributes.put( "localMode", cmd.hasOption( LOCAL_OPTION ) );
			Map<String, String[]> agents = null;
			if( cmd.hasOption( AGENT_OPTION ) )
			{
				agents = new HashMap<>();
				for( String option : cmd.getOptionValues( AGENT_OPTION ) )
				{
					int ix = option.indexOf( "=" );
					if( ix != -1 )
						agents.put( option.substring( 0, ix ), option.substring( ix + 1 ).split( "," ) );
					else
						agents.put( option, null );
				}
			}
			attributes.put( "agents", agents );

			attributes.put( "reportFolder", cmd.getOptionValue( REPORT_DIR_OPTION ) );
			attributes.put( "reportFormat",
					cmd.hasOption( REPORT_FORMAT_OPTION ) ? cmd.getOptionValue( REPORT_FORMAT_OPTION ) : "PDF" );

			List<String> statisticPages = null;
			if( cmd.hasOption( STATISTICS_REPORT_OPTION ) )
			{
				String[] optionValues = cmd.getOptionValues( STATISTICS_REPORT_OPTION );
				statisticPages = optionValues == null ? Collections.<String> emptyList() : Arrays.asList( optionValues );
			}
			attributes.put( "statisticPages", statisticPages );
			attributes.put( "compare", cmd.getOptionValue( STATISTICS_REPORT_COMPARE_OPTION ) );

			attributes.put( "abort", cmd.getOptionValue( ABORT_ONGOING_REQUESTS_OPTION ) );

			attributes.put( "includeSummary", cmd.hasOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION ) );

			attributes.put( "retainZoom", cmd.hasOption( RETAIN_SAVED_ZOOM_LEVELS ) );

			command = new ResourceGroovyCommand( "/RunTest.groovy", attributes );
		}
		else if( cmd.hasOption( FILE_OPTION ) )
		{
			command = new FileGroovyCommand( new File( cmd.getOptionValue( FILE_OPTION ) ), attributes );
		}
		else
		{
			printUsageAndQuit();
		}
	}

	@Override
	@SuppressWarnings( "static-access" )
	protected Options createOptions()
	{
		Options options = super.createOptions();
		options.addOption( WORKSPACE_OPTION, "workspace", true, "Sets the Workspace file to load" );
		options.addOption( PROJECT_OPTION, "project", true, "Sets the Project file to run" );
		options.addOption( TESTCASE_OPTION, "testcase", true,
				"Sets which TestCase to run (leave blank to run the entire Project)" );
		options.addOption( VU_SCENARIO_OPTION, "scenario", true,
				"Sets which Scenario to run (leave blank to run the entire Project)" );
		options.addOption( LIMITS_OPTION, "limits", true,
				"Sets the limits (<SECONDS>:<REQUESTS>:<FAILURES>) for the execution (e.g. -L 60:0:200 )" );
		options.addOption( OptionBuilder
				.withLongOpt( "agents" )
				.withDescription(
						"Sets the agents to use for the test ( usage -" + AGENT_OPTION
								+ " <ip>[:<port>][=<scenario>[,<scenario>] ...] )" ).hasArgs().create( AGENT_OPTION ) );
		options.addOption( FILE_OPTION, "file", true, "Executes the specified Groovy script file" );
		options.addOption( LOCAL_OPTION, "local", false, "Executes TestCases in local mode" );
		options.addOption( REPORT_DIR_OPTION, "reports", true, "Generates reports and saves them in specified folder" );
		options
				.addOption( REPORT_FORMAT_OPTION, "format", true,
						"Specify output format for the exported reports (supported formats are: PDF, XLS, HTML, RTF, CSV, TXT and XML)" );
		options
				.addOption( OptionBuilder
						.withLongOpt( "statistics" )
						.withDescription(
								"Sets which Statistics pages to add to the generated report (leave blank save all pages)" )
						.hasOptionalArgs().create( STATISTICS_REPORT_OPTION ) );
		options.addOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION, "summary", false,
				"Set to include summary report in statistics report" );
		options.addOption( STATISTICS_REPORT_COMPARE_OPTION, "compare", true,
				"Specify a saved execution to use as a base for comparison in the generated statistics report" );
		options
				.addOption(
						ABORT_ONGOING_REQUESTS_OPTION,
						"abort",
						true,
						"Overrides \"Abort ongoing requests on finish\" project property. If set to true ongoing requests will be canceled, if false test will finish when all ongoing requests complete. If not set, property value from project will be used to determine what to do with ongoing requests." );

		options.addOption( RETAIN_SAVED_ZOOM_LEVELS, false, "Use the saved zoom levels for charts from the project." );

		return options;
	}

	public static class CommandApplication extends Application
	{
		private LoadUILauncher launcher;

		@Override
		public void start( final Stage stage ) throws Exception
		{
			System.out.println( "CommandApplication starting" );

			Task<Void> task = new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					System.setSecurityManager( null );
					launcher = createLauncher( getParameters().getRaw().toArray( new String[0] ) );
					launcher.init();
					launcher.start();

					System.out.println( "command: " + command );

					if( command != null )
						framework.getBundleContext().registerService( GroovyCommand.class.getName(), command, null );

					return null;
				}
			};

			new Thread( task ).start();
		}

		protected LoadUILauncher createLauncher( String[] args )
		{
			return new LoadUICommandLineLauncher( args );
		}

		@Override
		public void stop() throws Exception
		{
			launcher.framework.getBundleContext().getBundle( 0 ).stop();
		}
	}
}
