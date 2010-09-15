package com.eviware.loadui.api.http;

import org.eclipse.jetty.client.HttpClient;

public interface HttpClientProvider
{

	public HttpClient getHttpClient();

	public HttpClient getHttpsClient();

}