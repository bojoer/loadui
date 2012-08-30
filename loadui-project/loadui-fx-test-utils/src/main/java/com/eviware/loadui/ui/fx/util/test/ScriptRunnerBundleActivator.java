package com.eviware.loadui.ui.fx.util.test;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.Stage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ScriptRunnerBundleActivator implements BundleActivator
{
	public static final String TEST_SCRIPT = "testScript";
	public static final String TEST_SCRIPT_PORT = "testScriptPort";
	private static final Logger log = LoggerFactory.getLogger( ScriptRunnerBundleActivator.class );
	private GroovyShell shell;
	private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile( ".*Content-Length:\\s+(\\d+).*" );

	@Override
	public void start( BundleContext context ) throws Exception
	{
		Thread thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
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

					Binding binding = new Binding();

					binding.setVariable( "workspace", workspaceProvider.getWorkspace() );
					binding.setVariable( "stage", stage );
					binding.setVariable( "controller", ControllerApi.wrap( new FXScreenController() ).target( stage ) );

					Thread.sleep( 3000 );

					shell = new GroovyShell( binding );

					String testScript = System.getProperty( TEST_SCRIPT );
					if( testScript != null )
					{
						runScriptFile( testScript );
					}

					String serverPort = System.getProperty( TEST_SCRIPT_PORT );
					if( serverPort != null )
					{
						startServer( Integer.parseInt( serverPort ) );
					}
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

	private void runScriptFile( final String testScript )
	{
		try
		{
			log.info( "Running test script: {}", testScript );

			StringBuilder scriptBuilder = new StringBuilder();
			for( String line : Files.readLines( new File( testScript ), Charsets.UTF_8 ) )
			{
				scriptBuilder.append( line ).append( "\r\n" );
			}

			Object result = runScript( scriptBuilder.toString() );

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

	private void startServer( final int port )
	{
		try (ServerSocket ssocket = new ServerSocket( port ))
		{
			System.out.println( "Listening for POST on port: " + port );

			while( true )
			{
				try (Socket socket = ssocket.accept())
				{
					BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
					String line = null;
					int length = -1;
					while( !( line = reader.readLine() ).equals( "" ) )
					{
						Matcher matcher = CONTENT_LENGTH_PATTERN.matcher( line );
						if( matcher.matches() )
						{
							length = Integer.parseInt( matcher.group( 1 ) );
						}
					}

					char[] bodyChars = new char[length];
					reader.read( bodyChars, 0, length );
					String body = new String( bodyChars );

					log.info( "Received script! Running..." );

					Object result;
					try
					{
						result = runScript( body );
						String response = "Script completed successfully with result:\r\n" + result;
						log.info( response );

						try (PrintStream ps = new PrintStream( socket.getOutputStream() ))
						{
							ps.println( "HTTP/1.1 200 OK" );
							ps.println( "Content-Type: text/plain; charset=UTF-8" );
							ps.println( "Connection: close" );
							ps.println( "Content-Length: " + response.length() );
							ps.println();
							ps.println( response );
						}
					}
					catch( RuntimeException | AssertionError e )
					{
						log.error( "Script threw exception: ", e );
						try (PrintStream ps = new PrintStream( socket.getOutputStream() ))
						{
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter( sw );
							pw.println( "Test FAILED, with the error:" );
							e.printStackTrace( pw );
							String response = sw.toString();

							ps.println( "HTTP/1.1 500 Internal Server Error" );
							ps.println( "Content-Type: text/plain; charset=UTF-8" );
							ps.println( "Connection: close" );
							ps.println( "Content-Length: " + response.length() );
							ps.println();
							ps.println( response );
						}
					}
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}
	}

	private Object runScript( final String testScript ) throws Exception
	{
		Script script = shell.parse( testScript );
		return script.run();
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
	}
}
