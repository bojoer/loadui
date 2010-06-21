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
package com.eviware.loadui.impl.addressable;

import java.util.Map;
import java.util.UUID;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.util.CacheMap;

public class AddressableRegistryImpl implements AddressableRegistry
{
	private final Map<String, Addressable> lookupTable = new CacheMap<String, Addressable>();

	@Override
	public String generateId()
	{
		return UUID.randomUUID().toString();
	}

	@Override
	public Addressable lookup( String id )
	{
		return lookupTable.get( id );
	}

	@Override
	public void register( Addressable addressable ) throws DuplicateAddressException
	{
		if( lookupTable.containsKey( addressable.getId() ) && lookup( addressable.getId() ) != addressable )
			throw new DuplicateAddressException( addressable.getId() );

		lookupTable.put( addressable.getId(), addressable );
	}

	@Override
	public void unregister( Addressable addressable )
	{
		lookupTable.remove( addressable.getId() );
	}
}
