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
package com.eviware.loadui.api.assertion;

import java.util.Collection;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;

public interface AssertionAddon extends Addon
{
	public static final String ASSERTION_ITEMS = AssertionAddon.class.getName() + "assertions";

	public Collection<? extends AssertionItem> getAssertions();

	public <T> AssertionItem.Mutable<T> createAssertion( Addressable owner,
			Resolver<ListenableValue<T>> listenableValueResolver );
}
