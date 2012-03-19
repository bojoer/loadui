package com.eviware.loadui.api.component;

public class ComponentCreationException extends Exception
{
	private static final long serialVersionUID = 1299528546052371222L;

	private final String componentType;

	public ComponentCreationException( String type )
	{
		this( type, null, null );
	}

	public ComponentCreationException( String type, String message )
	{
		this( type, message, null );
	}

	public ComponentCreationException( String type, Throwable throwable )
	{
		this( type, null, throwable );
	}

	public ComponentCreationException( String type, String message, Throwable throwable )
	{
		super( message, throwable );

		componentType = type;
	}

	public String getComponentType()
	{
		return componentType;
	}
}