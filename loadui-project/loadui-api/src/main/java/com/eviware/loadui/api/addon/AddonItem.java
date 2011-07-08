package com.eviware.loadui.api.addon;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.model.BaseItem;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.PropertyMap;

/**
 * A persisted object which belongs to an AddonHolder, but is managed by an
 * Addon.
 * 
 * @author dain.nilsson
 */
public interface AddonItem extends BaseItem
{
	/**
	 * A support class for an AddonItem allowing the AddonItem to persist data in
	 * the project file, such as attributes and Properties.
	 * 
	 * @author dain.nilsson
	 */
	public interface Support extends Addressable, AttributeHolder
	{
		/**
		 * Returns the type (corresponding to an Addon class) of the AddonItem.
		 * 
		 * @return
		 */
		public String getType();

		/**
		 * Deletes the persisted data from the parent AddonHolder for the
		 * AddonItemSupport.
		 */
		public void delete();

		/**
		 * Returns a PropertyMap providing Properties for the given
		 * PropertyHolder. The Properties themselves are persisted with the
		 * AddonItemSupport. An AddonItem should use this PropertyMap as support
		 * to implement PropertyHolder, if needed.
		 * 
		 * @param owner
		 * @return
		 */
		public PropertyMap getPropertyMap( PropertyHolder owner );

		/**
		 * Returns an AddonHolder.Support object for the given AddonHolder. An
		 * AddonItem should use this AddonHolder.Support to implement AddonHolder,
		 * if needed.
		 * 
		 * @param owner
		 * @return
		 */
		public AddonHolder.Support getAddonHolderSupport( AddonHolder owner );
	}
}
