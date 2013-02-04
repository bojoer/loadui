package com.eviware.loadui.ui.fx.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.util.BeanInjector;
import com.sun.javafx.PlatformUtil;

public class UIUtils
{
	protected static final Logger log = LoggerFactory.getLogger( UIUtils.class );
	
	private final static String TOOLBOX_IMAGES_PATH = "/com/eviware/loadui/ui/fx/toolboxIcons/";

	public static Image getImageFor( Object object )
	{
		if( object instanceof AgentItem || AgentItem.class.equals( object ) )
		{
			return new Image( root( "agent-icon.png" ) );
		}
		else if( object instanceof ProjectItem || ProjectItem.class.equals( object ) )
		{
			return new Image( root( "project-icon.png" ) );
		}
		else if( object instanceof SceneItem || SceneItem.class.equals( object ) )
		{
			return new Image( root( "testcase-icon.png" ) );
		}
		else if( object instanceof ComponentItem )
		{
			return new Image( BeanInjector.getBean( ComponentRegistry.class )
					.findDescriptor( ( ( ComponentItem )object ).getType() ).getIcon().toString() );
		}
		else if( object instanceof AssertionItem )
		{
			return new Image( root( "assertion_icon_toolbar.png" ) );
		}
		else
		{
			throw new RuntimeException( "No image found for resource "
					+ ( object instanceof Class ? object : "of class " + object.getClass().getName() ) );
		}
	}
	
	private static String root( String fileName )
	{
		return UIUtils.class.getResource( TOOLBOX_IMAGES_PATH + fileName ).toExternalForm();
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