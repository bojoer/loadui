package com.eviware.loadui.ui.fx.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.api.ImageResolver;
import com.sun.javafx.PlatformUtil;

public class UIUtils
{
	protected static final Logger log = LoggerFactory.getLogger( UIUtils.class );

	public static Image getImageFor( Object object )
	{
		for( ImageResolver resolver : ObservableLists.ofServices( ImageResolver.class ) )
		{
			log.debug( "ImageResolver: " + resolver.getClass().getName() );
			Image image = resolver.resolveImageFor( object );
			if( image != null )
				return image;
		}
		throw new RuntimeException( "No image found for resource "
				+ ( object instanceof Class ? object : "of class " + object.getClass().getName() ) );
	}

	public static String toCssId( String label )
	{
		return label.toLowerCase().replace( " ", "-" );
	}

	public static void openInExternalBrowser( final String url )
	{
		if( !PlatformUtil.isMac() )
		{
			try
			{
				Desktop.getDesktop().browse( new java.net.URI( url ) );
			}
			catch( IOException | URISyntaxException e )
			{
				log.error( "Unable to launch browser with url in external browser!", e );
			}
			return;
		}

		try
		{
			Thread t = new Thread( new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						Runtime.getRuntime().exec( "open " + url );
					}
					catch( IOException e )
					{
						log.error( "Unable to fork native browser with url in external browser!", e );
					}
				}
			} );
			t.start();
		}
		catch( Exception e )
		{
			log.error( "unable to display url!", e );
		}
	}

	public static final String LATEST_DIRECTORY = "gui.latestDirectory";
	public static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );
}
