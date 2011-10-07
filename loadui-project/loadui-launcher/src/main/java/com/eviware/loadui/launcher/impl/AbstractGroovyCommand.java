/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.launcher.impl;

import java.util.Map;

import com.eviware.loadui.launcher.api.GroovyCommand;

public abstract class AbstractGroovyCommand implements GroovyCommand
{
	private final Map<String, Object> attributes;
	private boolean exit = true;

	public AbstractGroovyCommand( Map<String, Object> attributes )
	{
		this.attributes = attributes;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	public void setExit( boolean exit )
	{
		this.exit = exit;
	}

	@Override
	public boolean exitOnCompletion()
	{
		return exit;
	}
}
