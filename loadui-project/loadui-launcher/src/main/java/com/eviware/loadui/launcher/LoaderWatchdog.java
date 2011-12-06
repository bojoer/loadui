package com.eviware.loadui.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

public class LoaderWatchdog implements Runnable
{
	private final Framework framework;

	public LoaderWatchdog( Framework framework )
	{
		this.framework = framework;
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep( 10000 );
		}
		catch( InterruptedException e1 )
		{
			e1.printStackTrace();
		}

		for( Bundle bundle : framework.getBundleContext().getBundles() )
		{
			switch( bundle.getState() )
			{
			case Bundle.ACTIVE :
				break;
			case Bundle.RESOLVED :
				break;
			default :
				System.err.printf( "Bundle: %s state: %s", bundle.getSymbolicName(), bundle.getState() );
				System.err.printf( "Headers: %s", bundle.getHeaders() );

				try
				{
					bundle.start();
				}
				catch( BundleException e )
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
}
