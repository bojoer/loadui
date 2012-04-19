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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
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
final public class LoadUILauncher
{
	protected static final String NOFX_OPTION = "nofx";
	protected static final String SYSTEM_PROPERTY_OPTION = "D";
	protected static final String HELP_OPTION = "h";
	protected static final String IGNORE_CURRENTLY_RUNNING_OPTION = "nolock";

	protected final static Logger log = Logger.getLogger( LauncherWatchdog.class.getName() );

	public static void main( String[] args )
	{
		System.setSecurityManager( null );

		LoadUILauncher launcher = new LoadUILauncher( args );
		launcher.init();
		launcher.start();

		new Thread( new LauncherWatchdog( launcher.framework, 20000 ), "loadUI Launcher Watchdog" ).start();
	}

	protected Framework framework;
	protected final Properties configProps;
	protected final String[] argv;
	private final Options options;
	private final CommandLine cmd;

	private boolean nofx = false;

	/**
	 * Initiates and starts the OSGi runtime.
	 */
	public LoadUILauncher( String[] args )
	{
		argv = args;

		File externalFile = new File( "res/buildinfo.txt" );

		//Workaround for some versions of Java 6 which have a known SSL issue
		String versionString = System.getProperty( "java.version", "0.0.0_00" );
		try
		{
			if( versionString.startsWith( "1.6" ) && versionString.contains( "_" ) )
			{
				int updateVersion = Integer.parseInt( versionString.split( "_", 2 )[1] );
				if( updateVersion > 27 )
				{
					log.info( "Detected Java version " + versionString + ", disabling CBC Protection." );
					System.setProperty( "jsse.enableCBCProtection", "false" );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		if( externalFile.exists() )
		{
			InputStream is = null;
			try
			{
				is = new FileInputStream( externalFile );
				Properties buildinfo = new Properties();
				buildinfo.load( is );
				System.setProperty( "loadui.build.number", buildinfo.getProperty( "build.number" ) );
				System.setProperty( "loadui.build.date", buildinfo.getProperty( "build.date" ) );
				System.setProperty( "loadui.name", buildinfo.getProperty( "loadui.name", "loadUI" ) );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( is != null )
					{
						is.close();
					}
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.setProperty( "loadui.build.number", "unknown" );
			System.setProperty( "loadui.build.date", "unknown" );
			System.setProperty( "loadui.name", "loadUI" );
		}

		options = createOptions();
		CommandLineParser parser = new PosixParser();
		try
		{
			cmd = parser.parse( options, argv );
		}
		catch( ParseException e )
		{
			System.err.print( "Error parsing commandline args: " + e.getMessage() + "\n" );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "loadUILauncher", options );

			exitInError();
			throw new RuntimeException();
		}

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

		initSystemProperties();

		System.out.println( "Launching " + System.getProperty( "loadui.name" ) + " Build: "
				+ System.getProperty( "loadui.build.number", "[internal]" ) + " "
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
		String extra = configProps.getProperty( "org.osgi.framework.system.packages.extra", "" );
		configProps.put( "org.osgi.framework.system.packages.extra",
				( extra == null || extra.equals( "" ) ) ? "com.eviware.loadui.launcher.api"
						: "com.eviware.loadui.launcher.api," + extra );

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

		framework = new FrameworkFactory().newFramework( configProps );

		try
		{
			framework.init();
			AutoProcessor.process( configProps, framework.getBundleContext() );

			if( nofx )
			{
				Pattern fxPattern = Pattern
						.compile( "^com\\.eviware\\.loadui\\.(\\w+[.-])*((fx-interface)|(cssbox-browser)).*$" );
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

			File source = new File( "." + File.separator + "ext" );
			if( source.isDirectory() )
			{
				for( File ext : source.listFiles( new FilenameFilter()
				{
					@Override
					public boolean accept( File dir, String name )
					{
						return name.toLowerCase().endsWith( ".jar" );
					}
				} ) )
				{
					try
					{
						File tmpFile = File.createTempFile( ext.getName(), ".jar" );
						BndUtils.wrap( ext, tmpFile );
						framework.getBundleContext().installBundle( tmpFile.toURI().toString() ).start();
					}
					catch( Exception e )
					{
						e.printStackTrace();
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
		Options newOptions = new Options();
		newOptions.addOption( SYSTEM_PROPERTY_OPTION, true, "Sets system property with name=value" );
		newOptions.addOption( NOFX_OPTION, false, "Do not include or require the JavaFX runtime" );
		newOptions.addOption( HELP_OPTION, "help", false, "Prints this message" );
		newOptions.addOption( IGNORE_CURRENTLY_RUNNING_OPTION, false, "Disable lock file" );

		return newOptions;
	}

	protected void processCommandLine( CommandLine cmd )
	{
		if( !cmd.hasOption( NOFX_OPTION ) )
		{
			System.out.println( "Opening splash..." );
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

		//Remove the old expired keystore, if it exists
		if( keystore.exists() )
		{
			FileInputStream kis = null;
			try
			{
				MessageDigest digest = MessageDigest.getInstance( "MD5" );
				kis = new FileInputStream( keystore );
				byte[] buffer = new byte[8192];
				int read = 0;
				while( ( read = kis.read( buffer ) ) > 0 )
				{
					digest.update( buffer, 0, read );
				}
				String hash = new BigInteger( 1, digest.digest() ).toString( 16 );
				if( "10801d8ea0f0562aa3ae22dcea258339".equals( hash ) )
				{
					kis.close();
					kis = null;
					keystore.delete();
				}
			}
			catch( NoSuchAlgorithmException e )
			{
				e.printStackTrace();
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if( kis != null )
					{
						kis.close();
					}
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

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
