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
package com.eviware.loadui.api.addon;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides additional functionality to an AddonHolder, without having to extend
 * the AddonHolder itself.
 * 
 * @author dain.nilsson
 */
public interface Addon
{
	/**
	 * Context object allowing an Addon to interact with its host AddonHolder.
	 * 
	 * @author dain.nilsson
	 */
	public interface Context
	{
		/**
		 * Returns the AddonHolder owning the Addon.
		 * 
		 * @return
		 */
		@Nonnull
		public AddonHolder getOwner();

		/**
		 * Creates an AddonItemSupport object which an AddonItem managed by the
		 * Addon may use to persist data.
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

		/**
		 * Exports the given AddonItem.Support (which must belong to the Context)
		 * to a String, which can later be imported into another Addon of the same
		 * type.
		 * 
		 * @param support
		 * @return
		 */
		@Nonnull
		public String exportAddonItemSupport( AddonItem.Support support );

		/**
		 * Imports a previously exported AddonItem.Support.
		 * 
		 * @param exportedAddonItemSupport
		 * @return
		 */
		@Nonnull
		public AddonItem.Support importAddonItemSupport( String exportedAddonItemSupport );
	}

	/**
	 * Factory interface for instantiating Addons of a specific type. Must be
	 * registered in the AddonRegistry to function.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <T>
	 */
	public interface Factory<T extends Addon>
	{
		/**
		 * Returns the type of the AddonFactory.
		 * 
		 * @return
		 */
		@Nonnull
		public Class<T> getType();

		/**
		 * Creates a new instance of the specific Addon using the given Context.
		 * 
		 * @param context
		 * @return
		 */
		@Nonnull
		public T create( @Nonnull Context context );

		/**
		 * Returns a set of AddonHolder types for which the Addon should be
		 * eagerly loaded for.
		 * 
		 * @return
		 */
		@Nonnull
		public Set<Class<?>> getEagerTypes();
	}
}
