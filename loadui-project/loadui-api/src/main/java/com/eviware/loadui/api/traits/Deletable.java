package com.eviware.loadui.api.traits;

/**
 * An Object which can be deleted.
 * 
 * @author dain.nilsson
 */
public interface Deletable
{
	/**
	 * If the Deletable also implements EventFirer, it should fire a BaseEvent
	 * with the DELETED constant as a key to inform listeners that the Deletable
	 * has been deleted.
	 */
	public final String DELETED = Deletable.class.getSimpleName() + "@deleted";

	/**
	 * Permanently removes the object (doesn't necessarily save the change to
	 * disk).
	 */
	public void delete();
}
