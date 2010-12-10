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
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.CheckBox;
import javafx.util.Sequences;

import javax.swing.tree.TreeModel;

/**
 * Allows the user to select leaf nodes in a TreeModel.
 *
 * @author dain.nilsson
 */
public class CascadingTreeSelector extends HBox {
	public-init var allowMultiple = false;
	
	public var onSelect: function( target:Object ):Void;
	public var onDeselect: function( target:Object ):Void;
	
	public-init var treeModel:TreeModel on replace {
		def rootLevel = TreeSelectorLevel { selector: this };
		content = rootLevel;
		rootLevel.addChildrenFor( treeModel.getRoot() );
	}
	
	package function getNextLevel( level:TreeSelectorLevel ):TreeSelectorLevel {
		def index = Sequences.indexByIdentity( content, level );
		if( sizeof content > (index+1) ) {
			content[index+1] as TreeSelectorLevel
		} else {
			def nextLevel = TreeSelectorLevel { selector: this };
			insert nextLevel into content;
			nextLevel
		}
	}
	
	package function removeLevel( level:TreeSelectorLevel ):Void {
		def index = Sequences.indexByIdentity( content, level );
		if( index > 0 ) {
			content = content [0..index-1];
		}
	}
}