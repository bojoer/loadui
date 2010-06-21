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
*MenuItem.fx
*
*Created on feb 11, 2010, 16:21:38 em
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Resizable;

package var selectedMenuItem:MenuItem = null;

/**
 * Base class for items that should be displayed i a PopupMenu.
 *
 * @author dain.nilsson
 */
public abstract class MenuItem extends CustomNode, Resizable {
	/**
	 * True if this MenuItem is selectable.
	 */
	public var selectable = true;

	/**
	 * True if this MenuItem is currently highlighted.
	 */
	public-read def selected = bind selectedMenuItem == this on replace {
		if( selected ) menu
	}
	
	/**
	 * The PopupMenu that this MenuItem belongs to.
	 */
	package public-read var menu:Node;
	
	/**
	 * Selects the MenuItem.
	 */
	public function select():Void {
		selectedMenuItem = this;
		menu.requestFocus();
	}
	
	override var onMouseEntered = function( e:MouseEvent ) {
		if( selectable )
			select();
	}
	
	override var onMouseExited = function( e:MouseEvent ) {
		if( selected )
			selectedMenuItem = null;
	}
}
