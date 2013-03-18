package com.eviware.loadui.api.messaging;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.model.AgentItem;

public interface MessageAwaiter
{
	
	/**
	 * 
	 * @return true when message arrives. This is a blocking call.
	 */
	public boolean await();
	
	
	interface MessageAwaiterFactory {
		
		MessageAwaiter create( AgentItem agent, String canvasId, Phase phase );
		
	}

}
