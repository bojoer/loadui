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
package com.eviware.loadui.api.messaging;

/**
 * Like a Socket, but much more high-level. The MessageEndpoint is an endpoint
 * of a channeled, two-way synchronous messaging queue.
 * 
 * @author dain.nilsson
 */
public interface MessageEndpoint
{
	/**
	 * Each channel name needs to start with this String.
	 */
	public static final String BASE_CHANNEL = "/loadui";

	/**
	 * Sends a message to the other endpoint of this connection, over the
	 * specified channel. The message data needs to be serializable by the
	 * implementation.
	 * 
	 * @param channel
	 *           The channel over which to send the message.
	 * @param data
	 *           The data to send.
	 */
	public void sendMessage( String channel, Object data );

	/**
	 * Adds a listener for messages arriving on a specific channel.
	 * 
	 * @param channel
	 *           The channel to listen for messages on.
	 * @param listener
	 *           The MessageListener to notify about incoming messages.
	 */
	public void addMessageListener( String channel, MessageListener listener );

	/**
	 * Stops listening for messages previously listened for.
	 * 
	 * @param listener
	 *           The MessageListener to remove.
	 */
	public void removeMessageListener( MessageListener listener );

	/**
	 * Adds a listener for connection events for the MessageEndpoint.
	 * 
	 * @param listener
	 *           The ConnectionListener to notify about connection events.
	 */
	public void addConnectionListener( ConnectionListener listener );

	/**
	 * Stops listening for connection events previously listened for.
	 * 
	 * @param listener
	 *           The ConnectionListener to remove.
	 */
	public void removeConnectionListener( ConnectionListener listener );

	/**
	 * Opens a closed MessageEndpoint, enabling communication.
	 */
	public void open();

	/**
	 * Closes an open MessageEndpoint, disabling communication.
	 */
	public void close();
}
