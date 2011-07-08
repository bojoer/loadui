package com.eviware.loadui.api.model;

import com.eviware.loadui.api.addressable.Addressable;

/**
 * A deletable item with a unique ID.
 * 
 * @author dain.nilsson
 */
public interface BaseItem extends Addressable
{
	// BaseEvents
	public final String DELETED = BaseItem.class.getSimpleName() + "@deleted";

	public void delete();
}
