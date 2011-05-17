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
package com.eviware.loadui.api.addressable;

import com.eviware.loadui.api.events.EventFirer;

/**
 * A registry for keeping track of Addressables.
 * 
 * @author dain.nilsson
 */
public interface AddressableRegistry extends EventFirer
{
	public static final String ADDRESSABLES = AddressableRegistry.class.getName() + "@addressables";

	/**
	 * Generates a unique String which can be used by an Addressable as an ID.
	 * 
	 * @return A unique String.
	 */
	public String generateId();

	/**
	 * Get an Addressable instance by its ID. The Addressable must be registered
	 * with the AddressableRegistry.
	 * 
	 * @param id
	 *           The ID of the Addressable to get.
	 * @return The Addressable with the given ID, or null if no Addressable is
	 *         found.
	 */
	public Addressable lookup( String id );

	/**
	 * Registers an Addressable so that the lookup method can be used to retrieve
	 * the instance.
	 * 
	 * @param addressable
	 *           The Addressable to register.
	 */
	public void register( Addressable addressable ) throws DuplicateAddressException;

	/**
	 * Unregisters an Addressable so that it no longer can be found using the
	 * lookup method.
	 * 
	 * @param addressable
	 *           The Addressable to unregister.
	 */
	public void unregister( Addressable addressable );

	/**
	 * Thrown if an Addressable is registered with an address that already exists
	 * in the registry with a different Object.
	 * 
	 * @author dain.nilsson
	 */
	public class DuplicateAddressException extends Exception
	{
		private static final long serialVersionUID = 4994154619670240358L;

		public DuplicateAddressException( String id )
		{
			super( "Duplicate address detected: " + id );
		}

		public DuplicateAddressException( Addressable a1, Addressable a2 )
		{
			super( "Duplicate address detected between objects: " + a1 + " and " + a2 + " with the common address: "
					+ a1.getId() );
		}
	}
}
