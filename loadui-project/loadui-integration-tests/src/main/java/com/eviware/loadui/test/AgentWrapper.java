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
package com.eviware.loadui.test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * An embedded loadUI Runner which can be used for testing.
 * 
 * @author dain.nilsson
 */
public class AgentWrapper
{
	private final File baseDir = new File( "target/runnerTest" );
	private final OSGiLauncher launcher;
	private final BundleContext context;

	public AgentWrapper() throws IOException
	{
		if( baseDir.exists() )
			IntegrationTestUtils.deleteRecursive( baseDir );

		if( !baseDir.mkdir() )
			throw new RuntimeException( "Could not create test directory!" );

		baseDir.deleteOnExit();

		launcher = new OSGiLauncher( new String[] { "-nolock", "-nofx" } );
		Properties config = launcher.getConfig();
		config.setProperty( "felix.cache.rootdir", baseDir.getAbsolutePath() );
		//config.setProperty( "felix.auto.deploy.dir", new File( "../loadui-agent-deps/target/bundle" ).getAbsolutePath() );

		File bundleDir = new File( baseDir, "bundle" );
		//IntegrationTestUtils.copyDirectory( new File( "../loadui-agent-deps/target/bundle" ), bundleDir );
		IntegrationTestUtils.copyDirectory( new File( "target/bundle" ), bundleDir );

		//Add the required packages that should be in the OSGi config file.
		config.setProperty(
				"org.osgi.framework.system.packages.extra",
				"com.sun.crypto.provider,com.sun.net.ssl,com.sun.net.ssl.internal.ssl,org.w3c.dom.traversal,javax.transaction.xa;version=1.1.0" );
		launcher.init();
		launcher.start();
		context = launcher.getBundleContext();

	}

	public void stop() throws BundleException
	{
		try
		{
			launcher.stop();
		}
		finally
		{
			IntegrationTestUtils.deleteRecursive( baseDir );
		}
	}

	public BundleContext getBundleContext()
	{
		return context;
	}
}
