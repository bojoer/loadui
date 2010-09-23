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
 *  loadUI, copyright (C) 2009 eviware.com 
 *
 *  loadUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  loadUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */



package com.eviware.loadui.fx.ui.toolbar;

import javafx.scene.CustomNode;
import javafx.scene.Cursor;
import javafx.scene.layout.Resizable;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.effect.Glow;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.image.ImageView;
import javafx.geometry.Point2D;
import javafx.animation.transition.TranslateTransition;
import javafx.fxd.FXDNode;
import javafx.util.Sequences;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import javafx.util.Math;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.pagination.Pagination;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;

//import org.jfxtras.animation.wipe.XWipePanel;
import com.eviware.loadui.fx.ui.XWipePanel;
import org.jfxtras.animation.wipe.SlideWipe;
import org.jfxtras.scene.shape.MultiRoundRectangle;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Panel;
import javafx.scene.layout.Stack;
import javafx.scene.control.Label;
import java.util.Comparator;
import javafx.util.Sequences;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.eviware.loadui.api.component.categories.*;

public def GROUP_HEIGHT = 126;


//TODO: Move the ordering to an xml file
class GroupOrder extends Comparator {



	//Used for ordering the groups (They will appear in the reverse order).
	def groupOrder:String[] = [
		"AGENTS",
		"PROJECTS",
		
		MiscCategory.CATEGORY.toUpperCase(),
		OutputCategory.CATEGORY.toUpperCase(),
		SchedulerCategory.CATEGORY.toUpperCase(),
		FlowCategory.CATEGORY.toUpperCase(),
		AnalysisCategory.CATEGORY.toUpperCase(),
		RunnerCategory.CATEGORY.toUpperCase(),
		GeneratorCategory.CATEGORY.toUpperCase(),
		"TESTCASES"
	];

	public override function compare(o1, o2) {
			def index1 = Sequences.indexOf(groupOrder, o1.toString().toUpperCase());
			def index2 = Sequences.indexOf(groupOrder, o2.toString().toUpperCase());
	
			if (index1 == index2 )
				o1.toString().compareToIgnoreCase(o2.toString())
			else
				index2-index1;
			}
}

class ItemOrder extends Comparator {
	//Used for ordering the items
	def loadGeneratorOrder:String[] = [ "FIXED RATE", "VARIANCE", "RANDOM", "RAMP", "VIRTUAL USERS", "FIXED LOAD" ];
	def analysisOrder:String[] = [  "STATISTICS", "ASSERTION"  ];
	def flowOrder:String[] = [ "SPLITTER", "DELAY" ];
	    	
	public override function compare(o1, o2) {
	    def t1:ToolbarItem = o1 as ToolbarItem;
	    def t2:ToolbarItem = o2 as ToolbarItem;
	    
	    if (t1.category.equalsIgnoreCase("Generators") and t2.category.equalsIgnoreCase("Generators")) {
			var index1 = Sequences.indexOf(loadGeneratorOrder, t1.label.toUpperCase());
	    	var index2 = Sequences.indexOf(loadGeneratorOrder, t2.label.toUpperCase());
	        
	    	if (not (index1 == -1 or index2 == -1))
	    		 return index1-index2;
	    }
	
	    
	    if (t1.category.equalsIgnoreCase("Analysis") and t2.category.equalsIgnoreCase("Analysis")) {
	    	var index1 = Sequences.indexOf(analysisOrder, t1.label.toUpperCase());
	    	var index2 = Sequences.indexOf(analysisOrder, t2.label.toUpperCase());
	    	        
	    	if (not (index1 == -1 or index2 == -1))
	    		return index1-index2;
	    }
	    	    
	   	if (t1.category.equalsIgnoreCase("Flow") and t2.category.equalsIgnoreCase("Flow")) {
	   		var index1 = Sequences.indexOf(flowOrder, t1.label.toUpperCase());
	   		var index2 = Sequences.indexOf(flowOrder, t2.label.toUpperCase());
	   		        
	   		if (not (index1 == -1 or index2 == -1))
	   			return index1-index2;
	   	}
	    
	    return o1.toString().compareTo(o2.toString());
	        	



	}

}
	
/**
 * The main toolbar component for the LoadUI Controller
 * 
 * @author nenad.ristic
 * @author dain.nilsson
 */
public class Toolbar extends CustomNode, Resizable, Pagination {

	/** Url of active up arrow (CSS property: up-arrow-active-url) */
	public var upArrowActiveUrl: String = "images/small_arrow_up.fxz";
	
	/** Url of inactive up arrow (CSS property: up-arrow-inactive-url) */
	public var upArrowInactiveUrl: String = "images/small_arrow_up_inactive.fxz";
	
	/** Url of active down arrow (CSS property: down-arrow-active-url) */
	public var downArrowActiveUrl: String = "images/small_arrow_down.fxz";
	
