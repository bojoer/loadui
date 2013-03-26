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
