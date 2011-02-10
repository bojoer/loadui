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
package com.eviware.loadui.fx.ui.pagelist;

import javafx.util.Math;
import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Tile;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Label;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.fxd.FXDNode;
import javafx.scene.effect.Glow;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.pagination.Pagination;

//import org.jfxtras.animation.wipe.XWipePanel;
import com.eviware.loadui.fx.ui.XWipePanel;
import org.jfxtras.animation.wipe.SlideWipe;

/**
 * A Panel which holds other nodes, paginating and displaying a number of them.
 * Using the two arrow controls, the user can flip through the different pages.
 *
 * @author dain.nilsson
 */
public class PagelistControl extends CustomNode, Resizable, Pagination {
	
	/**
	 * The text to display above the items as a label.
	 */
	public var text:String = "";
	
	/**
	 * The width to give to each item.
	 */
	var itemWidth: Number = bind Math.max(tiles[0].tileWidth, 100);
	
	public-init var itemSpacing = 18;
	
	override var onDisplayChange = function( oldContent:Node[], direction:Integer ):Void {
		def wipeDir = if( direction < 0 ) SlideWipe.LEFT_TO_RIGHT
			else if( direction > 0 ) SlideWipe.RIGHT_TO_LEFT
			else -1;

		if( wipeDir != -1 ) {
			oldContainer = displayedContainer;
			slideWipe.direction = wipeDir;
			wiping = true;
			wipe.next( buildContainer() );
		} else {
			displayedContainer.content = null;
			wipe.content = buildContainer();
		}
	}
	
	def realWidth = bind Math.max( width, itemWidth + 200 );
	
	/**
	 * The width that is given to the actual contents.
	 */
	public-read def contentWidth = bind realWidth - 156 on replace {
		fixItemsPerPage();
	}
	
	override var items on replace {
		fixItemsPerPage();
	}
	
	function fixItemsPerPage():Void {
		var i = 1;
		while( ( i + 1 ) * ( itemWidth + itemSpacing ) < contentWidth + itemSpacing )
			i++;
		itemsPerPage = i;
	}
	
	def displayedGroup = bind wipe.content[0] as Group;
	def displayedContainer = bind displayedGroup.content[0] as Container;
	var wipe: XWipePanel;
	var slideWipe: SlideWipe;
	
	var leftarrow:Node;
	var rightarrow:Node;
	
	var wiping = false;

	var oldContainer:Container = null;
	
	function getTextForNode( node:Node ) {
		Label {
			layoutInfo: LayoutInfo{
				width: bind itemWidth
				height: 45
			}
			text: node.toString()
			textFill: Color.web("#aeaeae")
			textWrap: true
			font: Font { size: 10 }
			vpos: VPos.TOP
		}
	}
	
	var tiles:Tile[];
	function buildContainer():Group {
		Group {
			layoutY: 50
			content: tiles = [
				Tile {
					width: bind contentWidth + itemSpacing
					hgap: bind itemSpacing
					nodeVPos: VPos.BOTTOM
					nodeHPos: HPos.LEFT
					content: displayedItems
				}, Tile {
					layoutY: bind height - 95
					width: bind contentWidth + itemSpacing
					hgap: bind itemSpacing
					nodeVPos: VPos.TOP
					nodeHPos: HPos.LEFT
					content: for( n in displayedItems ) getTextForNode( n )
				}
			]
		}
	}
	
	public var glossFill = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.TRANSPARENT }
			Stop { offset: 0.5, color: Color.WHITE }
			Stop { offset: 1, color: Color.TRANSPARENT }
		]
	}
	
	public var fill = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0 , color: Color.BLACK },
			Stop { offset: 0.7 , color: Color.web("#373737") },
			Stop { offset: 1 , color: Color.BLACK }
		]
	}
	
	public var fillOpacity = 0.5;
	
	public var leftArrowActive: String = "{__ROOT__}images/leftarrow_active.fxz";
	public var leftArrowInactive: String = "{__ROOT__}images/leftarrow_inactive.fxz";
	public var rightArrowActive: String = "{__ROOT__}images/rightarrow_active.fxz";
	public var rightArrowInactive: String = "{__ROOT__}images/rightarrow_inactive.fxz";
	
	override function create():Node {
		def glow = Glow { level: .5 };
		
		Group {
			content: [
				Group {
					opacity: bind fillOpacity
					content: [
						Rectangle {
							width: 2
							height: bind height
							fill: bind glossFill
						}, Rectangle {
							width: 2
							height: bind height
							x: bind realWidth - 2
							fill: bind glossFill
						}, Rectangle {
							opacity: bind fillOpacity
							width: bind realWidth
							height: bind height
							arcWidth: 15
							arcHeight: 15
							fill: bind fill
						}, leftarrow = Group {
							layoutX: 30
							layoutY: bind ( height - leftarrow.layoutBounds.height ) / 2
							content: [
								FXDNode {
									url: bind leftArrowActive
									visible: bind page > 0
									effect: bind if( leftarrow.hover ) glow else null
								}, FXDNode {
									url: bind leftArrowInactive
									visible: bind page <= 0
								}
							]
							blocksMouse: false
							onMousePressed: function( e:MouseEvent ) { if( e.primaryButtonDown and not wiping and page > 0 ) page-- }
						}, rightarrow = Group {
							layoutX: bind realWidth - ( 30 + rightarrow.layoutBounds.width )
							layoutY: bind ( height - rightarrow.layoutBounds.height ) / 2
							content: [
								FXDNode {
									url: bind rightArrowActive
									visible: bind page < numPages - 1
									effect: bind if( rightarrow.hover ) glow else null
								}, FXDNode {
									url: bind rightArrowInactive
									visible: bind page >= numPages - 1
								}
							]
							blocksMouse: false
							onMousePressed: function( e:MouseEvent ) { if( e.primaryButtonDown and not wiping and page < (numPages-1) ) page++ }
						}
					]
				}, Line {
					startX: 20
					endX: bind realWidth - 20
					startY: bind height - 50
					endY: bind height - 50
					stroke: Color.web("#737373")
				}, Text {
					x: 18
					y: 40
					fill: Color.web("#737373")
					content: bind text
				}, Text {
					x: bind realWidth - 75
					y: 40
					fill: Color.web("#737373")
					font: Font { size: 10 }
					content: bind ##[PAGE_NUM]"Page {page+1} of {numPages}"
				}, wipe = XWipePanel {
					height: bind height
					width: bind contentWidth
					layoutX: 78
					wipe: slideWipe = SlideWipe {
						time: 200ms
					}
					action: function() {
						if( oldContainer != null ) {
							oldContainer.content = null;
							oldContainer = null;
						}
						wiping = false;
					}
					content: buildContainer()
				}
			]
		}
	}
	
	override function getPrefHeight( width: Float ) {
		displayedContainer.getPrefHeight( contentWidth ) + 100
	}
	
	override function getPrefWidth( height: Float ) {
		displayedContainer.getPrefWidth( height - 100 ) + 150
	}
	
}
