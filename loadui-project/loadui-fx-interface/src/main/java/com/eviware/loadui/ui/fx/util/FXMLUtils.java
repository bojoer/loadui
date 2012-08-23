package com.eviware.loadui.ui.fx.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FXMLUtils
{
	private static ClassLoader classLoader = FXMLUtils.class.getClassLoader();

	public static void loadNew( Node root, Object controller )
	{
		loadNew( root, controller, Collections.<String, Object> emptyMap() );
	}

	public static void loadNew( Node root, Object controller, Map<String, ? extends Object> mapping )
	{
		FXMLLoader loader = new FXMLLoader( root.getClass().getResource( root.getClass().getSimpleName() + ".fxml" ) );
		loader.setClassLoader( classLoader );
		loader.setRoot( root );
		loader.setController( controller );

		if( mapping != null )
		{
			loader.getNamespace().putAll( mapping );
		}

		try
		{
			loader.load();
		}
		catch( IOException exception )
		{
			throw new RuntimeException( "Unable to load fxml view: " + root.getClass().getSimpleName() + ".fxml",
					exception );
		}
	}
}
