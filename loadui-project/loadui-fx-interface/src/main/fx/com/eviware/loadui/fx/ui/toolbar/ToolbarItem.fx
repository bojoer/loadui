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
/*
*ToolbarItem.fx
*
*Created on mar 11, 2010, 13:22:51 em
*/

package com.eviware.loadui.fx.ui.toolbar;

import javafx.scene.CustomNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;

/**
 * An item displayed in a Toolbar.
 *
 * @author dain.nilsson
 */
public class ToolbarItem extends BaseNode, Draggable, TooltipHolder {
	
	/**
	 * The icon to show in the Toolbar for this ToolbarItem.
	 */
	public-init protected var icon:Image;
	
	/**
	 * The label for this ToolbarItem.
	 */
	public var label:String;

	/**
	 * The label color for this ToolbarItem.
	 */
	public var textFill: Paint = Color.web("#9c9c9c");
		
	/**
	 * The category to place this ToolbarItem in.
	 */
	public-init protected var category:String = "misc";
	
	override var revert = false;
	
	override function create() {
		ImageView { image: icon }
	}
	
	override function toString():String {
		label
	}
}
