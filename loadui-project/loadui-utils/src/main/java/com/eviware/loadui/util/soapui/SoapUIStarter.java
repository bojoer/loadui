/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.soapui;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapUIStarter
{
	/** SoapUI start timeout in milliseconds */
	public static final long START_TIMEOUT = 20000;

	private static Logger logger = LoggerFactory.getLogger( SoapUIStarter.class );
	private static boolean isWindows = System.getProperty( "os.name" ).contains( "Windows" );
	private static boolean isOSX = System.getProperty( "os.name" ).contains( "Mac OS X" );

	public static void start( String soapUIbatPath )
	{
		if( CajoClient.getInstance().testConnection() )
		{
			logger.debug( "Cajo online!Test passed!" );
			try
			{
				// try to give it a focus
				CajoClient.getInstance().invoke( "bringToFront", null );
			}
			catch( Exception e )
			{
				logger.warn( "SoapUI is running but can't move it to the front." );
			}
			return;
		}

		logger.warn( "Cajo offline!Test not passed!" );
		// String extension = isWindows() ? ".bat" : ".sh";
		// if( extension.equals( ".sh" ) )
		// {
		// soapUIbatPath = soapUIbatPath.replace( ".bat", ".sh" );
		// }
		try
		{
			File file = new File( soapUIbatPath );
			if( !file.exists() )
			{
				return;
			}
			String[] commandsWin = new String[] { "cmd.exe", "/c", soapUIbatPath };
			String[] commandsLinux = new String[] { "sh", soapUIbatPath };
			String[] commandsOSX = new String[] { soapUIbatPath + "/Contents/MacOS/JavaApplicationStub" };
			logger.info( "Launching soapUI..." );
			ProcessBuilder pb;
			if( isWindows )
				pb = new ProcessBuilder( commandsWin );
			else if( isOSX )
				pb = new ProcessBuilder( commandsOSX );
			else
				pb = new ProcessBuilder( commandsLinux );
			Process p = pb.start();
			// Not closing the input stream may prevent the process from starting
			// immediately.
			p.getInputStream().close();

			// wait for soapUI to start
			final CountDownLatch latch = new CountDownLatch( 1 );
			new Thread()
			{
				@Override
				public void run()
				{
					long start = System.currentTimeMillis();
					while( !CajoClient.getInstance().testConnection() )
					{
						try
						{
							Thread.sleep( 1000 );
						}
						catch( InterruptedException e )
						{
							// do nothing
						}
						if( System.currentTimeMillis() - start > START_TIMEOUT )
						{
							logger.error( "Unable to establish connection to SoapUI. The timeout period elapsed prior to obtaining a connection to cajo server." );
							break;
						}
					}
					latch.countDown();
				}
			}.start();
			latch.await();
		}
		catch( Exception e )
		{
			logger.error( "Error while start soapui ", e );
		}
	}
}
