/*
 * Copyright 2011 eviware software ab
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.impl.FileGroovyCommand;
import com.eviware.loadui.launcher.impl.ResourceGroovyCommand;

public class LoadUICommandLineLauncher extends LoadUILauncher
{
	protected static final String LOCAL_OPTION = "l";
	protected static final String FILE_OPTION = "f";
	protected static final String AGENT_OPTION = "a";
	protected static final String LIMITS_OPTION = "L";
	protected static final String TESTCASE_OPTION = "t";
	protected static final String PROJECT_OPTION = "p";
	protected static final String WORKSPACE_OPTION = "w";
	protected static final String REPORT_DIR_OPTION = "r";
	protected static final String REPORT_FORMAT_OPTION = "F";
	protected static final String STATISTICS_REPORT_OPTION = "S";
	protected static final String STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION = "s";
	protected static final String ABORT_ONGOING_REQUESTS_OPTION = "A";

	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		String[] newArgs = new String[args.length + 1];
		System.arraycopy( args, 0, newArgs, 0, args.length );
		newArgs[args.length] = "-" + NOFX_OPTION;

		LoadUICommandLineLauncher launcher = new LoadUICommandLineLauncher( newArgs );
		launcher.init();
		launcher.start();
	}

	private GroovyCommand command;

	public LoadUICommandLineLauncher( String[] args )
	{
		super( args );
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
		options.addOption( LIMITS_OPTION, "limits", true, "Sets the limits for the execution (e.g. -L 60:0:200 )" );
		options.addOption( OptionBuilder
				.withLongOpt( "agents" )
				.withDescription(
						"Sets the agents to use for the test ( usage -" + AGENT_OPTION
								+ " <ip>[:<port>][=<testCase>[,<testCase>] ...] )" ).hasArgs().create( AGENT_OPTION ) );
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
		options.addOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION, "summary", false, "Set to include summary report in statistics report" );
		options
				.addOption(
						ABORT_ONGOING_REQUESTS_OPTION,
						"abort",
						true,
						"Overrides \"Abort ongoing requests on finish\" project property. If set to true ongoing requests will be canceled, if false test will finish when all ongoing requests complete. If not set, property value from project will be used to determine what to do with ongoing requests." );

		return options;
	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		super.processCommandLine( cmd );

		Map<String, Object> attributes = new HashMap<String, Object>();

		if( cmd.hasOption( PROJECT_OPTION ) )
		{
			attributes.put( "workspaceFile",
					cmd.hasOption( WORKSPACE_OPTION ) ? new File( cmd.getOptionValue( WORKSPACE_OPTION ) ) : null );
			attributes.put( "projectFile",
					cmd.hasOption( PROJECT_OPTION ) ? new File( cmd.getOptionValue( PROJECT_OPTION ) ) : null );
			attributes.put( "testCase", cmd.getOptionValue( TESTCASE_OPTION ) );
			attributes.put( "limits", cmd.hasOption( LIMITS_OPTION ) ? cmd.getOptionValue( LIMITS_OPTION ).split( ":" )
					: null );
			attributes.put( "localMode", cmd.hasOption( LOCAL_OPTION ) );
			Map<String, String[]> agents = null;
			if( cmd.hasOption( AGENT_OPTION ) )
			{
				agents = new HashMap<String, String[]>();
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

			attributes.put( "abort", cmd.getOptionValue( ABORT_ONGOING_REQUESTS_OPTION ) );

			attributes.put( "includeSummary", cmd.hasOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION ) );
			
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
	protected void start()
	{
		super.start();

		if( command != null )
			framework.getBundleContext().registerService( GroovyCommand.class.getName(), command, null );
	}
}
