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
import java.io.PrintStream;
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
public class LoadUILauncher
{
	protected static final String LOADUI_HOME = "loadui.home";
	protected static final String LOADUI_NAME = "loadui.name";
	protected static final String LOADUI_BUILD_DATE = "loadui.build.date";
	protected static final String LOADUI_BUILD_NUMBER = "loadui.build.number";

	protected static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";
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

	private static void loadPropertiesFile()
	{
		Properties systemProperties = new Properties();
		try (FileInputStream fis = new FileInputStream( "conf" + File.separator + "system.properties" ))
		{
			systemProperties.load( fis );
			for( Entry<Object, Object> entry : systemProperties.entrySet() )
				System.setProperty( ( String )entry.getKey(), ( String )entry.getValue() );
		}
		catch( IOException e )
		{
			// Ignore
		}
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

		//Fix for Protection!
		String username = System.getProperty( "user.name" );
		System.setProperty( "user.name.original", username );
		System.setProperty( "user.name", username.toLowerCase() );

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
			try (InputStream is = new FileInputStream( externalFile ))
			{
				Properties buildinfo = new Properties();
				buildinfo.load( is );
				System.setProperty( LOADUI_BUILD_NUMBER, buildinfo.getProperty( "build.number" ) );
				System.setProperty( LOADUI_BUILD_DATE, buildinfo.getProperty( "build.date" ) );
				System.setProperty( LOADUI_NAME, buildinfo.getProperty( LOADUI_NAME, "loadUI" ) );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
					}
		else
		{
			System.setProperty( LOADUI_BUILD_NUMBER, "unknown" );
			System.setProperty( LOADUI_BUILD_DATE, "unknown" );
			System.setProperty( LOADUI_NAME, "loadUI" );
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
			parseSystemProperties();
		}

		initSystemProperties();

		String sysOutFilePath = System.getProperty( "system.out.file" );
		if( sysOutFilePath != null )
		{
			File sysOutFile = new File( sysOutFilePath );
			if( !sysOutFile.exists() )
			{
				try
				{
					sysOutFile.createNewFile();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			try
			{
				System.err.println( "Writing stdout and stderr to file:" + sysOutFile.getAbsolutePath() );

				final PrintStream outStream = new PrintStream( sysOutFile );
				System.setOut( outStream );
				System.setErr( outStream );

				Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
				{
					@Override
					public void run()
					{
						outStream.close();
					}
				} ) );
			}
			catch( FileNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}

		System.out.println( "Launching " + System.getProperty( LOADUI_NAME ) + " Build: "
				+ System.getProperty( LOADUI_BUILD_NUMBER, "[internal]" ) + " "
				+ System.getProperty( LOADUI_BUILD_DATE, "" ) );
		Main.loadSystemProperties();
		configProps = Main.loadConfigProperties();
		if( configProps == null )
		{
			System.err.println( "There was an error loading the OSGi configuration!" );
			exitInError();
		}
		Main.copySystemProperties( configProps );
	}

	private void parseSystemProperties()
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

	protected void init()
	{
		String extra = configProps.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "" );
		configProps.put( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA,
				( extra == null || extra.equals( "" ) ) ? "com.eviware.loadui.launcher.api"
						: "com.eviware.loadui.launcher.api," + extra );

		if( cmd.hasOption( HELP_OPTION ) )
			printUsageAndQuit();

		if( !cmd.hasOption( IGNORE_CURRENTLY_RUNNING_OPTION ) )
		{
			ensureNoOtherInstance();
		}

		processCommandLine( cmd );

		framework = new FrameworkFactory().newFramework( configProps );

		try
		{
			framework.init();
			AutoProcessor.process( configProps, framework.getBundleContext() );

			if( nofx )
			{
				startAllNonFxBundles();
			}

			loadExternalJarsAsBundles();
		}
		catch( BundleException ex )
		{
			ex.printStackTrace();
		}
	}

