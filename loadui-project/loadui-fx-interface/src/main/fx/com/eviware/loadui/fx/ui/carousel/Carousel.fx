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
package com.eviware.loadui.fx.ui.carousel;

import javafx.scene.Node;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Container;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.util.Sequences;
import javafx.util.Math;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.ui.pagination.Pagination;
import com.eviware.loadui.fx.ui.form.fields.SelectField;

def darken = ColorAdjust { brightness: - 0.4 };
def darkenMore = ColorAdjust { brightness: - 0.6 };

public class Carousel extends VBox {
	override var styleClass = "carousel";
	override var fillWidth = true;
	override var padding = Insets { top: 10, right: 10, bottom: 10, left: 10 };
	
	def itemDisplay = ItemDisplay { parentNode: this }
	
	def selectField = SelectField {
		override var value on replace { select( value as Node ) }
		layoutInfo: LayoutInfo { hfill: true }
	}
	
	public var widthFactor:Number = 0.6;
	public var widthStep:Number = 0.25;
	
	public var label:String = "Agents";
	
	public var items:Node[] on replace {
		itemDisplay.items = [
			Rectangle { width: 1, height: 1, fill: Color.rgb(0,0,0,0.001) },
			Rectangle { width: 1, height: 1, fill: Color.rgb(0,0,0,0.001) },
			items,
			Rectangle { width: 1, height: 1, fill: Color.rgb(0,0,0,0.001) },
			Rectangle { width: 1, height: 1, fill: Color.rgb(0,0,0,0.001) }
		];
		selectField.options = items;
		
		if( sizeof items > 0 ) {
			select( items[0] );
		}
	}
	
	public function select( node:Node ):Void {
		if( sizeof items == 0 or node == null ) return;
		
		def index = Sequences.indexByIdentity( items, node );
		if( index != -1 ) {
			itemDisplay.page = index;
			selectField.value = node;
		}
	}
	
	init {
		content = [
			Region {
				styleClass: "carousel"
				managed: false
				width: bind width
				height: bind height
				onMouseWheelMoved: function( e ) {
					if( e.wheelRotation > 0 and itemDisplay.page < (itemDisplay.numPages-1) ) itemDisplay.page++ else if( e.wheelRotation < 0 and itemDisplay.page > 0 ) itemDisplay.page--;
				}
			}, HBox {
				layoutInfo: LayoutInfo { vfill: true, vgrow: Priority.ALWAYS, hfill: true, hgrow: Priority.ALWAYS }
				content: [
					Button {
						styleClass: "left-button"
						scaleX: 0.5
						scaleY: 0.5
						layoutInfo: LayoutInfo { vpos: VPos.CENTER }
						disable: bind itemDisplay.page <= 0
						action: function():Void { itemDisplay.previousPage() }
					}, itemDisplay, Button {
						styleClass: "right-button"
						scaleX: 0.5
						scaleY: 0.5
						layoutInfo: LayoutInfo { vpos: VPos.CENTER }
						disable: bind itemDisplay.page >= (itemDisplay.numPages-1)
						action: function():Void { itemDisplay.nextPage() }
					}
				]
			}, Separator {}, VBox {
				spacing: 5
				fillWidth: true
				padding: Insets { top: 10, right: 15, bottom: 10, left: 15 }
				content: [
					Label { text: bind label.toUpperCase() },
					selectField
				]
			}
		];
	}
}

class ItemDisplay extends Container, Pagination {
	override var itemsPerPage = 5;
	override var fluid = true;
	
	public-init var parentNode:Node;
	
	def clickBlocker = Rectangle {
		fill: Color.TRANSPARENT
		blocksMouse: true
		width: bind width
		height: bind height
		onMouseWheelMoved: function( e ) {
			if( e.wheelRotation > 0 ) nextPage() else if( e.wheelRotation < 0 ) previousPage();
		}
		onMouseClicked: function( e ) {
			if( e.button == MouseButton.PRIMARY ) {
				if( not displayed[1].contains( displayed[1].sceneToLocal( e.sceneX, e.sceneY ) ) ) {
					if( e.x < width/2 ) previousPage() else nextPage();
				}
			}
			parentNode.onMouseClicked( e );
		}
		onMouseEntered: function( e ) { parentNode.onMouseEntered( e ) }
		onMouseExited: function( e ) { parentNode.onMouseExited( e ) }
		onMouseMoved: function( e ) { parentNode.onMouseMoved( e ) }
		onMousePressed: function( e ) { parentNode.onMousePressed( e ) }
		onMouseReleased: function( e ) { parentNode.onMouseReleased( e ) }
		onMouseDragged: function( e ) { parentNode.onMouseDragged( e ) }
	}
	
	def displayed = bind displayedItems on replace {
		content = [ displayed[0], displayed[4], displayed[1], displayed[3], clickBlocker, displayed[2] ];
		select( displayed[2] );
		doLayout();
	}
	
	override function getPrefWidth( height ) {
		2 * super.getPrefWidth( height )
	}
	
	function placeNode( node:Node, pos:Number ):Void {
		def usableWidth = width * widthFactor;
		def posX = ( 1 + Math.cos( Math.PI * ( 1-pos ) ) ) * usableWidth/2;
		def x = (width-usableWidth)/2 + posX - node.layoutBounds.width/2;
		def y = (height - node.layoutBounds.height)/2;
		
		positionNode( node, x, y );
	}
	
	override function doLayout() {
		//Middle
		positionNode( displayed[2], 0, 0, width, height, HPos.CENTER , VPos.CENTER );
		displayed[2].scaleX = 1;
		displayed[2].scaleY = 1;
		//displayed[2].opacity = 1;
		displayed[2].effect = null;
		
		def bottomLine = displayed[2].layoutY + displayed[2].layoutBounds.height;
		
		//Leftmost
		positionNode( displayed[0], -displayed[0].layoutBounds.width/5, 0, width, bottomLine, HPos.LEFT , VPos.BOTTOM );
		displayed[0].scaleX = 0.5;
		displayed[0].scaleY = 0.5;
		//displayed[0].opacity = 0.25;
		displayed[0].effect = darkenMore;
		
		//Left
		positionNode( displayed[1], 0, 0, width, bottomLine, HPos.LEFT , VPos.BOTTOM );
		displayed[1].scaleX = 0.8;
		displayed[1].scaleY = 0.8;
		//displayed[1].opacity = 0.6;
		displayed[1].effect = darken;
		
		//Right
		positionNode( displayed[3], 0, 0, width, bottomLine, HPos.RIGHT , VPos.BOTTOM );
		displayed[3].scaleX = 0.8;
		displayed[3].scaleY = 0.8;
		//displayed[3].opacity = 0.6;
		displayed[3].effect = darken;
		
		//Rightmost
		positionNode( displayed[4], displayed[4].layoutBounds.width/5, 0, width, bottomLine, HPos.RIGHT , VPos.BOTTOM );
		displayed[4].scaleX = 0.5;
		displayed[4].scaleY = 0.5;
		//displayed[4].opacity = 0.25;
		displayed[4].effect = darkenMore;
		
		//Leftmost
		placeNode( displayed[0], 0 );
		
		//Left
		placeNode( displayed[1], 0.5 - widthStep );
		
		//Right
		placeNode( displayed[3], 0.5 + widthStep );
		
		//Rightmost
		placeNode( displayed[4], 1 );
	}
	
	postinit { doLayout() }
}