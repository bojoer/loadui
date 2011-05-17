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
package com.eviware.loadui.test;

import java.util.Properties;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * Holds an embedded Felix OSGi runtime, which can be started and stopped. It
 * can be configured using the Properties object and/or a standard Felix
 * conf/config.properties file.
 * 
 * @author dain.nilsson
 */
public class OSGiWrapper
{
	private final static int MINIMUM_TIME = 3000;
	private final static int LOAD_TIME = 1000;

	private Framework framework;
	private Properties config;
	private final long startTime = System.currentTimeMillis();

	public OSGiWrapper()
	{
		Main.loadSystemProperties();
		config = Main.loadConfigProperties();
		Main.copySystemProperties( config );
	}

	public Properties getConfig()
	{
		return config;
	}

	public void setConfig( Properties config )
	{
		this.config = config;
	}

	public BundleContext start() throws BundleException
	{
		System.out.println( "OSGiWrapper.start();" );

		framework = new FrameworkFactory().newFramework( config );
		framework.init();
		AutoProcessor.process( config, framework.getBundleContext() );
		framework.start();
		try
		{
			Thread.sleep( LOAD_TIME );
		}
		catch( InterruptedException e )
		{
		}

		return framework.getBundleContext();
	}

	public void stop() throws BundleException
	{
		long runtime = System.currentTimeMillis() - startTime;
		try
		{
			if( MINIMUM_TIME > runtime )
				Thread.sleep( MINIMUM_TIME - runtime );
		}
		catch( InterruptedException e1 )
		{
		}

		framework.stop();
		try
		{
			framework.waitForStop( 3000 );
		}
		catch( InterruptedException e )
		{
			System.err.println( "Unable to cleanly stop OSGi framework" );
			// e.printStackTrace();
		}
	}
}