	private void startAllNonFxBundles()
	{
		Pattern fxPattern = Pattern.compile( "^com\\.eviware\\.loadui\\.(\\w+[.-])*((fx-interface)|(cssbox-browser)).*$" );
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

	private void loadExternalJarsAsBundles()
	{
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

	private void ensureNoOtherInstance()
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

			try (RandomAccessFile randomAccessFile = new RandomAccessFile( lockFile, "rw" ))
			{
				FileLock lock = randomAccessFile.getChannel().tryLock();
			if( lock == null )
			{
				System.err.println( "An instance of loadUI is already running!" );
				exitInError();
			}
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

	protected final static void exitInError()
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

	protected void processCommandLine( CommandLine cmdLine )
	{
		if( !cmdLine.hasOption( NOFX_OPTION ) )
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

	protected final void initSystemProperties()
	{
		loadPropertiesFile();

		setDefaultSystemProperty( LOADUI_HOME, System.getProperty( "user.home", "." ) + File.separator + ".loadui" );
		setDefaultSystemProperty( "groovy.root", System.getProperty( LOADUI_HOME ) + File.separator + ".groovy" );

		setDefaultSystemProperty( "loadui.ssl.keyStore", System.getProperty( LOADUI_HOME ) + File.separator
				+ "keystore.jks" );
		setDefaultSystemProperty( "loadui.ssl.trustStore", System.getProperty( LOADUI_HOME ) + File.separator
				+ "certificate.pem" );
		setDefaultSystemProperty( "loadui.ssl.keyStorePassword", "password" );
		setDefaultSystemProperty( "loadui.ssl.trustStorePassword", "password" );

		setDefaultSystemProperty( "loadui.instance", "controller" );

		setDefaultSystemProperty( "sun.java2d.noddraw", "true" );

		File loaduiHome = new File( System.getProperty( LOADUI_HOME ) );
		if( !loaduiHome.isDirectory() )
			if( !loaduiHome.mkdirs() )
				throw new RuntimeException( "Unable to create directory: " + loaduiHome.getAbsolutePath() );

		File keystore = new File( System.getProperty( "loadui.ssl.keyStore" ) );

		//Remove the old expired keystore, if it exists
		if( keystore.exists() )
		{
			try (FileInputStream kis = new FileInputStream( keystore ))
			{
				MessageDigest digest = MessageDigest.getInstance( "MD5" );
				byte[] buffer = new byte[8192];
				int read = 0;
				while( ( read = kis.read( buffer ) ) > 0 )
				{
					digest.update( buffer, 0, read );
				}
				String hash = new BigInteger( 1, digest.digest() ).toString( 16 );
				if( "10801d8ea0f0562aa3ae22dcea258339".equals( hash ) )
				{
					if( !keystore.delete() )
						System.err.println( "Could not delete old keystore: " + keystore.getAbsolutePath() );
				}
			}
			catch( NoSuchAlgorithmException | IOException e )
			{
				e.printStackTrace();
			}
			}

		if( !keystore.exists() )
		{
			createKeyStore( keystore );
		}

		File truststore = new File( System.getProperty( "loadui.ssl.trustStore" ) );
		if( !truststore.exists() )
		{
			createTrustStore( truststore );
		}
	}

	private void createKeyStore( File keystore )
	{
		try (FileOutputStream fos = new FileOutputStream( keystore );
				InputStream is = getClass().getResourceAsStream( "/keystore.jks" ))
		{
			byte buf[] = new byte[1024];
			int len;
			while( ( len = is.read( buf ) ) > 0 )
				fos.write( buf, 0, len );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
			}

	private void createTrustStore( File truststore )
	{
		try (FileOutputStream fos = new FileOutputStream( truststore );
				InputStream is = getClass().getResourceAsStream( "/certificate.pem" ))
		{
			byte buf[] = new byte[1024];
			int len;
			while( ( len = is.read( buf ) ) > 0 )
				fos.write( buf, 0, len );

		}
		catch( Exception e )
		{
			e.printStackTrace();
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
			}
}
