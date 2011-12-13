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
import javafx.ext.swing.SwingComponent;

import java.awt.Dimension;

import com.eviware.loadui.util.browser.BrowserComponent;
import com.eviware.loadui.util.browser.Browser.CursorListener;

public class BrowserControl extends Stack {
	var browser:BrowserComponent;
	
	public var url:String on replace {
		browser.setUrl( url );
	} 
	
	override var width on replace {
		browser.setPreferredSize( new Dimension( width, height ) );
	}
	
	override var height on replace {
		browser.setPreferredSize( new Dimension( width, height ) );
	}
	
	postinit {
		browser = new BrowserComponent( url );
		content = SwingComponent.wrap( browser );
		browser.addCursorListener( Listener {} );
	}
}

class Listener extends CursorListener {
	override function handleCursorChanged( newCursor ) {
		cursor = if( newCursor.getType() == java.awt.Cursor.HAND_CURSOR ) Cursor.HAND else null;
	}
}