package com.eviware.loadui.ui.fx;

import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.stage.Stage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.CoreImageResolver;
import com.eviware.loadui.util.BeanInjector;

public class JavaFXActivator implements BundleActivator
{
	@Override
	public void start( final BundleContext context ) throws Exception
	{
		System.out.println( "JavaFX2 bundle started!" );

		final ClassLoader bundleClassLoader = JavaFXActivator.class.getClassLoader();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				//This is needed for custom controls to be able to load their skins from this bundle.
				//With multiple bundles this could be problematic, and should be replaced by some classloader that delegates to multiple bundles.
				Thread.currentThread().setContextClassLoader( bundleClassLoader );
			}
		} );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					BeanInjector.getBean( CoreImageResolver.class );
					new MainWindow( BeanInjector.getBeanFuture( Stage.class ).get(), BeanInjector.getBeanFuture(
							WorkspaceProvider.class ).get() ).show();
				}
				catch( InterruptedException | ExecutionException e )
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
		Thread thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep( 6000 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}

				System.out.println( "Shutdown timed out, forcing close..." );
				System.exit( 0 );
			}
		} );
		thread.setDaemon( true );
		thread.start();
	}
}
