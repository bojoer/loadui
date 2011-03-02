/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.statistics.db.properties.model;

import com.eviware.loadui.impl.statistics.db.properties.PropertiesBase;

public class ExecutionProperties extends PropertiesBase
{

	public static final String PROPERTIES_NAME = "execution";

	public static final String KEY_ID = "ID";
	public static final String KEY_START_TIME = "START_TIME";
	public static final String KEY_ARCHIVED = "ARCHIVED";
	public static final String KEY_LABEL = "LABEL";
	public static final String KEY_LENGTH = "LENGTH";
	public static final String KEY_ICON = "ICON";

	public ExecutionProperties( String baseDir )
	{
		super( baseDir );
	}

	@Override
	public String getName()
	{
		return PROPERTIES_NAME;
	}
	
}
