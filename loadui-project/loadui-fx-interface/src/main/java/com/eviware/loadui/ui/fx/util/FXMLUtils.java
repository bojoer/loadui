/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javafx.fxml.FXMLLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FXMLUtils
{
	protected static final Logger log = LoggerFactory.getLogger( FXMLUtils.class );
	public static ClassLoader classLoader = FXMLUtils.class.getClassLoader();

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller.
	 */
	public static void load( Object root )
	{
		load( root, root );
	}

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller as the controller.
	 */
	public static void load( Object root, Object controller )
	{
		load( root, controller, Collections.<String, Object> emptyMap() );
	}

	/**
	 * Loads an fxml resource using the simple name of root's class and sets root
	 * as the component tree root and controller as the controller. Uses mapping
	 * for controlling namespaces.
	 */
	public static void load( Object root, Object controller, Map<String, ? extends Object> mapping )
	{
		String fileName = root.getClass().getSimpleName() + ".fxml";
		URL url = root.getClass().getResource( fileName );
		if( url == null )
		{
			throw new RuntimeException(
					fileName
							+ " not found. If "
							+ root.getClass()
							+ " is extending class Zuper, then class Zuper must use a version of FXMLUtils.load() that explicitly defines the path to the FXML. E.g. FXMLUtils.load( this, this, Zuper.class.getResource( Zuper.class.getSimpleName() + '.fxml' ) )." );
		}
		load( root, controller, mapping, url );
	}

	/**
	 * Loads an fxml resource from resourceName and sets root as the component
	 * tree root and controller as the controller.
	 */
	public static void load( Object root, Object controller, URL resourceName )
	{
		load( root, controller, Collections.<String, Object> emptyMap(), resourceName );
	}

	/**
	 * Loads an fxml resource from resourceName and sets root as the component
	 * tree root and controller as the controller. Uses mapping for controlling
	 * namespaces.
	 */
	public static void load( Object root, Object controller, Map<String, ? extends Object> mapping, URL resourceName )
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
			throw new RuntimeException( "Unable to load fxml view: " + resourceName, exception );
		}
	}
}
