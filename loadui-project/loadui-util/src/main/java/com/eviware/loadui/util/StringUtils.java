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

public class StringUtils
{
	public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

	public static boolean isNullOrEmpty( String string )
	{
		return string == null || string.equals( "" );
	}

	public static String capitalize( String string )
	{
		return string.substring( 0, 1 ).toUpperCase() + string.substring( 1 );
	}

	public static String fixLineSeparators( String string )
	{
		return string == null ? null : string.replaceAll( "\\r\\n|\\r|\\n", LINE_SEPARATOR );
	}
}
