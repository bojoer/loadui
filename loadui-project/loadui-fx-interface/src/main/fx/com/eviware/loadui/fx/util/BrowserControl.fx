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
package com.eviware.loadui.fx.util;

import javafx.scene.Cursor;
import javafx.scene.layout.Stack;
import javafx.scene.control.ScrollView;
import javafx.ext.swing.SwingComponent;
import javafx.util.Math;

import java.awt.Dimension;

import com.eviware.loadui.util.browser.JSBrowserComponent;
import java.beans.PropertyChangeListener;

public class BrowserControl extends ScrollView {
	override var fitToWidth = true;
	//override var fitToHeight = true;
	
	var browser:JSBrowserComponent;
	var stack:Stack;
	
	public var url:String on replace {
		browser.setUrl( url );
	}
	
	override var height on replace {
		stack.height = Math.max( stack.height, height );
	}
	
	postinit {
		browser = new JSBrowserComponent( url );
		browser.addPropertyChangeListener( Listener {} );
		
		node = stack = Stack {
			override var width on replace {
				browser.setPreferredSize( new Dimension( width, height ) );
			}
			
			override var height on replace {
				browser.setPreferredSize( new Dimension( width, height ) );
			}
			
			content: SwingComponent.wrap( browser )
		}
	}
}

class Listener extends PropertyChangeListener {
	override function propertyChange( event ) {
		if( JSBrowserComponent.CURSOR.equals( event.getPropertyName() ) ) {
			cursor = if( (event.getNewValue() as java.awt.Cursor).getType() == java.awt.Cursor.HAND_CURSOR ) Cursor.HAND else null;
		} else if( JSBrowserComponent.PAGE_HEIGHT.equals( event.getPropertyName() ) ) {
			stack.height = event.getNewValue() as Number;
		}
	}
}