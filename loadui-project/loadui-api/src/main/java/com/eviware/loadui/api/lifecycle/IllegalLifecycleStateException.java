package com.eviware.loadui.api.lifecycle;

/**
 * Exception used when requesting a state change from an illegal state, for
 * instance, requesting a Start when already in a RUNNING state, or when a Start
 * has already been requested. Or, when requesting a Stop while already in an
 * IDLE state.
 * 
 * @author dain.nilsson
 */
public final class IllegalLifecycleStateException extends Exception
{
	private static final long serialVersionUID = 152307637308761877L;

	public IllegalLifecycleStateException( String message )
	{
		super( message );
	}
}