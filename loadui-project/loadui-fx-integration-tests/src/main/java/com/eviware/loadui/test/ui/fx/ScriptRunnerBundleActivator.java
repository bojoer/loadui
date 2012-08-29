package com.eviware.loadui.test.ui.fx;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.util.concurrent.Callable;

import javafx.stage.Stage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class ScriptRunnerBundleActivator implements BundleActivator
{
	public static final String TEST_SCRIPT = "testScript";
	private static final Logger log = LoggerFactory.getLogger( ScriptRunnerBundleActivator.class );

	@Override
	public void start( BundleContext context ) throws Exception
	{
		final String testScript = System.getProperty( TEST_SCRIPT );
		if( testScript == null )
		{
			return;
		}

		Thread thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Binding binding = new Binding();
					Stage stage = BeanInjector.getBeanFuture( Stage.class ).get();
					final WorkspaceProvider workspaceProvider = BeanInjector.getBeanFuture( WorkspaceProvider.class ).get();
					TestUtils.awaitCondition( new Callable<Boolean>()
					{
						@Override
						public Boolean call() throws Exception
						{
							return workspaceProvider.isWorkspaceLoaded();
						}
					} );

					binding.setVariable( "workspace", workspaceProvider.getWorkspace() );
					binding.setVariable( "stage", stage );
					binding.setVariable( "controller", ControllerApi.wrap( new FXScreenController() ).target( stage ) );

					Thread.sleep( 3000 );

					GroovyShell shell = new GroovyShell( binding );

					log.info( "Running test script: {}", testScript );

					Script script = shell.parse( new File( testScript ) );
					final Object result = script.run();
					log.info( "Script completed successfully with result: {}", result );
					BeanInjector.getBean( BundleContext.class ).getBundle( 0 ).stop();

					Thread.sleep( 5000 );
					System.exit( 0 );
				}
				catch( Exception e )
				{
					e.printStackTrace();
					System.exit( -1 );
				}
			}
		} );

		thread.setDaemon( true );
		thread.start();
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
	}
}
