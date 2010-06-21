package com.eviware.loadui.impl.http;

import org.eclipse.jetty.client.HttpClient;

import com.eviware.loadui.util.http.HttpClientProvider;

public class HttpClientProviderImpl implements HttpClientProvider {

	private HttpClient httpsClient;
	private HttpClient httpClient;
	
	public HttpClientProviderImpl(HttpClient httpsClient, HttpClient httpClient){
		this.httpsClient = httpsClient;
		this.httpClient = httpClient;
	}

	@Override
	public HttpClient getHttpsClient() {
		return httpsClient;
	}

	@Override
	public HttpClient getHttpClient() {
		return httpClient;
	}

}
