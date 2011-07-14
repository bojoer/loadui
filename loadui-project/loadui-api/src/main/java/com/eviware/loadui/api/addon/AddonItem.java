/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.api.addon;

import javax.annotation.Nonnull;

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
		 * Returns the type (corresponding to an Addon class name) of the
		 * AddonItem.
		 * 
		 * @return
		 */
		@Nonnull
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
		@Nonnull
		public PropertyMap getPropertyMap( PropertyHolder owner );

		/**
		 * Returns an AddonHolder.Support object for the given AddonHolder. An
		 * AddonItem should use this AddonHolder.Support to implement AddonHolder,
		 * if needed.
		 * 
		 * @param owner
		 * @return
		 */
		@Nonnull
		public AddonHolder.Support getAddonHolderSupport( AddonHolder owner );
	}
}
