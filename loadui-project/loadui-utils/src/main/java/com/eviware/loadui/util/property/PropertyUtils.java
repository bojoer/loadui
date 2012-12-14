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
package com.eviware.loadui.util.property;

import java.util.ArrayList;
import java.util.Collection;

import com.eviware.loadui.api.property.Property;

public class PropertyUtils
{
	public static boolean isVisible( Property<?> property )
	{
		return property.getKey().startsWith( "_" );
	}

	public static Collection<Property<?>> getVisible( Collection<Property<?>> properties )
	{
		return getVisible( properties, true );
	}

	public static Collection<Property<?>> getVisible( Collection<Property<?>> properties, boolean visible )
	{
		ArrayList<Property<?>> visibles = new ArrayList<>();
		for( Property<?> property : properties )
			if( isVisible( property ) == visible )
				visibles.add( property );

		return visibles;
	}

}
