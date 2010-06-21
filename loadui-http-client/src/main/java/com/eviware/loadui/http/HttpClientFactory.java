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
package com.eviware.loadui.http;

import org.eclipse.jetty.client.HttpClient;

public class HttpClientFactory
{
	public static HttpClient createHttpsClient()
	{
		// System.setProperty( "javax.net.debug", "ssl" );

		System.setProperty( "javax.net.ssl.keyStore", System.getProperty( "user.home" ) + "/.loadui/keystore.jks" );
		System.setProperty( "javax.net.ssl.trustStore", System.getProperty( "user.home" ) + "/.loadui/keystore.jks" );
		System.setProperty( "javax.net.ssl.keyStorePassword", "password" );
		System.setProperty( "javax.net.ssl.trustStorePassword", "password" );

		HttpClient httpClient = new HttpClient();

		httpClient.setKeyManagerPassword( System.getProperty( "javax.net.ssl.keyStorePassword" ) );

		httpClient.setKeyStoreLocation( System.getProperty( "javax.net.ssl.keyStore" ) );
		httpClient.setKeyStorePassword( System.getProperty( "javax.net.ssl.keyStorePassword" ) );

		httpClient.setTrustStoreLocation( System.getProperty( "javax.net.ssl.trustStore" ) );
		httpClient.setTrustStorePassword( System.getProperty( "javax.net.ssl.trustStorePassword" ) );

		try
		{
			httpClient.start();
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

		return httpClient;
	}
	
	public static HttpClient createHttpClient()
	{
		HttpClient httpClient = new HttpClient();

		try
		{
			httpClient.start();
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

		return httpClient;
	}

	
}
