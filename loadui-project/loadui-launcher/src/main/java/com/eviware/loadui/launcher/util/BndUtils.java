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
package com.eviware.loadui.launcher.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aQute.lib.io.IO;
import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;
import aQute.lib.osgi.Processor;
import aQute.lib.osgi.Verifier;

/**
 * BND utility class. Provides functionality for wrapping jars into OSGi
 * bundles.
 * 
 * @author predrag.vucetic
 * 
 */
public class BndUtils
{
	/**
	 * Takes all jar and ZIP files from folder specified by sourceDir parameter,
	 * makes OSGi from them and save them into folder specified by destDir
	 * parameter.
	 * 
	 * @param sourceDir
	 *           Folder to look for java libraries (jar and ZIP).
	 * @param destDir
	 *           Folder where to save created bundles.
	 */
	public static void wrapAll( File sourceDir, File destDir )
	{
		if( !sourceDir.exists() || !sourceDir.isDirectory() )
		{
			return;
		}
		File[] sources = sourceDir.listFiles( new FileFilter()
		{
			@Override
			public boolean accept( File pathname )
			{
				return pathname.isFile()
						&& ( pathname.getName().toLowerCase().endsWith( ".jar" ) || pathname.getName().toLowerCase()
								.endsWith( ".zip" ) ) && !pathname.getName().startsWith( ".__" );
			}
		} );

		final Set<String> createdBundles = new HashSet<>();
		for( File source : sources )
		{
			File dest = new File( destDir, ".__" + source.getName() );
			//			if( wrap( source, dest ) )
			//			{
			//				log.debug( "Added bundle for external library: " + source.getAbsolutePath() );
			//			}
			//			else
			//			{
			//				log.debug( "Unable to create bundle for external library: " + source.getAbsolutePath() );
			//			}
			createdBundles.add( dest.getAbsolutePath() );
		}

		//remove bundles that no longer exist in source folder
		File[] filesToRemove = destDir.listFiles( new FileFilter()
		{
			@Override
			public boolean accept( File pathname )
			{
				return pathname.isFile() && pathname.getName().startsWith( ".__" )
						&& !createdBundles.contains( pathname.getAbsolutePath() );
			}
		} );
		for( File file : filesToRemove )
		{
			if( !file.delete() )
				System.out.println( "Failed deleting file: " + file.getAbsolutePath() );
		}
	}

	/**
	 * Makes OSGi bundle from <code>input</code> file and saves it as
	 * <code>output</code> file.
	 * 
	 * @param input
	 *           Java library to create bundle from.
	 * @param output
	 *           Created bundle file.
	 */
	public static void wrap( File input, File output )
	{
		wrap( input, output, null, null, null, true );
	}

	/**
	 * Makes OSGi bundle from <code>input</code> file and saves it as
	 * <code>output</code> file.
	 * 
	 * @param input
	 *           Java library to create bundle from.
	 * @param output
	 *           Created bundle file.
	 * @param properties
	 *           New bundle properties file.
	 * @param classpath
	 * @param additional
	 * @param pedantic
	 * @return true on success, false otherwise.
	 */
	public static void wrap( File input, File output, File properties, File classpath[], Map<String, String> additional,
			boolean pedantic )
	{
		if( !input.exists() )
		{
			//log.error( "Error creating bundle. No such file: " + input.getAbsolutePath() );
			throw new IllegalArgumentException( "Input file does not exist: " + input.getAbsolutePath() );
		}

		try (Analyzer analyzer = new Analyzer())
		{
			analyzer.setPedantic( pedantic );
			analyzer.setJar( input );
			Jar dot = analyzer.getJar();

			if( properties != null )
			{
				analyzer.setProperties( properties );
			}
			if( additional != null )
			{
				analyzer.putAll( additional, false );
			}

			if( analyzer.getProperty( Analyzer.IMPORT_PACKAGE ) == null )
			{
				analyzer.setProperty( Analyzer.IMPORT_PACKAGE, "*;resolution:=optional" );
			}

			if( analyzer.getProperty( Analyzer.BUNDLE_SYMBOLICNAME ) == null )
			{
				analyzer.setProperty( Analyzer.BUNDLE_SYMBOLICNAME, generateSymbolicName( input ) );
			}

			if( analyzer.getProperty( Analyzer.EXPORT_PACKAGE ) == null )
			{
				analyzer.setProperty( Analyzer.EXPORT_PACKAGE, analyzer.calculateExportsFromContents( dot ) );
			}

			if( classpath != null )
			{
				analyzer.setClasspath( classpath );
			}

			analyzer.mergeManifest( dot.getManifest() );

			cleanUpVersionString( analyzer );

			output = setOutputPath( input, output, properties );

			analyzer.calcManifest();
			File f = File.createTempFile( "tmpbnd", ".jar" );
			f.deleteOnExit();
			try (Jar jar = analyzer.getJar())
			{
				jar.write( f );
				jar.close();
				if( !f.renameTo( output ) )
				{
					IO.copy( f, output );
				}
			}
			finally
			{
				if( !f.delete() )
				{
					throw new IOException( "Failed deleting file: " + f.getAbsolutePath() );
				}
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private static File setOutputPath( File input, File output, File properties )
	{
		if( output == null )
		{
			if( properties != null )
			{
				output = properties.getAbsoluteFile().getParentFile();
			}
			else
			{
				output = input.getAbsoluteFile().getParentFile();
			}
		}

		if( output.isDirectory() )
		{
			output = new File( output, generatePath( input ) );
		}
		return output;
	}

	private static String generateSymbolicName( File input )
	{
		Pattern p = Pattern.compile( "(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar" );
		String base = input.getName();
		Matcher m = p.matcher( base );
		base = "Untitled";
		if( m.matches() )
		{
			base = m.group( 1 );
		}
		return base;
	}

	private static void cleanUpVersionString( Analyzer analyzer )
	{
		String version = analyzer.getProperty( Analyzer.BUNDLE_VERSION );
		if( version != null )
		{
			version = Builder.cleanupVersion( version );
			analyzer.setProperty( Analyzer.BUNDLE_VERSION, version );
		}
	}

	private static String generatePath( File input )
	{
		String path = input.getName();
		if( path.endsWith( Processor.DEFAULT_JAR_EXTENSION ) )
		{
			path = path.substring( 0, path.length() - Processor.DEFAULT_JAR_EXTENSION.length() )
					+ Processor.DEFAULT_BAR_EXTENSION;
		}
		else
		{
			path = input.getName() + Processor.DEFAULT_BAR_EXTENSION;
		}
		return path;
	}
}
