package com.eviware.loadui.api.messaging;


public interface MessageAwaiter
{
	
	/**
	 * 
	 * @return true when message arrives. This is a blocking call.
	 */
	public boolean await();

}
