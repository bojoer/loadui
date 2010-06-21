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
/*
*SeparatorMenuItem.fx
*
*Created on feb 12, 2010, 11:43:49 fm
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * A separator for a PopupMenu. Displays a horizontal line. 
 *
 * @author dain.nilsson
 */
public class SeparatorMenuItem extends MenuItem {
	override function create() {
		Group { 
			 content: [
			 	Rectangle {
			 		fill: Color.TRANSPARENT
			 		width: bind width
			 		height: bind height
				}, Line {
					endX: bind width
					startY: 5
					endY: 5
					stroke: Color.GRAY
				}
			]
		}
	}
	
	override var selectable = false;
	
	override function getPrefHeight( width:Float ) { 10 }
	
	override function getPrefWidth( width:Float ) { 10 }
}
