/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.assertion;

import java.util.Collection;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;

/**
 * Addon providing AssertionItems attached to the parent AddonHolder.
 * 
 * @author dain.nilsson
 */
public interface AssertionAddon extends Addon
{
	/**
	 * CollectionEvent key used for watching the AddonHolder for added/removed
	 * AssertionItems.
	 */
	public static final String ASSERTION_ITEMS = AssertionAddon.class.getName() + "assertions";

	/**
	 * Returns the contained AssertionItems.
	 * 
	 * @return
	 */
	public Collection<? extends AssertionItem<?>> getAssertions();

	/**
	 * Creates a new AssertionItem, asserting the ListenableValue referenced by
	 * the given Reference, belonging to the given owner.
	 * 
	 * @param owner
	 * @param listenableValueResolver
	 * @return
	 */
	public <T> AssertionItem.Mutable<T> createAssertion( Addressable owner,
			Resolver<? extends ListenableValue<T>> listenableValueResolver );
}
