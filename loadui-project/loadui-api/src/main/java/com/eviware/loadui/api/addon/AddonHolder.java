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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.events.EventFirer;

/**
 * An object which can be extended using Addons.
 * 
 * @author dain.nilsson
 */
public interface AddonHolder extends EventFirer
{
	/**
	 * Returns the Addon instance for a specific Addon class, creating it if it
	 * doesn't already exist.
	 * 
	 * @param type
	 * @return
	 */
	@Nonnull
	public <T extends Addon> T getAddon( @Nonnull Class<T> type );

	/**
	 * Support class for AddonHolders, providing an implementation of getAddon to
	 * delegate to.
	 * 
	 * @author dain.nilsson
	 */

	public interface Support
	{
		@Nonnull
		public <T extends Addon> T getAddon( @Nonnull Class<T> cls );
	}
}
