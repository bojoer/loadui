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
package com.eviware.loadui.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * A collection of static utility methods for simplifying some tasks.
 * 
 * @author dain.nilsson
 */
public class Utilities
{
	public static boolean deleteRecursive( File target )
	{
		if( target.isDirectory() )
		{
			for( File file : target.listFiles() )
				if( !deleteRecursive( file ) )
					return false;
		}
		return target.delete();
	}

	public static void copyDirectory( File sourceLocation, File targetLocation ) throws IOException
	{
		if( sourceLocation.isDirectory() )
		{
			if( !targetLocation.exists() )
				targetLocation.mkdir();

			String[] children = sourceLocation.list();
			for( int i = 0; i < children.length; i++ )
				copyDirectory( new File( sourceLocation, children[i] ), new File( targetLocation, children[i] ) );
		}
		else
		{

			InputStream in = new FileInputStream( sourceLocation );
			OutputStream out = new FileOutputStream( targetLocation );

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while( ( len = in.read( buf ) ) > 0 )
				out.write( buf, 0, len );
			in.close();
			out.close();
		}
	}

	public static int getAvailablePort()
	{
		ServerSocket ss = null;
		try
		{
			ss = new ServerSocket( 0 );
			ss.setReuseAddress( true );
			return ss.getLocalPort();
		}
		catch( SocketException e )
		{
		}
		catch( IOException e )
		{
		}
		finally
		{
			if( ss != null )
			{
				try
				{
					ss.close();
					Thread.sleep( 1000 );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}

		return -1;
	}

	public static int getAvailablePort( int start )
	{
		while( !isPortAvailable( start ) )
			start++ ;

		return start;
	}

	public static boolean isPortAvailable( int port )
	{
		ServerSocket ss = null;
		try
		{
			ss = new ServerSocket( port );
			ss.setReuseAddress( true );
			return true;
		}
		catch( IOException e )
		{
		}
		finally
		{
			if( ss != null )
			{
				try
				{
					ss.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
