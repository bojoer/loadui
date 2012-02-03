/* 
 * Copyright 2011 SmartBear Software
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
import javafx.scene.Parent;
import javafx.scene.Group;
import javafx.scene.layout.Container;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.util.Properties;
import javafx.util.Sequences;

import java.net.URI;
import java.lang.Thread;
import java.lang.ClassLoader;
import java.util.Comparator;
import java.lang.Exception;

import java.net.URL;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.util.BeanInjector;

/**
 * Contains static helper functions for common JavaFX related tasks.
 *
 * @author dain.nilsson
 */
public class FxUtils {} //This class exists only so that the above javafxdoc will be displayed.

/**
 * Similar to the builtin __DIR__, __ROOT__ gives the absolute path to the root of the enveloping bundle.
 * Useful for accessing resources which are not in the current package.
 */
public def __ROOT__ = "{__DIR__}".replaceAll("/com/eviware/loadui/fx/$", "/");

/**
 * Similar to __ROOT__ above, but uses the supplied __DIR__ as the base, so that is works for other bundles.
 */
public function root( dir:String ):String {
	dir.split("com/eviware/loadui", 2)[0];
}

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

public function compareZIndex( nodeA:Node, nodeB:Node ):Integer {
	if( nodeA == nodeB )
		return 0;
	if( isDescendant( nodeA, nodeB ) )
		return 1;
	if( isDescendant( nodeB, nodeA ) )
		return -1;
	
	var parents:Node[] = [ nodeA ];
	var p:Parent = nodeA.parent;
	while( p != null ) {
		insert p into parents;
		p = p.parent;
	}
	p = nodeB.parent;
	var oldP:Node = nodeB;
	var i = -1;
	while( p != null ) {
		i = Sequences.indexByIdentity( parents, p );
		if( i >= 0 ) {
			def children = getChildren( p );
			def indexA = Sequences.indexByIdentity( children, parents[i-1] );
			def indexB = Sequences.indexByIdentity( children, oldP );
			return indexB - indexA;
		}
		oldP = p;
		p = p.parent;
	}
	return 0;
}

public function getChildren( p:Parent ):Node[] {
	if( p instanceof Group ) {
		(p as Group).content
	} else if( p instanceof Container ) {
		(p as Container).content
	} else {
		[]
	}
}

public function openURL(url:String) {
	java.awt.Desktop.getDesktop().browse(new URI(url));
}

class CompareByString extends Comparator {
	override function compare( a:Object, b:Object ) {
		String.valueOf( a.toString() ).compareTo( String.valueOf( b.toString() ) )
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
	return "#{twoDigitHex((color.red*255) as Integer)}{twoDigitHex((color.green*255) as Integer)}{twoDigitHex((color.blue*255) as Integer)}{if(color.opacity != 1.0) twoDigitHex((color.opacity*255) as Integer) else ''}";
}

public function colorToWebString( color:java.awt.Color ):String {
	return "#{twoDigitHex(color.getRed())}{twoDigitHex(color.getGreen())}{twoDigitHex(color.getBlue())}{if(color.getAlpha() != 255) twoDigitHex(color.getAlpha()) else ''}";
}

public function awtColorToFx( color:java.awt.Color ):Color {
	Color.web( colorToWebString( color ) )
} 

function twoDigitHex( n:Integer ):String {
	def str = Integer.toHexString( n );
	if( str.length() < 2 )
		"0{str}"
	else
		str
}
 
public def defaultImage = Image { url: "{__ROOT__}images/png/default-component-icon.png" };
public def agentImage = Image { url: "{__ROOT__}images/png/agent-icon.png" };
public def projectImage = Image { url: "{__ROOT__}images/png/project-icon.png" };
public def testCaseImage = Image { url: "{__ROOT__}images/png/testcase-icon.png" };
public def assertionImage = Image { url: "{__ROOT__}images/png/assertion_icon_toolbar.png" };

public var imageResolvers:(function(object:Object):Image)[] = function(object):Image {
	if( object instanceof AgentItem ) {
		agentImage
	} else if( object instanceof ProjectItem ) {
		projectImage
	} else if( object instanceof SceneItem ) {
		testCaseImage
	} else if( object instanceof ComponentItem ) {
		Image { url: BeanInjector.getBean(ComponentRegistry.class).findDescriptor((object as ComponentItem).getType()).getIcon().toString() }
	} else if( object instanceof AssertionItem ) {
		assertionImage
	} else {
		null
	}
}

/**
 * Gets an Image to be used as an icon for the given object.
 */
public function getImageFor( object:Object ):Image {
	for( resolver in imageResolvers ) {
		def image = resolver(object);
		if( image != null ) return image;
	}
	
	return defaultImage;
}