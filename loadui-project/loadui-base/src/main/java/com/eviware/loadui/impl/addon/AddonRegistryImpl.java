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
package com.eviware.loadui.impl.addon;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.addon.Addon.Factory;
import com.google.common.collect.Maps;

public class AddonRegistryImpl implements AddonRegistry, BundleContextAware
{
	public static final Logger log = LoggerFactory.getLogger( AddonRegistryImpl.class );

	private final HashMap<String, ServiceRegistration> registrations = Maps.newHashMap();
	private final HashMap<String, Addon.Factory<? extends Addon>> factories = Maps.newHashMap();

	private BundleContext bundleContext;

	@Override
	public synchronized void setBundleContext( BundleContext bundleContext )
	{
		this.bundleContext = bundleContext;
	}

	@Override
	public synchronized <T extends Addon> void registerFactory( Class<T> type, Factory<T> factory )
	{
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put( "type", type.getName() );

		registrations.put( type.getName(),
				bundleContext.registerService( Addon.Factory.class.getName(), factory, properties ) );
	}

	@Override
	public synchronized <T extends Addon> void unregisterFactory( Class<T> type, Factory<T> factory )
	{
		ServiceRegistration registration = registrations.get( type.getName() );
		if( registration != null )
			registration.unregister();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public synchronized <T extends Addon> Factory<T> getFactory( Class<T> type )
	{
		return ( Factory<T> )factories.get( type.getName() );
	}

	/**
	 * Called by Spring whenever a new Factory is registered as an OSGi service.
	 * 
	 * @param factory
	 * @param properties
	 */
	public synchronized void factoryAdded( Factory<?> factory, Map<String, String> properties )
	{
		if( properties.containsKey( "type" ) )
		{
			factories.put( properties.get( "type" ), factory );
			log.debug( "Registered Addon.Factory for type: {}", properties.get( "type" ) );
		}
	}

	/**
	 * Called by Spring whenever a Factory is unregistered as an OSGi service.
	 * 
	 * @param factory
	 * @param properties
	 */
	public synchronized void factoryRemoved( Factory<?> factory, Map<String, String> properties )
	{
		if( factories.remove( properties.get( "type" ) ) != null )
		{
			log.debug( "Unregistered Addon.Factory for type: {}", properties.get( "type" ) );
		}
	}
}
