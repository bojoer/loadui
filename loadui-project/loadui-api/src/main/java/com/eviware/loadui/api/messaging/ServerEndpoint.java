package com.eviware.loadui.api.messaging;

public interface ServerEndpoint
{
	public void addConnectionListener( ConnectionListener listener );

	public void removeConnectionListener( ConnectionListener listener );
}
