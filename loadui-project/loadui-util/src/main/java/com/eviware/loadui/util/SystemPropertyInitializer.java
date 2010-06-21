/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.util;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for setting System properties in a Spring configuration file.
 * 
 * @author dain.nilsson
 */
public class SystemPropertyInitializer
{
	public void setProperties( Map<String, String> properties )
	{
		for( Entry<String, String> entry : properties.entrySet() )
			System.setProperty( entry.getKey(), entry.getValue() );
	}

	public void setDefaultProperties( Map<String, String> properties )
	{
		for( Entry<String, String> entry : properties.entrySet() )
			if( System.getProperty( entry.getKey() ) == null )
				System.setProperty( entry.getKey(), entry.getValue() );
	}
}
