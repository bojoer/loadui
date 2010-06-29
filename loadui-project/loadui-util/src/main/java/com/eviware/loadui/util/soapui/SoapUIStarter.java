/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.loadui.util.soapui;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapUIStarter
{
	private static Logger logger = LoggerFactory.getLogger( SoapUIStarter.class );
	private static Boolean isWindows;

	public static void start( String soapUIbatPath )
	{
		if( CajoClient.getInstance().testConnection() )
		{
			logger.info( "Cajo online!Test passed!" );
			try
			{
				// try to give it a focus
				CajoClient.getInstance().invoke( "bringToFront", null );
			}
			catch( Exception e )
			{
				logger.info( "SoapUI is running but can't move it to the front." );
			}
			return;
		}

		logger.info( "Cajo offline!Test not passed!" );
		String extension = isWindows() ? ".bat" : ".sh";
		if( extension.equals( ".sh" ) )
		{
			soapUIbatPath = soapUIbatPath.replace( ".bat", ".sh" );
		}
		try
		{
			File file = new File( soapUIbatPath );
			if( !file.exists() )
			{
				return;
			}
			String[] commandsWin = new String[] { "cmd.exe", "/c", soapUIbatPath };
			String[] commandsLinux = new String[] { "sh", soapUIbatPath };
			logger.info( "Launching soapUI..." );
			ProcessBuilder pb = new ProcessBuilder( isWindows() ? commandsWin : commandsLinux );
			Process p = pb.start();
			// Not closing the input stream may prevent the process from starting
			// immediately.
			p.getInputStream().close();
		}
		catch( Exception e )
		{
			logger.error( "Error while start soapui ", e );
		}
	}

	public static boolean isWindows()
	{
		if( isWindows == null )
			isWindows = new Boolean( System.getProperty( "os.name" ).indexOf( "Windows" ) >= 0 );

		return isWindows.booleanValue();
	}

}