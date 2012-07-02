package com.eviware.loadui.ui.fx;

import java.util.concurrent.ExecutionException;

import javafx.stage.Stage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.fx.TestWindow;
import com.eviware.loadui.util.BeanInjector;

public class JavaFXActivator implements BundleActivator
{
	@Override
	public void start( final BundleContext context ) throws Exception
	{
		System.out.println( "JavaFX2 bundle started!" );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					new TestWindow( BeanInjector.getBeanFuture( Stage.class ).get(), BeanInjector.getBeanFuture(
							WorkspaceProvider.class ).get() ).show();
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				catch( ExecutionException e )
				{
					e.printStackTrace();
				}
			}
		} ).start();
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		System.out.println( "JavaFX2 bundle stopped!" );
	}
}
