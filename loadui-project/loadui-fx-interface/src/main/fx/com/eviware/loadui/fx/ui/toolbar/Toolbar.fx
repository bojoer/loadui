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
import javafx.scene.control.Label;
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
import com.eviware.loadui.fx.osgi.ToolbarItemManager;

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

import com.eviware.loadui.fx.ui.popup.TooltipHolder;

/**
 * The main toolbar component for the LoadUI Controller
 * 
 * @author nenad.ristic
 * @author dain.nilsson
 * @author henrik.olsson
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
	
	/** Comparator for ordering groups */
	public var groupOrder: Comparator = GroupOrder{};
	
	/** Comparator for ordering items inside group */
	public var itemOrder: Comparator = ItemOrder{};
	
	/** Toolbar title */
	public var toolbarTitle: String = "Components";
	
	/** If specified, link pointing to the specified URL will be added as the last item in the toolbar */
	public var linkURL: String = null;
	
	/** Label of the link that will be added as the last item in the toolbar. If not specified, link URL will be used instead */
	public var linkLabel: String = null;

	/** The height of each item group */
	public var groupHeight = 110;
	
	/** The width of the toolbar */
	public var preferredWidth = 90; //100
	
	/** The upper margin of each item group */
	public var groupTopMargin = 10;
	
	public var groupLeftMargin = 10;
	
	public override var width = 112;

	public function addItem( item:ToolbarItemNode ) {
		def group = item.category.toUpperCase();
		
		if( not itemGroups.containsKey( group ) ) {
			def newGroup = ToolbarItemGroup { category: group, expandedGroup: expandedGroup, groupHeight: groupHeight, topMargin: groupTopMargin, leftMargin: groupLeftMargin };
			itemGroups.put( group, newGroup );
			items = Sequences.sort( [ items, newGroup ], groupOrder ) as Node[];
		}
		
		def itemGroup = itemGroups.get( group ) as ToolbarItemGroup;
		
		itemGroup.items = Sequences.sort( [ itemGroup.items, item ], itemOrder ) as ToolbarItemNode[];
	}
	
	public function removeItem( item:ToolbarItemNode ) {
		def itemGroup = itemGroups.get( item.category.toUpperCase() ) as ToolbarItemGroup;
		if( itemGroup != null ) {
			delete item from itemGroup.items;
			
			if( sizeof itemGroup.items == 0) {
				itemGroups.remove( itemGroup.category );
				delete itemGroup from items;
			}
		}
	}
	
	public-read var hidden = false;
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

	override var itemsPerPage = bind ( height - 100 ) / groupHeight as Integer;

	def realHeight = bind 95 + groupHeight * Math.min( actualItemsPerPage, sizeof items );
	
	def expandedHolder:Group = Group {
		layoutY: 68
		layoutX: bind -translateX
	}
	def expandedGroup = ToolbarExpander { expandedHolder: expandedHolder, groupHeight: groupHeight, topMargin: groupTopMargin, groupLeftMargin: groupLeftMargin };
	
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
					content: toolbarTitle
					fill: bind textFill 
					font: Font.font("Amble", FontWeight.BOLD, 10);
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
		preferredWidth
	}
	
	override function getPrefHeight( width:Float ) {
		realHeight
	}
	
	function buildContent():Group {
		var yOffset = 0;
		var withSpacers:Node[];
		for( n in displayedItems ) {
			n.layoutY = yOffset;
			insert n into withSpacers;
			insert FXDNode {
				url: bind "{__ROOT__}{hrUrl}"
				scaleX: bind width
				translateX: bind width / 2
				layoutY: yOffset + groupHeight - 15
			} into withSpacers;
			
			yOffset += groupHeight;
		}
		delete withSpacers[sizeof withSpacers - 1];
		
		Group { content: withSpacers }
	}
	
	postinit {
		if(linkLabel == null){
			linkLabel = linkURL;
		}
		if(linkLabel != null){
			def link = HyperlinkItem{
				label: bind linkLabel, 
				url: bind linkURL
			}
			// put \uffff as first character to ensure this item will always be the last in the list 
			itemGroups.put( "\uffff{link.label}", link );
			items = Sequences.sort( [ items, link ], groupOrder ) as Node[];
		}
		
		ToolbarItemManager.registerToolbar( this );
	}	

}

/**
 * Hyperlink. Used as the last item in the toolbar.
 *
 * @author predrag.vucetic
 */
public class HyperlinkItem extends BaseNode, TooltipHolder {

	override var styleClass = "toolbar-hyperlink";
	
	public var label: String;
	
	public var url: String;
	
	override var tooltip: String = bind url;
	
	def contentGroup: Group = Group {
		content: [
			Label {
				layoutX: 13
				layoutY: 12
				text: bind label
				cursor: Cursor.HAND
				onMouseClicked: function(e: MouseEvent): Void {
					openURL(url);
				}
				styleClass: "toolbar-hyperlink"
			}
		]
	}
	
	override function create() {
		Group {
			layoutY: -12
			content: contentGroup
		}
	}
	
	override function toString():String {
		"{label} ({url})"
	}

}

