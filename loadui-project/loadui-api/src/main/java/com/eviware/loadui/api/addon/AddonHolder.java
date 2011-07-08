package com.eviware.loadui.api.addon;

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
	public <T extends Addon> T getAddon( Class<T> type );

	public interface Support
	{
		public <T extends Addon> T getAddon( Class<T> cls );
	}
}
