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
package com.eviware.loadui.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlTokenSource;

import com.google.common.io.Files;

public class XmlBeansUtils
{
	@edu.umd.cs.findbugs.annotations.SuppressWarnings( value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "Failing to delete these files should be ignored." )
	public static void saveToFile( XmlTokenSource source, File target ) throws IOException
	{
		File backup = File.createTempFile( "loadui-temp-", ".bak", target.getParentFile() );
		Files.deleteRecursively( backup );

		File temp = File.createTempFile( "loadui-temp-", ".xml", target.getParentFile() );
		source.save( temp );
		if( !target.renameTo( backup ) )
		{
			temp.delete();
			throw new IOException( "Error saving file: " + target + "! Unable to create backup!" );
		}
		if( !temp.renameTo( target ) )
		{
			backup.delete();
			throw new IOException( "Error saving file: " + target + "! Unable to write to file!" );
		}

		Files.deleteRecursively( backup );
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends XmlObject> T[] moveArrayElement( T[] array, int from, int to )
	{
		List<T> list = new ArrayList<T>( array.length );
		for( int i = 0; i < array.length; i++ )
			if( i != from )
				list.add( array[i]/* .copy() */);
		list.add( to, array[from]/* .copy() */);
		// Copy doesn't seem to be needed here, as set<...>Array() makes copies of
		// all elements anyway.

		return list.toArray( ( T[] )Array.newInstance( array[0].getClass(), array.length ) );
	}
}
