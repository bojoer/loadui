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
package com.eviware.loadui.test;

import java.io.File;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * An embedded loadUI Runner which can be used for testing.
 * 
 * @author dain.nilsson
 */
public class RunnerWrapper
{
	private final File baseDir = new File( "target/runnerTest" );
	private final OSGiWrapper osgi;
	private final BundleContext context;

	public RunnerWrapper() throws BundleException
	{
		if( baseDir.exists() )
			throw new RuntimeException( "Test directory already exists!" );

		if( !baseDir.mkdir() )
			throw new RuntimeException( "Could not create test directory!" );

		osgi = new OSGiWrapper();
		Properties config = osgi.getConfig();
		config.setProperty( "felix.cache.rootdir", baseDir.getAbsolutePath() );
		config.setProperty( "felix.auto.deploy.dir", new File( "../loadui-runner-deps/target/bundle" ).getAbsolutePath() );

		context = osgi.start();
	}

	public void stop() throws BundleException
	{
		try
		{
			osgi.stop();
		}
		catch( BundleException e )
		{
			throw e;
		}
		finally
		{
			Utilities.deleteRecursive( baseDir );
		}
	}

	public BundleContext getBundleContext()
	{
		return context;
	}
}
