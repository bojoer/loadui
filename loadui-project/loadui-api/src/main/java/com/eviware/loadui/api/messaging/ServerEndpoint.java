package com.eviware.loadui.api.messaging;

import java.util.Set;

public interface ServerEndpoint
{
	public Set<MessageEndpoint> getConnectedEndpoints();

	public void addConnectionListener( ConnectionListener listener );

	public void removeConnectionListener( ConnectionListener listener );
}
