/* 
 * Copyright 2011 eviware software ab
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

package com.eviware.loadui.fx.ui.menu;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.LayoutInfo;

def armedColor = Color.rgb( 0xb2, 0xb2, 0xb2 );
def unarmedColor = Color.rgb( 0x66, 0x66, 0x66 );
def shadowArmedColor = Color.TRANSPARENT;
def shadowUnarmedColor = Color.rgb( 0xb1, 0xb1, 0xb1 );

public class MenubarButton extends Button {
	public var shape:String;
	
	override var styleClass = "menubar-button";
	
	override var graphic = Group {
		content: [
			SVGPath { layoutX: 2, layoutY: 2, content: bind shape, fill: bind if( armed ) shadowArmedColor else shadowUnarmedColor },
			SVGPath { layoutX: 1, layoutY: 1, content: bind shape, fill: bind if( armed ) armedColor else unarmedColor },
		]
	}
	
	override var layoutInfo = LayoutInfo { width: 24, height: 24 }
}
