package com.eviware.loadui.ui.fx;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.stage.Stage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.ErrorHandler;
import com.eviware.loadui.util.BeanInjector;

public class JavaFXActivator implements BundleActivator
{
	@Override
	public void start( final BundleContext context ) throws Exception
	{
		System.out.println( "JavaFX2 bundle started!" );

		final ClassLoader bundleClassLoader = JavaFXActivator.class.getClassLoader();

		// delay requesting beans from OSGi in order to let it get started first
		new Timer( "JavaFX-LoadUI-Loader" ).schedule( new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					Stage stage = BeanInjector.getBeanFuture( Stage.class ).get();
					startJavaFX( stage, bundleClassLoader );
				}
				catch( Exception e )
				{
					System.out.println( "JavaFXActivator Unable to get Stage > " + e );
					ErrorHandler.promptRestart();
				}
			}
		}, 2000 );

	}

	private void startJavaFX( final Stage stage, final ClassLoader bundleClassLoader )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				//This is needed for custom controls to be able to load their skins from this bundle.
				//With multiple bundles this could be problematic, and should be replaced by some classloader that delegates to multiple bundles.

				System.out
						.println( "JavaFXActivator: loading Swing classes so they are visible by the licensing framework." );

				// Instantiate objects to fix Classloading problems in tablelog. Do not remove.
				//new org.jdesktop.swingx.JXTable();
				new javax.swing.JScrollPane();
				new javax.swing.JRadioButton();
				new javax.swing.JTextField();
				new javax.swing.JComboBox<String>();
				new javax.swing.JToolTip();
				new javax.swing.JOptionPane();
				new javax.swing.JEditorPane();
				new javax.swing.JProgressBar();
				new javax.swing.JFileChooser();
				new javax.swing.JToggleButton();
				new javax.swing.JCheckBox();
				new javax.swing.JRootPane();
				new javax.swing.JFormattedTextField();

				Thread.currentThread().setContextClassLoader( bundleClassLoader );

				try
				{
					new MainWindow( stage, BeanInjector.getBeanFuture( WorkspaceProvider.class ).get() ).show();
				}
				catch( RuntimeException | InterruptedException | ExecutionException e )
				{
					e.printStackTrace();
					ErrorHandler.promptRestart();
				}

			}
		} );
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
