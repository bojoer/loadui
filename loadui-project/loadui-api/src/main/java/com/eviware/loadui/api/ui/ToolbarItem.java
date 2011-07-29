package com.eviware.loadui.api.ui;

import java.net.URI;

import com.eviware.loadui.api.traits.Labeled;

/**
 * Item to be shown in a toolbar.
 * 
 * @author dain.nilsson
 */
public interface ToolbarItem extends Labeled
{
	/**
	 * Gets a URI to the icon to be displayed for this ToolbarItem.
	 * 
	 * @return
	 */
	public URI getIconUri();

	/**
	 * Gets the category for the ToolbarItem.
	 * 
	 * @return
	 */
	public String getCategory();
}
