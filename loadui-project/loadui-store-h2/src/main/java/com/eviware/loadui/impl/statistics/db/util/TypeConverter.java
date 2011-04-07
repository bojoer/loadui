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
package com.eviware.loadui.impl.statistics.db.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class TypeConverter
{

	public static BufferedImage imageFromByteArray( byte[] imagebytes )
	{
		try
		{
			if( imagebytes != null && ( imagebytes.length > 0 ) )
			{
				BufferedImage im = ImageIO.read( new ByteArrayInputStream( imagebytes ) );
				return im;
			}
			return null;
		}
		catch( IOException e )
		{
			throw new IllegalArgumentException( e.toString() );
		}
	}

	public static byte[] imageToByteArray( BufferedImage bufferedImage )
	{
		if( bufferedImage != null )
		{
			BufferedImage image = bufferedImage;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				ImageIO.write( image, "png", baos );
			}
			catch( IOException e )
			{
				throw new IllegalStateException( e.toString() );
			}
			byte[] b = baos.toByteArray();
			return b;
		}
		return new byte[0];
	}

	public static Object base64ToObject( String s )
	{
		try
		{
			byte[] data = Base64.decodeBase64( s );
			ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( data ) );
			Object o = ois.readObject();
			ois.close();
			return o;
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public static String objectToBase64( Serializable o )
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( o );
			oos.close();
			return new String( Base64.encodeBase64( baos.toByteArray() ) );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public static String objectToString( Object value )
	{
		if( value == null )
		{
			return "";
		}
		else if( value instanceof Date )
		{
			return String.valueOf( ( ( Date )value ).getTime() );
		}
		else if( value instanceof Number || value instanceof String || value instanceof Boolean )
		{
			return value.toString();
		}
		else if( value instanceof Serializable )
		{
			return objectToBase64( ( Serializable )value );
		}
		else if( value instanceof BufferedImage )
		{
			return Base64.encodeBase64String( imageToByteArray( ( BufferedImage )value ) );
		}
		else
		{
			return value.toString();
		}
	}

	public static Object stringToObject( String value, Class<? extends Object> type )
	{
		if( value == null )
		{
			return null;
		}
		else if( type == Long.class )
		{
			return Long.valueOf( value );
		}
		else if( type == Integer.class )
		{
			return Integer.valueOf( value );
		}
		else if( type == Double.class )
		{
			return Double.valueOf( value );
		}
		else if( type == Float.class )
		{
			return Float.valueOf( value );
		}
		else if( type == Boolean.class )
		{
			return Boolean.valueOf( value );
		}
		else if( type == String.class )
		{
			return value;
		}
		else if( type == Date.class )
		{
			Date d = new Date();
			d.setTime( Long.valueOf( value ) );
			return d;
		}
		else if( type == BufferedImage.class )
		{
			return imageFromByteArray( Base64.decodeBase64( value ) );
		}
		else
		{
			try
			{
				type.asSubclass( Serializable.class );
				return base64ToObject( value );
			}
			catch( Exception e )
			{
				return value;
			}
		}
	}

}
