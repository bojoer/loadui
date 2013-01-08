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
package com.eviware.loadui.launcher.api;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

public class OSGiUtils
{
	private static Framework framework;

	public static void shutdown()
	{
		shutdown( 0 );
	}

	public static void shutdown( int code )
	{
		stopFramework();
		System.exit( code );
	}

	private static void stopFramework()
	{
		if( framework != null )
		{
			try
			{
				System.out.println( "Stopping Framework..." );
				boolean wait = true;
				long start = System.currentTimeMillis();
				while( wait )
				{
					wait = false;
					for( Bundle bundle : framework.getBundleContext().getBundles() )
					{
						if( bundle.getState() == Bundle.STARTING && System.currentTimeMillis() - start < 5000 )
						{
							wait = true;
							Thread.sleep( 250 );
							break;
						}
					}
				}

				framework.stop();
			}
			catch( BundleException e )
			{
				e.printStackTrace();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	public static void setFramework( Framework framework )
	{
		OSGiUtils.framework = framework;
	}

	public static void restart()
	{
		stopFramework();
		try
		{
			framework.waitForStop( 0 );
		}
		catch( InterruptedException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			framework.start();
		}
		catch( BundleException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
