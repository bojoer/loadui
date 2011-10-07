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
package com.eviware.loadui.api.messaging;

/**
 * A listener which can react to incoming messages on a specific channel on a
 * specific MessageEndpoint.
 * 
 * @author dain.nilsson
 */
public interface MessageListener
{
	/**
	 * Called when a new message has arrived on a channel that the listener is
	 * listening for.
	 * 
	 * @param channel
	 *           The channel that the incoming message is sent over.
	 * @param endpoint
	 *           The endpoint which received the message.
	 * @param data
	 *           The message data.
	 */
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data );
}
