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

import javafx.scene.layout.VBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class TreeLevelNode extends VBox {
	public-init var level:TreeSelectorLevel;
	
	public-init var target:Object on replace {
		content = [
			Label { text: "{target}" },
			for( childIndex in [0..level.selector.treeModel.getChildCount( target ) - 1] ) {
				def child = level.selector.treeModel.getChild( target, childIndex );
				ValueNode { treeNode: this, text: "{child}", value: child }
			}
		];
	}
	
	package function deselectAll():Void {
		for( node in content[c|c instanceof ValueNode] ) {
			(node as ValueNode).selected = false;
		}
	}
}