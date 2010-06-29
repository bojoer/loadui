package com.eviware.loadui.launcher.impl;

import com.eviware.loadui.launcher.api.Command;

public class StringCommand implements Command
{
	public final String command;

	public StringCommand( String command )
	{
		this.command = command;
	}

	@Override
	public String getCommand()
	{
		return command;
	}

	@Override
	public String toString()
	{
		return command;
	}
}
