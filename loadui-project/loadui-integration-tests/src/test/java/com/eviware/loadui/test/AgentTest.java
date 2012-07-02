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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.Bundle;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.categories.IntegrationTest;

/**
 * Integration tests for testing the loadUI agent through its API.
 * 
 * @author dain.nilsson
 */
@Category( IntegrationTest.class )
public class AgentTest
{
	private static AgentWrapper agent;

	@BeforeClass
	public static void startAgent() throws Exception
	{
		int port = IntegrationTestUtils.getAvailablePort();
		int sslPort = IntegrationTestUtils.getAvailablePort();
		System.out.println( "Starting Agent on ports " + port + " and " + sslPort );
		System.setProperty( LoadUI.HTTPS_PORT, Integer.toString( sslPort ) );
		agent = new AgentWrapper();
	}

	@AfterClass
	public static void stopAgent() throws Exception
	{
		agent.stop();
	}

	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = agent.getBundleContext().getBundles();
		for( Bundle bundle : bundles )
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
	}
}
