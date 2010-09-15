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
*ToolbarItemFrame.fx
*
*Created on mar 12, 2010, 15:22:47 em
*/

package com.eviware.loadui.fx.ui.toolbar;

import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.TextOrigin;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.ui.dnd.DraggableFrame;

/**
 * A holder for ToolbarItems, which provides a DraggableFrame with a placeholder, and displays the ToolbarItems label.
 *
 * @author dain.nilsson
 */
public class ToolbarItemFrame extends CustomNode {
	/**
	 * A ToolbarItem to place inside the ToolbarItemFrame.
	 */
	public var item:ToolbarItem;
	
	public var font:Font = Font.font( "Arial", 10 );
	
	//public-read var df: DraggableFrame;
	
	override function create() {
		Group {
			layoutX: 13
			layoutY: 32
			content: [
				//df = DraggableFrame {
				DraggableFrame {
					draggable: bind item
					placeholder: ImageView { image: bind item.icon }
				}, Text {
					y: 57
					wrappingWidth: 80
					content: bind item.label
					textOrigin: TextOrigin.TOP
					fill: bind item.textFill
					font: bind font
				}
			]
		}
	}
}
