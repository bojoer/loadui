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

import java.util.Collection;

/**
 * A special MessageEndpoint that wraps a number of other MessageEndpoints,
 * allowing them to be treated as a single endpoint.
 * 
 * @author dain.nilsson
 */
public interface BroadcastMessageEndpoint extends MessageEndpoint
{
	/**
	 * Gets the wrapped MessageEndpoints.
	 * 
	 * @return A Collection of all wrapped MessageEndpoints.
	 */
	public Collection<MessageEndpoint> getEndpoints();

	/**
	 * Adds a new endpoint to be wrapped.
	 * 
	 * @param endpoint
	 *           The MessageEndpoint to add.
	 */
	public void registerEndpoint( MessageEndpoint endpoint );

	/**
	 * Removes a registered endpoint from the BroadcastMessageEndpoint
	 * 
	 * @param endpoint
	 *           The endpoint to remove.
	 */
	public void deregisterEndpoint( MessageEndpoint endpoint );
}
