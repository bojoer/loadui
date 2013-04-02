/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.messaging;

/**
 * A listener which can react to a MessageEndpoint connecting or disconnecting.
 * 
 * @author dain.nilsson
 */
public interface ConnectionListener
{
	/**
	 * Called when the connection state of the MessageEndpoint changes.
	 * 
	 * @param endpoint
	 *           The MessageEndpoint whose state changed.
	 * @param connected
	 *           The new connection state of the endpoint.
	 */
	public void handleConnectionChange( MessageEndpoint endpoint, boolean connected );
}
