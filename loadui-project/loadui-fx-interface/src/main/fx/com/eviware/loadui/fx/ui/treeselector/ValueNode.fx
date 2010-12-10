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
package com.eviware.loadui.fx.ui.treeselector;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.LayoutInfo;

public class ValueNode extends CheckBox {
	public-init var treeNode:TreeLevelNode;
	public-init var value:Object;
	
	override var selected = false on replace {
		if( treeNode.level.selector.treeModel.isLeaf( value ) ) {
			if( selected ) {
				treeNode.level.selector.onSelect( value );
			} else {
				treeNode.level.selector.onDeselect( value );
			}
		} else {
			if( selected and not treeNode.level.selector.allowMultiple ) {
				for( node in treeNode.content[c|c instanceof ValueNode] ) {
					if( node != this ) {
						(node as ValueNode).selected = false;
					}
				}
			}
			def nextLevel = treeNode.level.selector.getNextLevel( treeNode.level );
			if( selected ) {
				nextLevel.addChildrenFor( value );
			} else {
				nextLevel.removeChildrenFor( value );
			}
		}
	}
}
