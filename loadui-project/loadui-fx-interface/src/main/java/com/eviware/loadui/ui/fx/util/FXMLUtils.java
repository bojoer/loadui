package com.eviware.loadui.ui.fx.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.LabelBuilder;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

public class FXMLUtils
{
	private static final Logger log = LoggerFactory.getLogger( FXMLUtils.class );

	private static ClassLoader classLoader = FXMLUtils.class.getClassLoader();

	public static Node load( Class<?> type, final Callable<? extends Object> createController )
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setClassLoader( classLoader );

		URL fxmlURL = type.getResource( type.getSimpleName() + ".fxml" );
		loader.setLocation( fxmlURL );
		final Callback<Class<?>, Object> originalFactory = loader.getControllerFactory();
		loader.setControllerFactory( new Callback<Class<?>, Object>()
		{
			@Override
			public Object call( Class<?> cls )
			{
				Object controller;
				try
				{
					controller = createController.call();
					if( !cls.isInstance( controller ) )
					{
						controller = originalFactory.call( cls );
					}
				}
				catch( Exception e )
				{
					controller = originalFactory.call( cls );
				}
				return controller;
			}
		} );

		InputStream fxmlStream = null;
		try
		{
			fxmlStream = fxmlURL.openStream();
			return ( Parent )loader.load( fxmlStream );
		}
		catch( IOException e )
		{
			log.error( "Unable to load Node for Class: " + type, e );
			return LabelBuilder.create().text( e.getMessage() ).build();
		}
		finally
		{
			Closeables.closeQuietly( fxmlStream );
		}
	}
}
