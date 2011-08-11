package com.eviware.loadui.util.test;

import java.util.HashMap;
import java.util.HashSet;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.addon.Addon.Factory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Map backed AddonRegistry which doesn't use OSGi. Used in Unit tests. Does NOT
 * load eager Addons!
 * 
 * @author dain.nilsson
 */
public class DefaultAddonRegistry implements AddonRegistry
{
	private final HashMap<Class<? extends Addon>, Addon.Factory<? extends Addon>> factories = Maps.newHashMap();
	private final HashSet<AddonHolder> registeredHolders = Sets.newHashSet();

	@Override
	public synchronized <T extends Addon> void registerFactory( Class<T> type, Addon.Factory<T> factory )
	{
		factories.put( type, factory );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public synchronized <T extends Addon> Addon.Factory<T> getFactory( Class<T> type )
	{
		return ( Factory<T> )factories.get( type );
	}

	@Override
	public <T extends Addon> void unregisterFactory( Class<T> type, Factory<T> factory )
	{
		factories.remove( type );
	}

	@Override
	public void registerAddonHolder( AddonHolder addonHolder )
	{
		registeredHolders.add( addonHolder );
	}

	@Override
	public void unregisterAddonHolder( AddonHolder addonHolder )
	{
		registeredHolders.remove( addonHolder );
	}
}