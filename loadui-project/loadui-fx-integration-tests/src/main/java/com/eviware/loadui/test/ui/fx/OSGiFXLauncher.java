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
package com.eviware.loadui.test.ui.fx;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javafx.application.Application;
import javafx.stage.Stage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.LoadUIFXLauncher;
import com.eviware.loadui.launcher.LoadUILauncher;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.google.common.util.concurrent.SettableFuture;

public class OSGiFXLauncher extends LoadUIFXLauncher
{
	private static volatile OSGiFXLauncher instance;
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static File baseDir;

	public static OSGiFXLauncher getInstance()
	{
		return instance;
	}

	public static Future<Stage> getStageFuture()
	{
		return stageFuture;
	}

	public static void main( File baseDir, String[] args )
	{
		OSGiFXLauncher.baseDir = baseDir;
		Application.launch( OSGiFXApplication.class, args );
	}

	private OSGiFXLauncher( String[] args )
	{
		super( args );
	}

	public Properties getConfig()
	{
		return configProps;
	}

	@Override
	public void init()
	{
		Properties config = getConfig();
		config.setProperty( "felix.cache.rootdir", baseDir.getAbsolutePath() );

		File bundleDir = new File( baseDir, "bundle" );

		try
		{
			IntegrationTestUtils.copyDirectory( new File(
					"../../loadui-installers/loadui-controller-installer/target/main" ), baseDir );
			//IntegrationTestUtils.copyDirectory( new File( "../loadui-controller-deps/target/bundle" ), bundleDir );
			IntegrationTestUtils.copyDirectory( new File( "target/bundle" ), bundleDir );
		}
		catch( IOException e1 )
		{
			throw new RuntimeException( e1 );
		}

		// osgi = new OSGiWrapper();
		// Properties config = osgi.getConfig();
		// config.setProperty( "felix.cache.rootdir", baseDir.getAbsolutePath() );
		// File bundleDir = new File( baseDir, "bundle" );
		// Utilities.copyDirectory( new File(
		// "../loadui-controller-deps/target/bundle" ), bundleDir );

		//Add the required packages that should be in the OSGi config file.
		StringBuilder apiPackages = new StringBuilder(
				"com.sun.crypto.provider,com.sun.net.ssl,com.sun.net.ssl.internal.ssl,org.w3c.dom.traversal,javax.transaction.xa;version=1.1.0,sun.io,org.antlr.runtime,org.antlr.runtime.tree" );

		// Remove the API bundle.
		for( File bundle : bundleDir.listFiles() )
		{
			if( bundle.getName().startsWith( "loadui-api" ) )
			{
				extractApi( apiPackages, bundle );
			}
		}

		config.put( "org.osgi.framework.system.packages.extra", apiPackages.toString() );
		config.setProperty( "felix.auto.deploy.dir", bundleDir.getAbsolutePath() );

		super.init();
	}

	@Override
	public void start()
	{
		super.start();

		OSGiFXLauncher.instance = this;
	}

	public BundleContext getBundleContext()
	{
		return framework.getBundleContext();
	}

	public void stop() throws BundleException
	{
		framework.stop();
	}

	private static void extractApi( StringBuilder apiPackages, File bundle )
	{
		try (ZipFile api = new ZipFile( bundle ))
		{
			Set<String> packages = new TreeSet<>();
			for( Enumeration<? extends ZipEntry> e = api.entries(); e.hasMoreElements(); )
			{
				ZipEntry entry = e.nextElement();
				if( entry.getName().endsWith( ".class" ) )
				{
					packages.add( entry.getName().substring( 0, entry.getName().lastIndexOf( "/" ) ).replaceAll( "/", "." ) );
				}
			}

			int dashIndex = LoadUI.VERSION.indexOf( "-" );
			String version = dashIndex < 0 ? LoadUI.VERSION : LoadUI.VERSION.substring( 0, dashIndex );
			for( String pkg : packages )
				apiPackages.append( ", " ).append( pkg ).append( "; version=\"" ).append( version ).append( '"' );
		}
		catch( ZipException e )
		{
			throw new RuntimeException( e );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}

		if( !bundle.delete() )
			throw new RuntimeException( "Unable to delete file: " + bundle );
	}

	public static class OSGiFXApplication extends FXApplication
	{
		@Override
		protected LoadUILauncher createLauncher( String[] args )
		{
			return new OSGiFXLauncher( args );
		}

		@Override
		public void start( Stage stage ) throws Exception
		{
			super.start( stage );

			stageFuture.set( stage );
		}
	}
}
