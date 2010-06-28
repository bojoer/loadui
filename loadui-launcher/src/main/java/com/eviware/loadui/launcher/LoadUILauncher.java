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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;

/**
 * Starts an embedded OSGi Runtime (Felix) with all the required JavaFX packages
 * exposed, enabling JavaFX bundles to run.
 * 
 * @author dain.nilsson
 */
public class LoadUILauncher
{
	private final Framework framework;
	private final Properties configProps;

	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		new LoadUILauncher( args );
	}

	/**
	 * Initiates and starts the OSGi runtime.
	 */
	public LoadUILauncher( String[] args )
	{
		System.out.println( "Starting OSGi Framework..." );
		Main.loadSystemProperties();
		configProps = Main.loadConfigProperties();
		Main.copySystemProperties( configProps );

		initSystemProperties();

		try
		{
			CommandLineParser parser = new PosixParser();
			Options options = createOptions();
			CommandLine cmd = parser.parse( options, args );

			if( cmd.hasOption( "h" ) )
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "loadUILauncher", options );

				System.exit( 0 );
			}

			processCommandLine( cmd );
		}
		catch( ParseException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		framework = new FrameworkFactory().newFramework( configProps );
		try
		{
			framework.init();
			AutoProcessor.process( configProps, framework.getBundleContext() );
			framework.start();
			System.out.println( "Framework started!" );
		}
		catch( BundleException ex )
		{
			ex.printStackTrace();
		}
	}

	protected Options createOptions()
	{
		Options options = new Options();
		options.addOption( "D", true, "Sets system property with name=value" );
		options.addOption( "nofx", false, "Do not include or require the JavaFX runtime" );
		options.addOption( "h", "help", false, "Prints this message" );

		return options;
	}

	protected void processCommandLine( CommandLine cmd )
	{
		if( cmd.hasOption( "D" ) )
		{
			for( String option : cmd.getOptionValues( "D" ) )
			{
				int ix = option.indexOf( '=' );
				if( ix != -1 )
				{
					System.setProperty( option.substring( 0, ix ), option.substring( ix + 1 ) );
				}
			}
		}
		if( !cmd.hasOption( "nofx" ) )
		{
			addJavaFxPackages();
		}

		if( cmd.hasOption( "E" ) )
		{
			System.out.println( "Echo " + cmd.getOptionValue( "E" ) );
		}
	}

	protected void initSystemProperties()
	{
		System.setProperty( "loadui.home", System.getProperty( "user.home", "." ) + File.separator + ".loadui" );
		System.setProperty( "groovy.root", System.getProperty( "loadui.home" ) + File.separator + ".groovy" );

		System.setProperty( "javax.net.ssl.keyStore", System.getProperty( "loadui.home" ) + "/keystore.jks" );
		System.setProperty( "javax.net.ssl.trustStore", System.getProperty( "loadui.home" ) + "/keystore.jks" );
		System.setProperty( "javax.net.ssl.keyStorePassword", "password" );
		System.setProperty( "javax.net.ssl.trustStorePassword", "password" );
	}

	private void addJavaFxPackages()
	{
		InputStream is = getClass().getResourceAsStream( "/packages-extra.txt" );
		if( is != null )
		{
			try
			{
				StringBuilder out = new StringBuilder();
				byte[] b = new byte[4096];
				for( int n; ( n = is.read( b ) ) != -1; )
					out.append( new String( b, 0, n ) );

				String extra = configProps.getProperty( "org.osgi.framework.system.packages.extra", "" );
				if( !extra.equals( "" ) )
					out.append( "," ).append( extra );

				configProps.put( "org.osgi.framework.system.packages.extra", out.toString() );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stops the OSGi runtime.
	 */
	public void stop()
	{
		System.out.println( "Stopping Framework..." );
		try
		{
			framework.stop();
			System.out.println( "Framework stopped!" );
		}
		catch( BundleException ex )
		{
			ex.printStackTrace();
		}
	}
}
