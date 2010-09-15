package com.eviware.loadui.launcher.api;

import org.osgi.framework.Bundle;
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
		System.exit( 0 );
	}

	public static void setFramework( Framework framework )
	{
		OSGiUtils.framework = framework;
	}
}
