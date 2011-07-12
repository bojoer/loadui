package com.eviware.loadui.api.addon;

import java.util.HashMap;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.addon.Addon.Factory;

/**
 * A static registry of available Addons, with factories.
 * 
 * @author dain.nilsson
 */
public class AddonRegistry
{
	private static final HashMap<Class<? extends Addon>, Addon.Factory<? extends Addon>> factories = new HashMap<Class<? extends Addon>, Addon.Factory<? extends Addon>>();

	/**
	 * Registers an Addon.Factory for creating Addons of a specific type. Only
	 * one factory should be registered per Addon type.
	 * 
	 * @param type
	 * @param factory
	 */
	public static <T extends Addon> void registerFactory( @Nonnull Class<T> type, @Nonnull Addon.Factory<T> factory )
	{
		factories.put( type, factory );
	}

	/**
	 * Returns the Addon.Factory registered for a specific Addon type.
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static <T extends Addon> Addon.Factory<T> getFactory( @Nonnull Class<T> type )
	{
		return ( Factory<T> )factories.get( type );
	}
}
