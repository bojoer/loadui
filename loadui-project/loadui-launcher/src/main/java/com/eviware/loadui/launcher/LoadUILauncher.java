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
package com.eviware.loadui.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import com.eviware.loadui.launcher.api.OSGiUtils;
import com.eviware.loadui.launcher.api.SplashController;
import com.eviware.loadui.launcher.util.BndUtils;

/**
 * Starts an embedded OSGi Runtime (Felix) with all the required JavaFX packages
 * exposed, enabling JavaFX bundles to run.
 * 
 * @author dain.nilsson
 */
public class LoadUILauncher
{
	protected static final String NOFX_OPTION = "nofx";
	protected static final String SYSTEM_PROPERTY_OPTION = "D";
	protected static final String HELP_OPTION = "h";
	protected static final String IGNORE_CURRENTLY_RUNNING_OPTION = "nolock";

	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		// Alpha release expiration, TODO: REMOVE THE IF-CONDITION AND THE WHOLE ELSE-BLOCK IN FULL VERSION 
		//		if( new java.util.GregorianCalendar().before( new java.util.GregorianCalendar( 2011, java.util.Calendar.DECEMBER,
		//				31 ) ) )
		//		{
		LoadUILauncher launcher = new LoadUILauncher( args );
		launcher.init();
		launcher.start();
		//		}
		//		else
		//		{
		//			System.out.println( "*** ALPHA PERIOD OVER. QUITTING... ***" );
		//			try
		//			{
		//				Thread.sleep( 5000 );
		//			}
		//			catch( InterruptedException e )
		//			{
		//				e.printStackTrace();
		//			}
		//		}

