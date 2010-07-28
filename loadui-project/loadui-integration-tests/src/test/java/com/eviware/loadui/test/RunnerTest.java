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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.junit.*;
import org.osgi.framework.Bundle;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for testing the loadUI runner through its API.
 * 
 * @author dain.nilsson
 */
public class RunnerTest
{
	private static RunnerWrapper runner;

	@BeforeClass
	public static void startRunner() throws Exception
	{
		int port = Utilities.getAvailablePort();
		System.out.println( "Starting Runner on port " + port );
		System.setProperty( "loadui.http.port", Integer.toString( port ) );
		runner = new RunnerWrapper();
	}

	@AfterClass
	public static void stopRunner() throws Exception
	{
		runner.stop();
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = runner.getBundleContext().getBundles();
		// assertThat( bundles.length, greaterThanOrEqualTo( 51 ) );
		for( Bundle bundle : bundles )
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(), anyOf(
					is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
	}

	@Test
	public void shouldHaveRunnerStatusPage() throws Exception
	{
		HttpClient client = new HttpClient();
		HeadMethod method = new HeadMethod( "http://127.0.0.1:" + System.getProperty( "loadui.http.port" ) + "/" );
		client.executeMethod( method );

		Header serverHeader = method.getResponseHeader( "Server" );
		Header dateHeader = method.getResponseHeader( "Date" );

		assertThat( serverHeader, notNullValue() );
		assertThat( dateHeader, notNullValue() );

		String[] parts = serverHeader.getValue().split( ";" );
		assertThat( parts.length, is( 2 ) );
		assertThat( parts[0], is( "LoadUI Agent" ) );
	}
}
