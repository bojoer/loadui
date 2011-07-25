package com.eviware.loadui.api.addon;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * An object with AddonItems as children.
 * 
 * @author dain.nilsson
 */
public interface AddonItemHolder
{
	/**
	 * Creates an AddonItemSupport object which an AddonItem managed by the Addon
	 * may use to persist data.
	 * 
	 * @return
	 */
	@Nonnull
	public AddonItem.Support createAddonItemSupport();

	/**
	 * Returns all existing AddonItemSupport objects for the Addon.
	 * 
	 * @return
	 */
	@Nonnull
	public Collection<AddonItem.Support> getAddonItemSupports();
}