		new Thread( new LoaderWatchdog( launcher.framework, 10000 ), "loadUI Launcher Watchdog" ).start();
	}

	protected Framework framework;
	protected final Properties configProps;
	protected final String[] argv;
	private Options options;
	private boolean nofx = false;

	/**
	 * Initiates and starts the OSGi runtime.
	 */
	public LoadUILauncher( String[] args )
	{
		argv = args;

		final InputStream is = getClass().getResourceAsStream( "/properties/buildinfo.txt" );
		try
		{
			Properties buildinfo = new Properties();
			buildinfo.load( is );
			System.setProperty( "loadui.build.number", buildinfo.getProperty( "build.number" ) );
			System.setProperty( "loadui.build.date", buildinfo.getProperty( "build.date" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		System.out.println( "Launching loadUI Build: " + System.getProperty( "loadui.build.number", "[internal]" ) + " "
				+ System.getProperty( "loadui.build.date", "" ) );
		Main.loadSystemProperties();
		configProps = Main.loadConfigProperties();
		if( configProps == null )
		{
			System.err.println( "There was an error loading the OSGi configuration!" );
			exitInError();
		}
		Main.copySystemProperties( configProps );
	}

	protected void init()
	{
		initSystemProperties();

		String extra = configProps.getProperty( "org.osgi.framework.system.packages.extra", "" );
		configProps.put( "org.osgi.framework.system.packages.extra",
				( extra == null || extra.equals( "" ) ) ? "com.eviware.loadui.launcher.api"
						: "com.eviware.loadui.launcher.api," + extra );

		CommandLineParser parser = new PosixParser();
		options = createOptions();

		try
		{
			CommandLine cmd = parser.parse( options, argv );

			if( cmd.hasOption( HELP_OPTION ) )
				printUsageAndQuit();

			if( !cmd.hasOption( IGNORE_CURRENTLY_RUNNING_OPTION ) )
			{
				try
				{
					File bundleCache = new File( configProps.getProperty( "org.osgi.framework.storage" ) );
					if( !bundleCache.isDirectory() )
						if( !bundleCache.mkdirs() )
							throw new RuntimeException( "Unable to create directory: " + bundleCache.getAbsolutePath() );

					File lockFile = new File( bundleCache, "loadui.lock" );
					if( !lockFile.exists() )
						if( !lockFile.createNewFile() )
							throw new RuntimeException( "Unable to create file: " + lockFile.getAbsolutePath() );

					FileLock lock = new RandomAccessFile( lockFile, "rw" ).getChannel().tryLock();
					if( lock == null )
					{
						System.err.println( "An instance of loadUI is already running!" );
						exitInError();
					}
				}
				catch( OverlappingFileLockException e )
				{
					System.err.println( "An instance of loadUI is already running!" );
					exitInError();
				}
				catch( IOException e )
				{
					e.printStackTrace();
					exitInError();
				}
			}

			processCommandLine( cmd );
		}
		catch( ParseException e )
		{
			System.err.print( "Error parsing commandline args: " + e.getMessage() + "\n" );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "loadUILauncher", options );

			exitInError();
		}

		//make bundles from external libraries
		File source = new File( "." + File.separator + "ext" );
		File dest = new File( "." + File.separator + "bundle" );
		BndUtils.wrapAll( source, dest );

		framework = new FrameworkFactory().newFramework( configProps );

		try
		{
			framework.init();
			AutoProcessor.process( configProps, framework.getBundleContext() );

			if( nofx )
			{
				Pattern fxPattern = Pattern.compile( "^com\\.eviware\\.loadui\\.(\\w+[.-])*fx-interface.*$" );
				for( Bundle bundle : framework.getBundleContext().getBundles() )
				{
					String bundleName = bundle.getSymbolicName();
					if( bundle.getHeaders().get( Constants.FRAGMENT_HOST ) == null
							&& ( bundleName == null || !fxPattern.matcher( bundleName ).find() ) )
					{
						try
						{
							bundle.start();
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch( BundleException ex )
		{
			ex.printStackTrace();
		}
	}

	protected final void exitInError()
	{
		try
		{
			System.err.println( "Exiting..." );
			Thread.sleep( 5000 );
		}
		catch( InterruptedException e )
		{
		}
		System.exit( -1 );
	}

	protected void printUsageAndQuit()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "loadUILauncher", options );

		OSGiUtils.shutdown();
	}

	protected void start()
	{
		try
		{
			framework.start();
			OSGiUtils.setFramework( framework );
			System.out.println( "Framework started!" );
		}
		catch( BundleException e )
		{
			e.printStackTrace();
		}
	}

	protected Options createOptions()
	{
		Options options = new Options();
		options.addOption( SYSTEM_PROPERTY_OPTION, true, "Sets system property with name=value" );
		options.addOption( NOFX_OPTION, false, "Do not include or require the JavaFX runtime" );
		options.addOption( HELP_OPTION, "help", false, "Prints this message" );
		options.addOption( IGNORE_CURRENTLY_RUNNING_OPTION, false, "Disable lock file" );

		return options;
	}

	protected void processCommandLine( CommandLine cmd )
	{
		if( cmd.hasOption( SYSTEM_PROPERTY_OPTION ) )
		{
			for( String option : cmd.getOptionValues( SYSTEM_PROPERTY_OPTION ) )
			{
				int ix = option.indexOf( '=' );
				if( ix != -1 )
					System.setProperty( option.substring( 0, ix ), option.substring( ix + 1 ) );
				else
					System.setProperty( option, "true" );
			}
		}
		if( !cmd.hasOption( NOFX_OPTION ) )
		{
			SplashController.openSplash();
			addJavaFxPackages();
		}
		else
		{
			//Do not auto-load any loadui JavaFX bundles
			configProps.setProperty( "felix.auto.deploy.action", "install" );
			nofx = true;
		}
	}

	protected void initSystemProperties()
	{
		Properties systemProperties = new Properties();
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream( "conf" + File.separator + "system.properties" );
			systemProperties.load( fis );
			for( Entry<Object, Object> entry : systemProperties.entrySet() )
				System.setProperty( ( String )entry.getKey(), ( String )entry.getValue() );
		}
		catch( FileNotFoundException e )
		{
			// Ignore
		}
		catch( IOException e )
		{
			// Ignore
		}
		finally
		{
			if( fis != null )
			{
				try
				{
					fis.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		setDefaultSystemProperty( "loadui.home", System.getProperty( "user.home", "." ) + File.separator + ".loadui" );
		setDefaultSystemProperty( "groovy.root", System.getProperty( "loadui.home" ) + File.separator + ".groovy" );

		setDefaultSystemProperty( "loadui.ssl.keyStore", System.getProperty( "loadui.home" ) + File.separator
				+ "keystore.jks" );
		setDefaultSystemProperty( "loadui.ssl.trustStore", System.getProperty( "loadui.home" ) + File.separator
				+ "certificate.pem" );
		setDefaultSystemProperty( "loadui.ssl.keyStorePassword", "password" );
		setDefaultSystemProperty( "loadui.ssl.trustStorePassword", "password" );

		setDefaultSystemProperty( "loadui.instance", "controller" );

		setDefaultSystemProperty( "sun.java2d.noddraw", "true" );

		File loaduiHome = new File( System.getProperty( "loadui.home" ) );
		if( !loaduiHome.isDirectory() )
			if( !loaduiHome.mkdirs() )
				throw new RuntimeException( "Unable to create directory: " + loaduiHome.getAbsolutePath() );

		File keystore = new File( System.getProperty( "loadui.ssl.keyStore" ) );
		if( !keystore.exists() )
		{
			InputStream is = getClass().getResourceAsStream( "/keystore.jks" );
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream( keystore );
				byte buf[] = new byte[1024];
				int len;
				while( ( len = is.read( buf ) ) > 0 )
					fos.write( buf, 0, len );

			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( is != null )
						is.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				try
				{
					if( fos != null )
						fos.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		File truststore = new File( System.getProperty( "loadui.ssl.trustStore" ) );
		if( !truststore.exists() )
		{
			InputStream is = getClass().getResourceAsStream( "/certificate.pem" );
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream( truststore );
				byte buf[] = new byte[1024];
				int len;
				while( ( len = is.read( buf ) ) > 0 )
					fos.write( buf, 0, len );

			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( is != null )
						is.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				try
				{
					if( fos != null )
						fos.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	protected void setDefaultSystemProperty( String property, String value )
	{
		if( System.getProperty( property ) == null )
		{
			System.setProperty( property, value );
		}
	}

	protected void addJavaFxPackages()
	{
		try
		{
			Class.forName( "javafx.lang.FX" );
		}
		catch( ClassNotFoundException e )
		{
			return;
		}

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
				if( !extra.isEmpty() )
					out.append( "," ).append( extra );

				configProps.setProperty( "org.osgi.framework.system.packages.extra", out.toString() );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					is.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
