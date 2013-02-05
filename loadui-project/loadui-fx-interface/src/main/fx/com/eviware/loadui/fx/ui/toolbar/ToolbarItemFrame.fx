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
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.geometry.VPos;

import com.eviware.loadui.fx.ui.dnd.DraggableFrame;

/**
 * A holder for ToolbarItemNodes, which provides a DraggableFrame with a placeholder, and displays the ToolbarItemNodes label.
 *
 * @author dain.nilsson
 */
public class ToolbarItemFrame extends CustomNode {
	/**
	 * A ToolbarItemNode to place inside the ToolbarItemFrame.
	 */
	public var item:ToolbarItemNode;
	
	public var font:Font = Font.font("Amble", 10 );
	
	public var leftMargin:Integer = 13;
	
	public var showLabels = true;
	
	override function create() {
		Group {
			layoutX: leftMargin
			layoutY: 32
			content: [
				DraggableFrame {
					draggable: bind item
					placeholder: bind item.placeholder
				}, if(showLabels) Label {
					height: 16
					layoutY: 52
					width: bind 80
					text: bind item.label
					vpos: VPos.CENTER
					//textOrigin: TextOrigin.TOP
					textWrap: true
					textFill: bind item.textFill
					font: bind font
				} else null
			]
		}
	}
}
