/* 
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Properties;

import java.net.URI;
import java.lang.Thread;
import java.lang.ClassLoader;
import java.util.Comparator;
import java.lang.Exception;

import java.net.URL;

/**
 * Contains static helper functions for common JavaFX related tasks.
 *
 * @author dain.nilsson
 */
public class FxUtils {} //This class exists only so that the above javafxdoc will be displayed.

public def VERSION = "1.5.0-SNAPSHOT";

/**
 * Similar to the builtin __DIR__, __ROOT__ gives the absolute path to the root of the enveloping bundle.
 * Useful for accessing resources which are not in the current package.
 */
public def __ROOT__ = "{__DIR__}".replaceAll("/com/eviware/loadui/fx/$", "/");

var jfxThread: Thread;

/**
 * Sets the class loader for JavaFX Thread.
 */
public function setJavaFXThreadClassLoader(classLoader: ClassLoader){
	jfxThread.setContextClassLoader(classLoader);
}

def urls = new Properties();
function runInit():Boolean {
	FX.deferAction(function():Void {
		def is = com.eviware.loadui.fx.Dummy.class.getResourceAsStream( "/properties/help_urls.properties" );
		urls.load( is );
		is.close();
		
		jfxThread = Thread.currentThread();
	});
	true
}
def initialized = runInit();

public function getURL( id:String ):String {
	urls.get( id )
}

/**
 * Checks to see if the current Thread is the JavaFX Thread. If so, runs the action.
 * If not, schedules the action to be run in the JavaFX Thread using FX.deferAction.
 */
public function runInFxThread( action: function():Void ) {
	if( Thread.currentThread() == jfxThread ) {
		action();
	} else {
		FX.deferAction( action );
	}
}

/**
 * Checks to see if the current Thread is the JavaFX Thread.
 */
public function isFxThread():Boolean {
	Thread.currentThread() == jfxThread
}

/**
 * Checks to see if a node is a descendant to another node.
 */
public function isDescendant( ancestor:Node, descendant:Node ) {
	var p:Node = descendant;
	while( p != null ) {
		p = p.parent;
		if( p == ancestor )
			return true;
	}
	false
}

public function openURL(url:String) {
	java.awt.Desktop.getDesktop().browse(new URI(url));
}

class CompareByString extends Comparator {
	override function compare( a:Object, b:Object ) {
		a.toString().compareTo( b.toString() )
	}
}

/**
 * Comparator that compares items by their toString representations.
 */
public def COMPARE_BY_TOSTRING = new CompareByString();

/**
 * Converts a JavaFX Color to an AWT Color.
 */
public function getAwtColor( color:Color ):java.awt.Color {
	return new java.awt.Color( color.red, color.green, color.blue, color.opacity );
}

/**
 * Creates an AWT Color from a web color string.
 */
public function getAwtColor( colorString:String ):java.awt.Color {
	return getAwtColor( Color.web( colorString ) );
}

public function colorToWebString( color:Color ):String {
	return "#{Integer.toHexString((color.red*255) as Integer)}{Integer.toHexString((color.green*255) as Integer)}{Integer.toHexString((color.blue*255) as Integer)}{if(color.opacity != 1.0) Integer.toHexString((color.opacity*255) as Integer) else ''}";
}
