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
package com.eviware.loadui.impl.addon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class AddonRegistryImpl implements AddonRegistry, BundleContextAware
{
	public static final Logger log = LoggerFactory.getLogger( AddonRegistryImpl.class );

	private final HashMap<String, ServiceRegistration> registrations = Maps.newHashMap();
	private final HashMap<String, Addon.Factory<?>> factories = Maps.newHashMap();
	private final Multimap<Class<?>, Addon.Factory<?>> eagerAddons = HashMultimap.create();
	private final Set<AddonHolder> registeredHolders = Collections
			.newSetFromMap( new WeakHashMap<AddonHolder, Boolean>() );

	private BundleContext bundleContext;

	@Override
	public synchronized void setBundleContext( BundleContext bundleContext )
	{
		this.bundleContext = bundleContext;
	}

	@Override
	public synchronized <T extends Addon> void registerFactory( Class<T> type, Addon.Factory<T> factory )
	{
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put( "type", type.getName() );

		registrations.put( type.getName(),
				bundleContext.registerService( Addon.Factory.class.getName(), factory, properties ) );
	}

	@Override
	public synchronized <T extends Addon> void unregisterFactory( Class<T> type, Addon.Factory<T> factory )
	{
		ServiceRegistration registration = registrations.get( type.getName() );
		if( registration != null )
			registration.unregister();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public synchronized <T extends Addon> Addon.Factory<T> getFactory( Class<T> type )
	{
		return ( Addon.Factory<T> )factories.get( type.getName() );
	}

	/**
	 * Called by Spring whenever a new Factory is registered as an OSGi service.
	 * 
	 * @param factory
	 * @param properties
	 */
	public synchronized void factoryAdded( final Addon.Factory<?> factory, Map<String, String> properties )
	{
		final String typeStr = factory.getType().getName();

		factories.put( typeStr, factory );
		for( Class<?> type : factory.getEagerTypes() )
		{
			eagerAddons.put( type, factory );
		}
		log.debug( "Registered Addon.Factory for type: {}", typeStr );

		Iterable<AddonHolder> matchingHolders = Iterables.filter( registeredHolders, new Predicate<AddonHolder>()
		{
			@Override
			public boolean apply( AddonHolder input )
			{
				return Iterables.all( factory.getEagerTypes(), new Predicate<Class<?>>()
				{
					@Override
					public boolean apply( Class<?> input )
					{
						return input.isInstance( input );
					}
				} );
			}
		} );

		for( AddonHolder holder : matchingHolders )
		{
			loadAddon( holder, factory );
		}
	}

	/**
	 * Called by Spring whenever a Factory is unregistered as an OSGi service.
	 * 
	 * @param factory
	 * @param properties
	 */
	public synchronized void factoryRemoved( Addon.Factory<?> factory, Map<String, String> properties )
	{
		final String typeStr = factory.getType().getName();
		if( factories.remove( typeStr ) != null )
		{
			for( Class<?> type : factory.getEagerTypes() )
			{
				eagerAddons.remove( type, factory );
			}
			log.debug( "Unregistered Addon.Factory for type: {}", typeStr );
		}
	}

	@Override
	public void registerAddonHolder( final AddonHolder addonHolder )
	{
		registeredHolders.add( addonHolder );
		final Predicate<Class<?>> typeMatcher = new Predicate<Class<?>>()
		{
			@Override
			public boolean apply( Class<?> input )
			{
				return input.isInstance( addonHolder );
			}
		};

		for( Addon.Factory<?> factory : Iterables.concat( Maps.filterKeys( eagerAddons.asMap(), typeMatcher ).values() ) )
		{
			loadAddon( addonHolder, factory );
		}
	}

	@Override
	public void unregisterAddonHolder( AddonHolder addonHolder )
	{
		registeredHolders.remove( addonHolder );
	}

	private void loadAddon( AddonHolder addonHolder, Addon.Factory<?> factory )
	{
		addonHolder.getAddon( factory.getType() );
	}
}
