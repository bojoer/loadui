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
package com.eviware.loadui.fx.ui.treeselector;

import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.CheckBox;
import javafx.geometry.HPos;
import javafx.util.Sequences;

import com.sun.javafx.scene.layout.Region;

import javax.swing.tree.TreeModel;

/**
 * Allows the user to select leaf nodes in a TreeModel.
 *
 * @author dain.nilsson
 */
public class CascadingTreeSelector extends Stack {
	public-init var allowMultiple = false;
	def bgHbox = HBox {
		nodeHPos: HPos.RIGHT
	}
	
	override var styleClass = "cascading-tree-selector";
	
	public-init var columnCount:Integer = 1 on replace {
		bgHbox.content = for( i in [1..columnCount] ) Region { styleClass: "cascading-tree-selector-cell", layoutInfo: LayoutInfo { width: 16, hfill: false } }
	}
	
	public var onSelect: function( target:Object ):Void;
	public var onDeselect: function( target:Object ):Void;
	
	def hbox = HBox {};
	
	init {
		content = [ bgHbox, hbox ];
	}
	
	public-init var treeModel:TreeModel on replace {
		def rootLevel = TreeSelectorLevel { selector: this, layoutInfo: LayoutInfo { width: bind width / columnCount, hfill: false, hgrow: Priority.NEVER } };
		hbox.content = rootLevel;
		rootLevel.addChildrenFor( treeModel.getRoot() );
	}
	
	package function getNextLevel( level:TreeSelectorLevel ):TreeSelectorLevel {
		def index = Sequences.indexByIdentity( hbox.content, level );
		if( sizeof hbox.content > (index+1) ) {
			hbox.content[index+1] as TreeSelectorLevel
		} else {
			def nextLevel = TreeSelectorLevel { selector: this, layoutInfo: LayoutInfo { width: bind width / columnCount, hfill: false, hgrow: Priority.NEVER } };
			insert nextLevel into hbox.content;
			nextLevel
		}
	}
	
	package function removeLevel( level:TreeSelectorLevel ):Void {
		def index = Sequences.indexByIdentity( hbox.content, level );
		if( index > 0 ) {
			hbox.content = hbox.content[0..index-1];
		}
	}
}