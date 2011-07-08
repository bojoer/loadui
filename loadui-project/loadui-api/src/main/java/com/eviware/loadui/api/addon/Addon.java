package com.eviware.loadui.api.addon;

import java.util.Collection;

/**
 * Provides additional functionality to an AddonHolder, without having to extend
 * the AddonHolder itself.
 * 
 * @author dain.nilsson
 */
public interface Addon
{
	/**
	 * Context object allowing an Addon to interact with its host AddonHolder.
	 * 
	 * @author dain.nilsson
	 */
	public interface Context
	{
		/**
		 * Returns the AddonHolder owning the Addon.
		 * 
		 * @return
		 */
		public AddonHolder getOwner();

		/**
		 * Creates an AddonItemSupport object which an AddonItem managed by the
		 * Addon may use to persist data.
		 * 
		 * @return
		 */
		public AddonItem.Support createAddonItemSupport();

		/**
		 * Returns all existing AddonItemSupport objects for the Addon.
		 * 
		 * @return
		 */
		public Collection<AddonItem.Support> getAddonItemSupports();
	}

	/**
	 * Factory interface for instantiating Addons of a specific type. Must be
	 * registered in the AddonRegistry to function.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public interface Factory<T extends Addon>
	{
		/**
		 * Creates a new instance of the specific Addon using the given Context.
		 * 
		 * @param context
		 * @return
		 */
		public T create( Context context );
	}
}
