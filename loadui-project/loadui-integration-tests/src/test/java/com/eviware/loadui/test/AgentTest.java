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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.util.OsgiStringUtils;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;

/**
 * Base class for all integration tests.
 * 
 * @author dain.nilsson
 */
public abstract class AgentTest
{
	private static final Logger log = LoggerFactory.getLogger( AgentTest.class );

	private static ControllerWrapper agent;

	@BeforeClass
	public static void startAgent() throws Exception
	{
		int port = IntegrationTestUtils.getAvailablePort();
		int sslPort = IntegrationTestUtils.getAvailablePort();
		log.info( "Starting Agent on ports " + port + " and " + sslPort );
		System.setProperty( LoadUI.HTTPS_PORT, Integer.toString( sslPort ) );
		agent = new ControllerWrapper();

		assertNoFailedBundles();

		//BeanInjector.setBundleContext( agent.getBundleContext() );
	}

	@AfterClass
	public static void stopAgent() throws Exception
	{
		agent.stop();
	}

	public static void assertNoFailedBundles()
	{
		log.info( "Checking if all bundles started properly" );
		Bundle[] bundles = agent.getBundleContext().getBundles();
		for( Bundle bundle : bundles )
		{
			log.info( StringUtils.padLeft(
					"Bundle: " + bundle.getSymbolicName() + ": " + OsgiStringUtils.bundleStateAsString( bundle ), 100 ) );
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
		}
		log.info( "ALL BUNDLES ACTIVE OR RESOLVED" );
	}
}
