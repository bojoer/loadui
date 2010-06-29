package com.eviware.loadui.launcher.api;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

public class OSGiUtils
{
	private static Framework framework;

	public static void shutdown()
	{
		if( framework != null )
		{
			try
			{
				System.out.println( "Stopping Framework..." );
				framework.stop();
			}
			catch( BundleException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			System.exit( 0 );
	}

	public static void setFramework( Framework framework )
	{
		OSGiUtils.framework = framework;
	}
}
