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
package com.eviware.loadui.fx.ui.treeselector;

import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;

public class TreeSelectorLevel extends ScrollView {
	public-init var selector:CascadingTreeSelector;

	public var showLabel = true;

	def vbox:VBox = VBox {
		spacing: 18
	}
	
	override var styleClass = "tree-selector-level";
	override var hbarPolicy = ScrollBarPolicy.NEVER;
	override var fitToWidth = true;
	override var pannable = false;
	
	init {
		node = vbox;
	}
	
	package function addChildrenFor( target:Object ):Void {
		insert TreeLevelNode { level: this, target: target, showLabel: bind showLabel } into vbox.content;
	}
	
	package function removeChildrenFor( target:Object ):Void {
		for( node in vbox.content ) {
			def treeNode = node as TreeLevelNode;
			if( treeNode.target == target ) {
				treeNode.deselectAll();
				delete treeNode from vbox.content;
				if( sizeof vbox.content == 0 ) {
					selector.removeLevel( this );
				}
				break;
			}
		}
	}
}