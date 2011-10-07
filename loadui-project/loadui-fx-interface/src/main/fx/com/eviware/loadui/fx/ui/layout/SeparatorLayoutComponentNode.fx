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
/*
*SeparatorLayoutComponentNode.fx
*
*Created on apr 14, 2010, 18:08:56 em
*/

package com.eviware.loadui.fx.ui.layout;

import com.eviware.loadui.api.layout.SeparatorLayoutComponent;
import javafx.scene.shape.Rectangle;
import org.jfxtras.scene.layout.XMigLayout;
import javafx.scene.layout.Panel;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;



import javafx.scene.paint.Color;


public class SeparatorLayoutComponentNode extends LayoutComponentNode {
	
	/**override var layoutBounds = bind lazy BoundingBox {
	  minX: mig.layoutBounds.minX
	  minY: mig.layoutBounds.minY
	  width:  width
	  height:  height
	}*/
	
	function constraints() {
		def separatorLayoutComponent = layoutComponent as SeparatorLayoutComponent;
		def existing = separatorLayoutComponent.getConstraints();
		
		if( existing != null and existing != "" )
			existing
		else if(separatorLayoutComponent.isVertical()) {
			"growy"
		} else {
			"newline, growx, spanx";
		}
	}
	
	override function create() {
		layoutInfo = XMigLayout.nodeConstraints( constraints() );
		
    	Rectangle {
    		width: bind width
    		height: bind height
    		fill: Color.rgb(0x93, 0x93, 0x93)
    	}
	}
	
	override function getPrefHeight( width:Float ) {
	    1
	}
	
	override function getPrefWidth( height:Float ) {
	    1
	}
}
