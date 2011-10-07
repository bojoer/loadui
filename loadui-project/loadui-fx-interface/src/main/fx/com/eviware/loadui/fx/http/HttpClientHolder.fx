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
package com.eviware.loadui.fx.http;

import com.eviware.loadui.api.http.HttpClientProvider;
import org.eclipse.jetty.client.HttpClient;

public-read var instance: HttpClientHolder;

public class HttpClientHolder {

	var provider: HttpClientProvider;
	public function setProvider(provider: HttpClientProvider): Void { 
		this.provider = provider; 
	}
	
	public function getHttpsClient(): HttpClient {
		provider.getHttpsClient(); 
	}
	
	public function getHttpClient(): HttpClient {
		provider.getHttpClient(); 
	}

	function initialize(): Void {
		instance = this;
	}
	
	function destroy(): Void {
		instance = this;
	}

}