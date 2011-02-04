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
*TabDialog.fx
*
*Created on aug 11, 2010, 11:49:50 fm
*/

package com.eviware.loadui.fx.ui.tabs;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.InnerShadow;
import javafx.geometry.VPos;
import javafx.geometry.Insets;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;

public class TabDialog extends Dialog {
	def buttons:Container = HBox {
		styleClass: "tab-dialog"
		spacing: 14
		nodeVPos: VPos.CENTER
		layoutInfo: LayoutInfo { margin: Insets { left: 10, right: 10 } }
		content: Label { text: bind subtitle, layoutInfo: LayoutInfo { height: 40, width: 100 } }
	}
	var stack:Stack;
	var stackLayoutInfo:LayoutInfo;
	def toggleGroup = MyToggleGroup{};
	public-init var width:Number;
	public-init var height:Number;
	
	public var subtitle:String;
	
	public var selectedTab:Tab on replace oldTab {
		stack.content = [ stack.content[0], selectedTab.content ];
		oldTab.onUnselect();
		selectedTab.selected = true;
		selectedTab.onSelect();
		if( oldTab != null and selectedTab != null )
			moved = true;
	}
	
	public var tabs:Tab[] on replace {
		for( toggle in toggleGroup.toggles ) toggle.toggleGroup = null;
		
		buttons.content = [
			buttons.content[0],
			for( tab in tabs ) {
				ToggleButton {
					text: tab.label
					toggleGroup: toggleGroup
					value: tab
				}
			}
		];
		toggleGroup.selectedToggle = null;
		toggleGroup.selectedToggle = toggleGroup.toggles[0];
	}
	
	init {
		dialogContent.padding = null;
		dialogContent.content = [
			Rectangle {
				managed: false
				width: bind dialogPanel.width - 7
				height: 40
				x: -7
				y: -1
				fill: Color.rgb( 0xe7, 0xe7, 0xe7 )
			}, Rectangle {
				managed: false
				width: bind dialogPanel.width - 4
				height: 2
				x: -8
				y: -2
				fill: Color.rgb( 0, 0, 0, 0.3 )
			}, buttons, stack = Stack {
				layoutInfo: stackLayoutInfo = LayoutInfo { margin: Insets { left: 10, top: 15, right: 10, bottom: 5 } }
				padding: Insets { left: 20, top: 20, right: 20, bottom: 20 }
				content: [
					TabBorder {
						layoutInfo: LayoutInfo { vfill: true hfill: true, margin: Insets { left: -20, top: -20, right: -20, bottom: -20 } }
					}, selectedTab.content
				]
			}
		];
		
		if( FX.isInitialized( width ) ) {
			stackLayoutInfo.width = width;
		}
		
		if( FX.isInitialized( height ) ) {
			stackLayoutInfo.height = height;
		}
	}
}

class MyToggleGroup extends ToggleGroup {
	var selectOne = false;
	override var selectedToggle on replace oldVal {
		if( selectedToggle == null ) {
			//selectedTab = null;
			selectOne = true;
			FX.deferAction( requestSelect );
		} else {
			selectedTab = selectedToggle.value as Tab;
		}
	}
	
	function requestSelect() {
		if( selectOne ) {
			selectOne = false;
			if( sizeof toggles > 0 ) {
				selectedToggle = toggles[0];
			} else {
				selectedTab = null;
			}
		}
	}
}

class TabBorder extends Resizable, CustomNode {
	override function create():Node {
		Rectangle {
			width: bind width
			height: bind height
			fill: Color.rgb( 0xe4, 0xe4, 0xe4 )
			arcWidth: 5
			arcHeight: 5
			effect: InnerShadow {
				radius: 5
				color: Color.rgb( 0x99, 0x99, 0x99 )
			}
		}
	}
	
	override function getPrefHeight( width:Number ) { -1 }
	override function getPrefWidth( height:Number ) { -1 }
}