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

import java.util.Collection;
import java.util.HashMap;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addon.AddonItem.Support;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Support class for AddonHolders.
 * 
 * @author dain.nilsson
 */
public class AddonHolderSupportImpl implements AddonHolder.Support, Releasable
{
	private final HashMap<Class<? extends Addon>, Addon> addons = Maps.newHashMap();
	private final AddonHolder owner;
	private final AddonItemHolderSupport addonItemHolderSupport;
	private final AddonRegistry addonRegistry = BeanInjector.getBean( AddonRegistry.class );

	public AddonHolderSupportImpl( AddonHolder owner, AddonListConfig config )
	{
		this.owner = owner;

		addonItemHolderSupport = new AddonItemHolderSupport( config );
	}

	public void init()
	{
		addonRegistry.registerAddonHolder( owner );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T extends Addon> T getAddon( Class<T> cls )
	{
		ensureAddon( cls );

		return ( T )addons.get( cls );
	}

	/**
	 * Ensures that the given Addon type has been initialized, and if not,
	 * attempts to initialize it using the addonRegistry.
	 * 
	 * @param cls
	 */
	private <T extends Addon> void ensureAddon( Class<T> cls )
	{
		if( !addons.containsKey( cls ) )
		{
			Addon.Factory<T> factory = addonRegistry.getFactory( cls );
			Preconditions.checkNotNull( factory, "No Addon.Factory available for {}", cls );

			final String type = cls.getName();
			addonItemHolderSupport.loadType( type );

			addons.put( cls, factory.create( new ContextImpl( cls.getName() ) ) );
		}
	}

	@Override
	public void release()
	{
		addonRegistry.unregisterAddonHolder( owner );
		ReleasableUtils.releaseAll( addons.values(), addonItemHolderSupport );
		addons.clear();
	}

	/**
	 * Internal package method called by AddonItemSupportImpl to clean up
	 * addonItems when an AddonItemSupportImpl is deleted.
	 * 
	 * @param addonItemSupport
	 */
	void removeAddonItem( AddonItemSupportImpl addonItemSupport )
	{
		addonItemHolderSupport.removeAddonItem( addonItemSupport );
	}

	class ContextImpl implements Addon.Context
	{
		private final String type;

		private ContextImpl( String type )
		{
			this.type = type;
		}

		@Override
		public AddonHolder getOwner()
		{
			return owner;
		}

		@Override
		public Collection<AddonItem.Support> getAddonItemSupports()
		{
			return addonItemHolderSupport.getAddonItemSupports( type );
		}

		@Override
		public Support createAddonItemSupport()
		{
			return addonItemHolderSupport.createAddonItemSupport( type );
		}
	}
}