	/** Url of inactive down arrow (CSS property: down-arrow-inactive-url) */
	public var downArrowInactiveUrl: String = "images/small_arrow_down_inactive.fxz";
	
	/** Main component container background fill (CSS property: background--fill) */
	public var backgroundFill: Paint = Color.web("#565656");
	
	/** Main component container stroke fill (CSS property: background-stroke-fill) */
	public var backgroundStrokeFill: Paint = Color.web("#4e4e4e");
	
	/** Toolbar title text fill (CSS property: text-fill) */
	public var textFill: Paint = Color.web("#303030");
	
	/** Background fill of rectangle where title text is written (CSS property: text-background-fill) */
	public var textBackgroundFill: Paint = Color.web("#4f4f4f");
	
	/** Fill of toolbar up border (CSS property: border-up-fill) */
	public var borderUpFill: Paint = Color.web("#717171");
	
	/** Fill of toolbar right and bottom border (CSS property: border-bottom-right-fill) */
	public var borderBottomRightFill: Paint = Color.web("#484848");
	
	/** Fill of toolbar right and bottom border stroke (CSS property: border-bottom-right-stroke-fill) 
	 *  This is beacuse righ bottom border is made of rectangle which has both background fill and 
	 *  stroke defined  	
	 */
	public var borderBottomRightStrokeFill: Paint = Color.web("#434343");
	
	/** Shadow fill which appears under bottom border (CSS property: shadow-fill) */
	public var shadowFill: Paint = Color.web("#000000");
	
	/** Url of toolbar expand arrow (CSS property: expand-arrow-url) */
	public var expandArrowUrl: String = "images/double_arrows.fxz";
	
	/** Url of horizontal rule which separates components (CSS property: hr-url) */
	public var hrUrl: String = "images/component_bar_hr.fxz";
	

	

	public function addItem( item:ToolbarItem ) {
		def group = item.category.toUpperCase();
		
		if( not itemGroups.containsKey( group ) ) {
			def newGroup = ToolbarItemGroup { category: group, expandedGroup: expandedGroup };
			itemGroups.put( group, newGroup );
			content = Sequences.sort( [ content, newGroup ], GroupOrder{ 
			
			}) as Node[];
		}
		
		def itemGroup = itemGroups.get( group ) as ToolbarItemGroup;
		
		itemGroup.items = Sequences.sort( [ itemGroup.items, item ], ItemOrder{
		
		} ) as ToolbarItem[];
	}
	
	public function removeItem( item:ToolbarItem ) {
		def itemGroup = itemGroups.get( item.category.toUpperCase() ) as ToolbarItemGroup;
		if( itemGroup != null ) {
			delete item from itemGroup.items;
			
			if( sizeof itemGroup.items == 0) {
				itemGroups.remove( itemGroup );
				delete itemGroup from content;
			}
		}
	}
	
	var hidden = false;
	def itemGroups:Map = new HashMap();
	var mainToolbar:Group;

	def slideTimeline = TranslateTransition {
		node: this
		duration: 100ms
	}

	var wipe:XWipePanel;
	
	def slideWipe = SlideWipe {
		time: 200ms
	}

	def displayedGroup = bind wipe.content[0] as Group;
	var oldGroup:Group;
	var wiping = false;
	override var onDisplayChange = function( oldContent:Node[], direction:Integer ):Void {
		expandedGroup.group = null;
		
		def wipeDir = if( direction > 0 ) SlideWipe.BOTTOM_TO_TOP
			else if( direction < 0 ) SlideWipe.TOP_TO_BOTTOM
			else -1;
		if( wipeDir != -1 ) {
			oldGroup = displayedGroup;
			slideWipe.direction = wipeDir;
			wiping = true;
			wipe.next( buildContent() );
		} else {
			displayedGroup.content = null;
			wipe.content = buildContent();
		}
	}

	override var width = 112;

	override var itemsPerPage = bind ( height - 100 ) / GROUP_HEIGHT as Integer;

	def realHeight = bind 95 + GROUP_HEIGHT * Math.min( actualItemsPerPage, sizeof content );
	
	def expandedHolder:Group = Group {
		layoutY: 68
		layoutX: bind -translateX
	}
	def expandedGroup = ToolbarExpander { expandedHolder: expandedHolder };
	
	def modalLayer = Rectangle {
		width: bind scene.width
		height: bind scene.height
		fill: Color.TRANSPARENT
		onMousePressed: function( e:MouseEvent ) {
			if( not expandedGroup.contains( expandedGroup.sceneToLocal( Point2D { x: e.sceneX, y: e.sceneY } ) ) )
				expandedGroup.group = null;
		}
	}

	def glow = Glow { level: .5 };
	
