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
package com.eviware.loadui.util.test;

import java.util.Map;
import java.util.Set;

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
	private final Map<Class<? extends Addon>, Addon.Factory<? extends Addon>> factories = Maps.newHashMap();
	private final Set<AddonHolder> registeredHolders = Sets.newHashSet();

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