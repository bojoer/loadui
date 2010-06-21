/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.api.ui.toolbar;

import java.util.Collection;

/**
 * The toolbar group is a set of one or more items which will be siplayed on the toolbar.
 * Only the default item will be visible, until the user clicks the expand button, at which
 * point all of them will be displayed.
 * 
 * @author nenad.ristic
 * 
 */
public interface ToolbarGroup
{
	/**
	 * @return The Id must be unique for each group
	 */
	String getId();
	
	/**
	 * @return The display name of the group
	 */
	String getName();
	
	/**
	 * @return A list of all the items in the group
	 */
	Collection<ToolbarItem> getItemList();
	
	/**
	 * @param itemId 
	 * 
	 * @return The item with the specified id
	 */
	ToolbarItem getItem(String itemId);
	
	/**
	 * Should raise an exception if the item is not present
	 * 
	 * @param itemId The uniques id of the item to be removed
	 */
	void removeItem(String itemId);
	
	/**
	 * Adds a single item to the group, it will be displayed at the end of the group
	 * 
	 * @param item
	 */
	void addItem(ToolbarItem item);

}
