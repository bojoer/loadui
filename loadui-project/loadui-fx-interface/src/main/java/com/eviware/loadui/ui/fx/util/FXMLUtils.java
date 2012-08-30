package com.eviware.loadui.ui.fx.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class FXMLUtils
{
	public static ClassLoader classLoader = FXMLUtils.class.getClassLoader();

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller.
	 */
	public static void load( Node root )
	{
		load( root, root );
	}

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller as the controller.
	 */
	public static void load( Node root, Object controller )
	{
		load( root, controller, Collections.<String, Object> emptyMap() );
	}

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller as the controller. Uses mapping
	 * for controlling namespaces.
	 */
	public static void load( Node root, Object controller, Map<String, ? extends Object> mapping )
	{
		load( root, controller, mapping, root.getClass().getResource( root.getClass().getSimpleName() + ".fxml" ) );
	}

	/**
	 * Loads an fxml resource from resourceName and sets root as the component
	 * tree root and controller as the controller.
	 */
	public static void load( Node root, Object controller, URL resourceName )
	{
		load( root, controller, Collections.<String, Object> emptyMap(), resourceName );
	}

	/**
	 * Loads an fxml resource from resourceName and sets root as the component
	 * tree root and controller as the controller. Uses mapping for controlling
	 * namespaces.
	 */
	public static void load( Node root, Object controller, Map<String, ? extends Object> mapping, URL resourceName )
	{
		FXMLLoader loader = new FXMLLoader( resourceName );
		loader.setClassLoader( classLoader );
		loader.setRoot( root );
		loader.setController( controller );

		if( mapping != null && !mapping.isEmpty() )
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
