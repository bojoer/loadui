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
package com.eviware.loadui.impl.addressable;

import java.util.EventObject;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.CacheMap;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;

public class AddressableRegistryImpl implements AddressableRegistry, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( AddressableRegistryImpl.class );

	private final Map<String, Addressable> lookupTable = new CacheMap<>();
	private final EventSupport eventSupport = new EventSupport( this );

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
			throw new DuplicateAddressException( addressable, lookup( addressable.getId() ) );

		lookupTable.put( addressable.getId(), addressable );
		fireEvent( new CollectionEvent( this, ADDRESSABLES, CollectionEvent.Event.ADDED, addressable ) );
	}

	@Override
	public void unregister( Addressable addressable )
	{
		if( lookupTable.remove( addressable.getId() ) != null )
			fireEvent( new CollectionEvent( this, ADDRESSABLES, CollectionEvent.Event.REMOVED, addressable ) );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void release()
	{
		ReleasableUtils.release( eventSupport );
	}
}
