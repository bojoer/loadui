package com.eviware.loadui.impl.addon;

import java.util.Collection;
import java.util.HashMap;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.api.addon.AddonItem.Support;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.config.AddonItemConfig;
import com.eviware.loadui.config.AddonListConfig;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
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
	private final AddonListConfig config;
	private final HashMultimap<String, AddonItem.Support> addonItems = HashMultimap.create();

	public AddonHolderSupportImpl( AddonHolder owner, AddonListConfig config )
	{
		this.owner = owner;
		this.config = config;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T extends Addon> T getAddon( Class<T> cls )
	{
		if( !addons.containsKey( cls ) )
		{
			Addon.Factory<T> factory = AddonRegistry.getFactory( cls );
			Preconditions.checkNotNull( factory, "No Addon.Factory available for {}", cls );
			final String type = cls.getName();
			for( AddonItemConfig addonItem : config.getAddonArray() )
			{
				if( type.equals( addonItem.getType() ) )
				{
					addonItems.put( type, new AddonItemSupportImpl( this, addonItem, config ) );
				}
			}

			addons.put( cls, factory.create( new ContextImpl( cls.getName() ) ) );
		}

		return ( T )addons.get( cls );
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( addons.values() );
		addons.clear();
	}

	void removeAddonItem( AddonItemSupportImpl addonItemSupport )
	{
		addonItems.get( addonItemSupport.getType() ).remove( addonItemSupport );
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
			return ImmutableSet.copyOf( addonItems.get( type ) );
		}

		@Override
		public Support createAddonItemSupport()
		{
			final AddonItemConfig addonConfig = config.addNewAddon();
			addonConfig.setType( type );
			final AddonItemSupportImpl addonItem = new AddonItemSupportImpl( AddonHolderSupportImpl.this, addonConfig,
					config );
			addonItems.put( type, addonItem );

			return addonItem;
		}
	}
}