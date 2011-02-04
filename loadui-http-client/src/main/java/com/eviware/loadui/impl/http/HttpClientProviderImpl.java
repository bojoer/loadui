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
package com.eviware.loadui.impl.http;

import org.eclipse.jetty.client.HttpClient;

import com.eviware.loadui.api.http.HttpClientProvider;

public class HttpClientProviderImpl implements HttpClientProvider
{
	private HttpClient httpsClient;
	private HttpClient httpClient;

	public HttpClientProviderImpl( HttpClient httpsClient, HttpClient httpClient )
	{
		this.httpsClient = httpsClient;
		this.httpClient = httpClient;
	}

	@Override
	public HttpClient getHttpsClient()
	{
		return httpsClient;
	}

	@Override
	public HttpClient getHttpClient()
	{
		return httpClient;
	}
}