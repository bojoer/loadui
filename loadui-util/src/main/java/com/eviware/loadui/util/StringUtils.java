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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils
{
	public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

	public static boolean isNullOrEmpty( String string )
	{
		return string == null || string.equals( "" );
	}

	public static String capitalize( String string )
	{
		return isNullOrEmpty( string ) ? string : string.substring( 0, 1 ).toUpperCase() + string.substring( 1 );
	}

	public static String fixLineSeparators( String string )
	{
		return string == null ? null : string.replaceAll( "\\r\\n|\\r|\\n", LINE_SEPARATOR );
	}

	public static String multiline( String... lines )
	{
		if( lines.length == 0 )
			return null;
		if( lines.length == 1 )
			return lines[0];

		StringBuilder sb = new StringBuilder( lines[0] );
		for( int i = 1; i < lines.length; i++ )
			sb.append( LINE_SEPARATOR ).append( lines[i] );

		return sb.toString();
	}

	public static String serialize( Collection<String> strings )
	{
		StringBuilder sb = new StringBuilder();
		for( String item : strings )
			sb.append( item.length() ).append( ":" ).append( item );

		return sb.toString();
	}

	public static List<String> deserialize( String serialized )
	{
		List<String> strings = new ArrayList<String>();
		String remaining = serialized;
		String[] parts = remaining.split( ":", 2 );
		while( parts.length == 2 )
		{
			int length = Integer.parseInt( parts[0] );
			strings.add( parts[1].substring( 0, length ) );
			remaining = parts[1].substring( length );
			parts = remaining.split( ":", 2 );
		}
		return strings;
	}
}
