package com.eviware.loadui.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.apache.commons.cli.CommandLine;

public class LoadUIFXLauncher extends LoadUILauncher
{
	public static void main( String[] args )
	{
		Application.launch( FXApplication.class, args );
	}

	public LoadUIFXLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected void processCommandLine( CommandLine cmdLine )
	{
		try (InputStream is = getClass().getResourceAsStream( "/packages-extra.txt" ))
		{
			if( is != null )
			{
				StringBuilder out = new StringBuilder();
				byte[] b = new byte[4096];
				for( int n; ( n = is.read( b ) ) != -1; )
					out.append( new String( b, 0, n ) );

				String extra = configProps.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "" );
				if( !extra.isEmpty() )
					out.append( "," ).append( extra );

				configProps.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, out.toString() );
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	public static class FXApplication extends Application
	{
		private LoadUILauncher launcher;

		@Override
		public void start( final Stage stage ) throws Exception
		{
			Scene splashScene;
			try
			{
				splashScene = FXMLLoader.load( new File( "res/loadui-splash.fxml" ).toURI().toURL() );
			}
			catch( IOException e )
			{
				splashScene = SceneBuilder.create().width( 600 ).height( 320 ).fill( Color.DARKGRAY )
						.root( LabelBuilder.create().text( System.getProperty( LOADUI_NAME, "loadUI" ) ).build() ).build();
			}

			final Stage splash = StageBuilder.create().style( StageStyle.TRANSPARENT ).scene( splashScene )
					.icons( new Image( "res/icon_64x64.png" ), new Image( "res/icon_32x32.png" ) ).build();
			splash.initModality( Modality.APPLICATION_MODAL );
			splash.centerOnScreen();
			splash.show();
			splash.toFront();

			stage.getIcons().addAll( new Image( "res/icon_64x64.png" ), new Image( "res/icon_32x32.png" ) );
			stage.setOnShown( new EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent event )
				{
					System.out.println( "closing splash!" );
					splash.close();
				}
			} );

			System.out.println( "start called!" );

			Task<Void> task = new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					System.setSecurityManager( null );

					launcher = createLauncher( getParameters().getRaw().toArray( new String[0] ) );
					launcher.init();
					launcher.start();

					launcher.framework.getBundleContext().registerService( Stage.class, stage,
							new Hashtable<String, Object>() );
					return null;
				}
			};

			new Thread( task ).start();
		}

		protected LoadUILauncher createLauncher( String[] args )
		{
			return new LoadUIFXLauncher( args );
		}

		@Override
		public void stop() throws Exception
		{
			launcher.framework.getBundleContext().getBundle( 0 ).stop();
		}
	}
}
