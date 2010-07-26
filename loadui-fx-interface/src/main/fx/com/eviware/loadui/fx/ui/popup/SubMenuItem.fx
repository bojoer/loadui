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
*SubMenuItem.fx
*
*Created on feb 11, 2010, 17:02:47 em
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;

import com.eviware.loadui.fx.ui.resources.MenuArrow;

/**
 * A MenuItem that opens another PopupMenu when activated.
 *
 * @author dain.nilsson
 */
public class SubMenuItem extends MenuItem {
	/**
	 * The label to display for this MenuItem.
	 */
	public var text:String;
	
	/**
	 * The PopupMenu to open upon being activated.
	 */
	public var submenu: PopupMenu;
	
	/**
	 * Expands the submenu.
	 */
	public function expand():Void {
		if( submenu.isOpen ) {
			while( PopupMenu.topMenu != submenu )
				PopupMenu.topMenu.close();
		} else {
			def bis = localToScene(layoutBounds);
			
			submenu.layoutX = if( bis.maxX + submenu.layoutBounds.width < scene.width )
				bis.maxX
			else if( bis.minX - submenu.layoutBounds.width >= 0 )
				bis.minX - submenu.layoutBounds.width
			else
				0;
			
			submenu.layoutY = if( bis.minY - 5 + submenu.layoutBounds.height < scene.height )
				bis.minY - 5
			else
				scene.height - submenu.layoutBounds.height;
			
			while( PopupMenu.topMenu != menu )
				PopupMenu.topMenu.close();
			submenu.open();
			submenu.items[0].select();
		}
	}
	
	def smi = bind selectedMenuItem on replace {
		if( submenu.isOpen and smi.menu == menu and smi != this ) {
			submenu.close();
		}
	}
	
	def label = Text {
		content: bind text
		textOrigin: TextOrigin.TOP
		fill: bind if( disabled ) Color.GRAY else if( selected or submenu.isOpen ) Color.WHITE else Color.BLACK
		x: 5
		y: 4
	}
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: bind if( selected or submenu.isOpen ) Color.web("#3e5fc3") else Color.WHITE
					width: bind width
					height: bind height
				}, MenuArrow {
					fill: bind if( selected or submenu.isOpen ) Color.WHITE else Color.BLACK
					layoutX: bind width - 10
					layoutY: bind height / 2
				}, label
			]
			onMouseClicked: function( e:MouseEvent ) {
				expand();
			}
			onMouseEntered: function( e:MouseEvent ) {
				expand();
			}
		}
	}
	
	override function getPrefHeight( width:Float ) {
		label.layoutBounds.height + 6
	}
	
	override function getPrefWidth( height:Float ) {
		label.layoutBounds.width + 20
	}
}
