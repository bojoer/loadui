package com.eviware.loadui.ui.fx.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.PlatformUtil;

public class UIUtils
{
	public static final Logger LOG = LoggerFactory.getLogger( UIUtils.class );

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
				LOG.error( "Unable to launch browser with url in external browser!", e );
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
						LOG.error( "Unable to fork native browser with url in external browser!", e );
					}
				}
			} );
			t.start();
		}
		catch( Exception e )
		{
			LOG.error( "unable to display url!", e );
		}
	}

	public static final String LATEST_DIRECTORY = "gui.latestDirectory";
	public static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );
}
