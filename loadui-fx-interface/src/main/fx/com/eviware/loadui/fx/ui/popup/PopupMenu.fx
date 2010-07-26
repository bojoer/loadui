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
*PopupMenu.fx
*
*Created on feb 11, 2010, 16:28:41 em
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.util.Math;
import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Container;
import javafx.scene.layout.Resizable;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.LayoutInfo;

import java.lang.RuntimeException;

/**
 * A Group which is used as an overlay to place PopupMenus in.
 * The Group should be positioned at 0,0 in the scene.
 */
public var overlay:Group;

var openMenus:PopupMenu[] = [] on replace oldVal {
	if( sizeof openMenus == 0 ) {
		delete menuGroup from overlay.content;
	} else if( sizeof oldVal == 0 ) {
		insert menuGroup into overlay.content;
	}
};
def menuGroup = Group { content: bind [ modalLayer, openMenus ] };
package def topMenu = bind openMenus[ sizeof openMenus - 1];

def modalLayer = Rectangle {
	fill: Color.TRANSPARENT
	width: bind overlay.scene.width
	height: bind overlay.scene.height
	//blocksMouse: true
	onMousePressed: function( e:MouseEvent ) {
		closeAll();
	}
}

/**
 * Closes all open PopupMenus.
 */
public function closeAll() {
	openMenus[0].close();
}


/**
 * A PopupMenu that holds MenuItems.
 *
 * @author dain.nilsson
 */
public class PopupMenu extends CustomNode {
	/**
	 * True if the PopupMenu is currently in an open state.
	 */
	public-read var isOpen = false;
	
	/**
	 * Invoked whenever the PopupMenu is opened, before it is displayed.
	 */
	public var onOpen: function():Void;
	
	public var minWidth: Number = 0;
	
	def rvbox = ResizingVBox {
		layoutX: 5
		layoutY: 5
		minWidth: bind minWidth - 10
	}
	
	/**
	 * The MenuItems displayed in this PopupMenu.
	 */
	public var items: MenuItem[] = [] on replace {
		for( item in items )
			item.menu = this;
		rvbox.content = items;
		rvbox.requestLayout();
	}
	
	init {
		if( not FX.isInitialized( overlay ) )
			throw new RuntimeException( "PopupMenu.overlay is not set!" );
	}
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: Color.WHITE
					strokeWidth: 1
					stroke: Color.GRAY
					width: bind rvbox.width + 10
					height: bind rvbox.height + 10
					blocksMouse: true
					visible: bind rvbox.height > 0
				}, rvbox
			]
		}
	}
	
	/**
	 * Opens the PopupMenu if it is not already open, so that it becomes visible. 
	 */
	public function open() {
		if( not isOpen ) {
			onOpen();
			isOpen = true;
			insert this into openMenus;
			requestFocus();
		}
	}
	
	/**
	 * Closes this PopupMenu and any other PopupMenus on top if this one.
	 */
	public function close():Void {
		if( isOpen ) {
			while( topMenu != this )
				topMenu.close();
			delete this from openMenus;
			isOpen = false;
			if( MenuItem.selectedMenuItem.menu == this )
				MenuItem.selectedMenuItem = null;
		}
	}
	
	override var onKeyPressed = function( e:KeyEvent ) {
		def current = MenuItem.selectedMenuItem;
		if( e.code == KeyCode.VK_ENTER ) {
			if( current instanceof ActionMenuItem )
				(current as ActionMenuItem).activate();
		} else if( isOpen and sizeof items > 0 ) {
			if( e.code == KeyCode.VK_UP ) {
				if( current == null ) {
					items[sizeof items - 1].select();
				} else if( current.menu == this ) {
					selectNext( true );
				}
			} else if( e.code == KeyCode.VK_DOWN ) {
				if( current == null ) {
					items[0].select();
				} else if( current.menu == this ) {
					selectNext( false );
				}
			} else if( current instanceof SubMenuItem ) {
				if( not (current as SubMenuItem).submenu.isOpen and e.code == KeyCode.VK_RIGHT ) {
					def sub = current as SubMenuItem;
					sub.expand();
					sub.submenu.items[0].select();
				}
			} else if( e.code == KeyCode.VK_LEFT or e.code == KeyCode.VK_ESCAPE ) {
				def p = openMenus[ Sequences.indexByIdentity( openMenus, this ) -1 ];
				close();
				if( p != null ) {
					p.requestFocus();
					for( i in p.items[x|x instanceof SubMenuItem] ) {
						if( (i as SubMenuItem).submenu == this ) {
							i.select();
							break;
						}
					}
				}
			}
		}
	}
	
	function selectNext( prev:Boolean ) {
		def current = MenuItem.selectedMenuItem;
		if( sizeof items > 1 ) {
			var i = Sequences.indexByIdentity( items, current );
			
			while( not items[i].selectable or items[i] == current ) {
				if( prev ) i-- else i++;
				if( i<0 ) i = sizeof items - 1;
				if( i> sizeof items - 1 ) i = 0;
			}
			
			items[i].select();
		}
	}
}

class ResizingVBox extends Container {

	public var minWidth: Number = 0 on replace {
		doLayout();
	}
	
	override function doLayout() {
		def resizables = content[n|n instanceof Resizable];
		
		for( r in resizables ) {
			def resizable = r as Resizable;
			width = Math.max( width, resizable.getPrefWidth( resizable.height ) );
		}
		width = Math.max( width, minWidth );
		
		var offsetY = 0.0;
		for( node in getManaged( content ) ) {
			if( node instanceof Resizable ) {
				def r = node as Resizable;
				r.width = width;
				r.height = r.getPrefHeight( width );
			}
			node.layoutY = offsetY;
			offsetY += node.layoutBounds.height;
		}
		
		height = offsetY;
	}
}
