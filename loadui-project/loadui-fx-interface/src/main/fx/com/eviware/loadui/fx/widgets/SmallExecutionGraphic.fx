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
package com.eviware.loadui.fx.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import com.eviware.loadui.fx.ui.resources.PlayShape;

public class SmallExecutionGraphic extends Stack {
	
	public var running = false;
	
	def playShape = Group {
		layoutInfo: LayoutInfo { margin: Insets { left: 2, top: 2 } }
		visible: bind not running
		content: [
			PlayShape {
				layoutY: 1
				width: 5
				height: 6
				fill: Color.web("#b3b4b5")
			}, PlayShape {
				width: 5
				height: 6
				fill: Color.web("#6d6e71")
			}
		]
	}
	
	def stopShape = Group {
		visible: bind running
		content: [
			Rectangle {
				width: 6
				height: 6
				fill: Color.web("#5e5f60")
			}, Rectangle {
				layoutX: 1
				layoutY: 1
				width: 6
				height: 6
				fill: Color.web("#c3c5c7")
			}
		]
	}
	
	init {
		children = [ playShape, stopShape ];
	}
}