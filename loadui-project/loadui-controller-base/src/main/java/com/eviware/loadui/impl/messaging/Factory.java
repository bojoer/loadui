package com.eviware.loadui.impl.messaging;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.messaging.MessageAwaiter;
import com.eviware.loadui.api.messaging.MessageAwaiterFactory;
import com.eviware.loadui.api.model.AgentItem;

public class Factory implements MessageAwaiterFactory {

	@Override
	public MessageAwaiter create( AgentItem agent, String canvasId, Phase phase )
	{
		return new MessageAwaiterImpl( agent, canvasId, phase );
	}
	
}