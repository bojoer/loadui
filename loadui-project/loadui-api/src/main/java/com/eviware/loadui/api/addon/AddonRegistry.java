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
import javax.annotation.Nullable;

/**
 * A registry of available Addons, with factories.
 * 
 * @author dain.nilsson
 */
public interface AddonRegistry
{
	/**
	 * Registers an Addon.Factory for creating Addons of a specific type. Only
	 * one factory should be registered per Addon type.
	 * 
	 * @param type
	 * @param factory
	 */
	public <T extends Addon> void registerFactory( @Nonnull Class<T> type, @Nonnull Addon.Factory<T> factory );

	/**
	 * Unregisters an Addon.Factory that has previously been registered.
	 * 
	 * @param type
	 * @param factory
	 */
	public <T extends Addon> void unregisterFactory( @Nonnull Class<T> type, @Nonnull Addon.Factory<T> factory );

	/**
	 * Returns the Addon.Factory registered for a specific Addon type.
	 * 
	 * @param type
	 * @return
	 */
	@Nullable
	public <T extends Addon> Addon.Factory<T> getFactory( @Nonnull Class<T> type );
}
