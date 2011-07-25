package com.eviware.loadui.impl.addon;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.addon.AddonItem;
import com.eviware.loadui.config.AddonItemConfig;
import com.eviware.loadui.config.AddonListConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class AddonItemHolderSupport
{
	private final AddonListConfig listConfig;
	private final HashMultimap<String, AddonItem.Support> addonItems = HashMultimap.create();
	private final HashSet<String> loadedTypes = Sets.newHashSet();

	public AddonItemHolderSupport( AddonListConfig listConfig )
	{
		this.listConfig = listConfig;
	}

	public AddonItem.Support createAddonItemSupport( @Nonnull String type )
	{
		loadType( type );

		final AddonItemConfig addonConfig = listConfig.addNewAddon();
		addonConfig.setType( type );
		final AddonItemSupportImpl addonItem = new AddonItemSupportImpl( this, addonConfig, listConfig );
		addonItems.put( type, addonItem );

		return addonItem;
	}

	public void loadType( @Nonnull String type )
	{
		if( loadedTypes.add( type ) )
		{
			for( AddonItemConfig config : listConfig.getAddonArray() )
			{
				if( type.equals( config.getType() ) )
				{
					addonItems.put( type, new AddonItemSupportImpl( this, config, listConfig ) );
				}
			}
		}
	}

	public Collection<AddonItem.Support> getAddonItemSupports( @Nonnull String type )
	{
		loadType( type );

		return ImmutableSet.copyOf( addonItems.get( type ) );
	}

	public void removeAddonItem( @Nonnull AddonItem.Support child )
	{
		addonItems.get( child.getType() ).remove( child );
	}
}
