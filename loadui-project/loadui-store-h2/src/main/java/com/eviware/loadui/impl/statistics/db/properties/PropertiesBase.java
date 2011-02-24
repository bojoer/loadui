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
package com.eviware.loadui.impl.statistics.db.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import com.eviware.loadui.impl.statistics.db.util.TypeConverter;

public abstract class PropertiesBase
{
	private Properties properties = new Properties();

	private File propertiesFile;

	public PropertiesBase( String baseDir )
	{
		File dir = new File( baseDir );
		if( !dir.exists() )
		{
			dir.mkdirs();
		}
		propertiesFile = new File( baseDir + File.separator + getName() + ".properties" );
		if( !propertiesFile.exists() )
		{
			store();
		}
		load();
	}

	private void load()
	{
		try
		{
			FileInputStream inStream = new FileInputStream( propertiesFile );
			properties.load( inStream );
			inStream.close();
		}
		catch( Exception e )
		{
			throw new RuntimeException( "Unable to load properties", e );
		}
	}

	private void store()
	{
		try
		{
			FileOutputStream outStream = new FileOutputStream( propertiesFile );
			properties.store( outStream, "" );
			outStream.close();
		}
		catch( Exception e )
		{
			throw new RuntimeException( "Unable to store properties", e );
		}
	}

	public <T> T get( String key, Class<T> t )
	{
		return t.cast( TypeConverter.stringToObject( properties.getProperty( key ), t ) );
	}

	public void set( String key, Object value )
	{
		properties.setProperty( key, TypeConverter.objectToString( value ) );
		store();
	}

	public abstract String getName();
}
