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
*MenuHolder.fx
*
*Created on mar 11, 2010, 10:09:58 fm
*/

package com.eviware.loadui.fx.ui.popup;

import com.eviware.loadui.fx.ui.node.BaseMixin;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * Mixin class for giving BaseNodes a PopupMenu which opens upon clicking the node.
 *
 * @author dain.nilsson
 */
public mixin class MenuHolder extends BaseMixin {
	
	/**
	 * The popup menu to open.
	 */
	public var menu:PopupMenu;
	
	/**
	 * True if the menu is open, false if not.
	 */
	public-read var menuOpen = bind menu.isOpen on replace {
		if( menuOpen ) {
			def sb = (this as BaseNode).localToScene( (this as BaseNode).layoutBounds );
			closer.layoutX = sb.minX;
			closer.layoutY = sb.minY;
			closer.width = sb.width;
			closer.height = sb.height;
			insert closer into BaseNode.overlay.content;
		} else {
			delete closer from BaseNode.overlay.content;
		}
	}
	
	def closer = Rectangle {
		fill: Color.TRANSPARENT
		blocksMouse: true
	}
	
	init {
		(this as BaseNode).addMouseHandler( MOUSE_PRESSED, function( e:MouseEvent ) {
			if( not menuOpen and e.button == MouseButton.PRIMARY ) {
				def sb = (this as BaseNode).localToScene( (this as BaseNode).layoutBounds );
				menu.layoutX = if( sb.minX + menu.layoutBounds.width < (this as BaseNode).scene.width ) sb.minX
					else (this as BaseNode).scene.width - menu.layoutBounds.width;
				menu.layoutY = if( sb.maxY + menu.layoutBounds.height < (this as BaseNode).scene.height ) sb.maxY
					else (this as BaseNode).scene.height - menu.layoutBounds.height;
				menu.open();
			}
		} );
	}
}
