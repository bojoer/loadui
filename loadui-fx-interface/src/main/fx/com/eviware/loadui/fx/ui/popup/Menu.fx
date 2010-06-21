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
*Menu.fx
*
*Created on feb 16, 2010, 09:38:29 fm
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.Paints;

/**
 * A BaseNode which opens a PopupMenu when clicked. Optionally it may have a tooltip.
 *
 * @author dain.nilsson
 */
public class Menu extends BaseNode, MenuHolder, TooltipHolder {
	
	/**
	 * Prevents the creation of a highlighted square as the background when the menu is open. 
	 */
	public-init var noHighlight = false;
	
	override var blocksMouse = true;
	
	override var layoutBounds = bind group.layoutBounds;
	
	var group:Group;
	override function create() {
		group = if( not noHighlight ) Group {
			content: [
				Rectangle {
					width: bind contentNode.layoutBounds.width
					height: bind contentNode.layoutBounds.height
					layoutX: bind contentNode.layoutBounds.minX
					layoutY: bind contentNode.layoutBounds.minY
					fill: bind if( menuOpen ) Color.web("#3e5fc3") else Color.TRANSPARENT
				}, Rectangle {
					width: bind contentNode.layoutBounds.width
					height: bind contentNode.layoutBounds.height
					layoutX: bind contentNode.layoutBounds.minX
					layoutY: bind contentNode.layoutBounds.minY
					arcWidth: 10
					arcHeight: 10
					fill: Paints.MENU_HIGHLIGHT
					visible: bind contentNode.hover and Draggable.currentDraggable == null
				}, Group { content: bind contentNode }
			]
		} else Group { content: bind contentNode };
	}
}