	override function create(): Node {
		def upArrow:Node = Group {
			layoutX: bind ( width - upArrow.layoutBounds.width ) / 2
			layoutY: 6
			content: [
				FXDNode {
					url: bind "{__ROOT__}{upArrowActiveUrl}"
					visible: bind page > 0
					effect: bind if( upArrow.hover ) glow else null
				}, FXDNode {
					url: bind "{__ROOT__}{upArrowInactiveUrl}"
					visible: bind page == 0
				}
			]
		}
		
		def downArrow:Node = Group {
			layoutY: bind 6
			layoutX: bind ( width - downArrow.layoutBounds.width ) / 2
			content: [
				FXDNode {
					url: bind "{__ROOT__}{downArrowActiveUrl}"
					visible: bind page < numPages - 1
					effect: bind if( downArrow.hover ) glow else null
				}, FXDNode {
					url: bind "{__ROOT__}{downArrowInactiveUrl}"
					visible: bind page == numPages - 1
				}
			]
		}

		mainToolbar = Group {
			blocksMouse: true
    		content: [
    			Rectangle {
					fill: bind shadowFill
					height: bind 10
					width: bind width + 5
					translateX: -5
					translateY: bind realHeight - 8
					arcWidth: 10
					arcHeight: 10
				}, Rectangle {
					fill: bind borderBottomRightFill
					height: bind realHeight
					width: bind width + 5
					translateX: -5
					arcWidth: 10
					arcHeight: 10
					stroke: bind borderBottomRightStrokeFill 
				}, Rectangle {
					width: bind width + 5
					height: 10
					arcWidth: 10
					arcHeight: 10
					translateX: -5
					fill: bind borderUpFill
					clip: Rectangle {
						width: bind width + 5
						height: 3
					}
				}, Rectangle {
					fill: bind backgroundFill
					height: bind realHeight - 7
					width: bind width + 2
					translateX: -5
					translateY: 5
					arcWidth: 10
					arcHeight: 10
					stroke: bind backgroundStrokeFill
				}, Rectangle {
					fill: bind textBackgroundFill
					width: bind width - 2
					height: 20
					layoutY: 2
				}, Stack {
					layoutX: bind width - 30
					layoutY: 2
					cursor: Cursor.HAND
					content: [
						Rectangle {
							width: 30
							height: 20
							fill: Color.TRANSPARENT
						}, FXDNode {
							url: bind "{__ROOT__}{expandArrowUrl}"
							rotate: 90
							scaleY: bind if( hidden ) 1 else -1
						}
					]
					blocksMouse: true
					onMouseClicked: function( e:MouseEvent ) {
						slideTimeline.toX = if( hidden ) 0 else -( width - 25 );
						slideTimeline.playFromStart();
						hidden = not hidden;
					}
				}, Text {
					textOrigin: TextOrigin.TOP
					x: 6
					y: 6
					content: "Components"
					fill: bind textFill 
					font: Font.font("Arial", FontWeight.BOLD, 10);
				}, Group {
					layoutY: 26
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.PRIMARY and page > 0 and not wiping )
							page--;
					}
					blocksMouse: true
					content: [
						Rectangle {
							fill: Color.TRANSPARENT
							width: bind width
							height: 28
						}, upArrow
					]
				}, FXDNode {
					url: bind "{__ROOT__}{hrUrl}"
					scaleX: bind width
					layoutX: bind width / 2
					layoutY: 53
				}, wipe = XWipePanel {
					layoutY: 68
					width: bind width
					height: bind realHeight - 130
					wipe: slideWipe
					content: buildContent()
					action: function() {
						if( oldGroup != null )
							oldGroup.content = null;
						wiping = false;
					}
				}, expandedHolder, FXDNode {
					url: bind "{__ROOT__}{hrUrl}"
					scaleX: bind width
					layoutX: bind width / 2
					layoutY: bind realHeight - 42
				}, Group {
					layoutY: bind realHeight - 31
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.PRIMARY and page < numPages - 1 and not wiping )
							page++;
					}
					blocksMouse: true
					content: [
						Rectangle {
							fill: Color.TRANSPARENT
							width: bind width
							height: 28
						}, downArrow
					]
				}
			]
			onMouseWheelMoved: function( e:MouseEvent ) {
				if( e.wheelRotation > 0 and page < numPages - 1 and not wiping ) {
					page++;
				} else if( e.wheelRotation < 0 and page > 0 and not wiping ) {
					page--;
				}
			}
		}
	}
	
	override function getPrefWidth( height:Float ) {
		100
	}
	
	override function getPrefHeight( width:Float ) {
		realHeight
	}
	
	function buildContent():Group {
		var yOffset = 0;
		var withSpacers:Node[];
		for( n in displayedContent ) {
			n.layoutY = yOffset;
			insert n into withSpacers;
			insert FXDNode {
				url: bind "{__ROOT__}{hrUrl}"
				scaleX: bind width
				translateX: bind width / 2
				layoutY: yOffset + GROUP_HEIGHT - 15
			} into withSpacers;
			
			yOffset += GROUP_HEIGHT;
		}
		delete withSpacers[sizeof withSpacers - 1];
		
		Group { content: withSpacers }
	}
	
	

}



