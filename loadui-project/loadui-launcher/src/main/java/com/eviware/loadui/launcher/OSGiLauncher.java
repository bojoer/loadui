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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;

/**
 * Starts an embedded OSGi Runtime (Felix) with all the required JavaFX packages
 * exposed, enabling JavaFX bundles to run.
 * 
 * @author dain.nilsson
 */
public class OSGiLauncher
{
	private final Framework framework;

	public static void main( String[] args )
	{
		System.setSecurityManager( null );
		new OSGiLauncher();
	}

	/**
	 * Initiates and starts the OSGi runtime.
	 */
	public OSGiLauncher()
	{
		System.out.println( "Starting OSGi Framework..." );
		Main.loadSystemProperties();
		Properties configProps = Main.loadConfigProperties();
		Main.copySystemProperties( configProps );

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
