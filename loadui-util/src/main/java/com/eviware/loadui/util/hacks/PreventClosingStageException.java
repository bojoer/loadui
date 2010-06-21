package com.eviware.loadui.util.hacks;

@SuppressWarnings( "serial" )
public class PreventClosingStageException extends Exception
{
	private final String message = "This is hack exception that does nothing. It is used to prevent JavaFx stage from closing.";

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public void printStackTrace()
	{
		System.out.println( message );
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		return null;
	}
}
