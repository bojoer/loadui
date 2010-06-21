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
*TabPanel.fx
*
*Created on feb 23, 2010, 10:56:50 fm
*/

package com.eviware.loadui.fx.ui.tabs;

import javafx.util.Math;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Container;
import javafx.scene.layout.Stack;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.effect.InnerShadow;


/**
 * A resizable panel capable of containing multiple tabs. 
 *
 * @author dain.nilsson
 */
public class TabPanel extends Container {
	var mWidth:Number;
	var mHeight:Number;
	var tabBar:HBox;
	var lab:Text;
	def tabGroup = Group {
	    layoutY: -9
	    content: [ lab = Text { 
	                   layoutY: 12
	                   layoutX: 2
	                   content: "Settings"
	                   font: Font.font("Arial", 11)
	                   fill: Color.web("#4d4d4d")
                   },
                   tabBar = HBox {
                       layoutX: lab.boundsInParent.maxX + 45
                       content: []                    
                      spacing: 14
                  }]
	}
	
	/**
	 * The tabs contained in the TabPanel.
	 */
	public var tabs:Tab[] on replace {
		mWidth = 0;
		mHeight = 0;
		for( tab in tabs ) {
		    var btn:TabButton;
			tab.panel = this;
			mWidth = Math.max( mWidth, tab.content.layoutBounds.width );
			mHeight = Math.max( mHeight, tab.content.layoutBounds.height );
			insert btn = TabButton { text: tab.label, action: function() { selected = tab } } into tabBar.content;
			tab.onUnselect = function() {
			    btn.selected = false;
			}
		}
		selected = tabs[0];
		(tabBar.content[0] as TabButton).selected = true;
	}
	
	var innerFrame = Rectangle {
                           arcWidth: 5
                           arcHeight: 5
                           height: 280
                           width: 500 - 18 - 18// + 4
                           fill: Color.web("#e4e4e4")
                           effect: InnerShadow {
	                           radius: 5
										color: Color.web("#999999")
									}
                       };
                       
	override var content = bind [ tabGroup, 
						           innerFrame,
						           selected.content ];
	
	/**
	 * The currently selected tab.
	 */
	public var selected:Tab on replace oldTab {
		selected.onSelect();
		for( tab in tabs[t| t != selected])
		  tab.onUnselect();
	}
	
	override function doLayout() {
		resizeNode( selected.content, mWidth, mHeight );
		resizeNode( innerFrame, selected.content.boundsInParent.width + 20, selected.content.boundsInParent.height + 20);
        positionNode( innerFrame, -2, tabGroup.boundsInLocal.height + 15);
		positionNode( selected.content, 9, tabGroup.boundsInLocal.height + 38);
	}
	
	def prefHeight = bind lazy tabBar.boundsInLocal.height + mHeight;
	override function getPrefHeight( width:Float ) {
		prefHeight;
	}
	
	def prefWidth = bind lazy Math.max( tabBar.layoutBounds.width, mWidth );
	override function getPrefWidth( height:Float ) {
		prefWidth;
	}
}
