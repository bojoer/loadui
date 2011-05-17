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
*Limiter.fx
*
*Created on apr 22, 2010, 15:20:27 em
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import com.eviware.loadui.fx.ui.node.BaseNode;

public class Limiter extends BaseNode, Resizable {

	public var displayFill:Paint = Color.web("#000000");
	public var displayStroke:Paint = Color.web("#000000");
	public var displayFont:Font = Font.font( "monospaced", 9 );
	public var displayTextFill:Paint = Color.web("#c9c9c9");
	public var limitFill:Paint = Color.web("#000000");
	public var limitStroke:Paint = Color.web("#606060");
	public var barBackgroundFill:Paint = Color.web("#606060");
	public var barFill:Paint = Color.web("#77ef0e");

	
	public var text:String;
	public var value:String;
	public var limit:String = null;
	public var progress:Number = 0;
	public var small:Boolean = false;
	
	
	var labelNode:Label;
	override function create() {
		Group {
			content: [
				Rectangle {
					height: 15
					width: bind width
					fill: bind displayFill
					//stroke: bind displayStroke
				},Rectangle {
					layoutX: 1
					layoutY: 1
					height: 15 - 2
					width: bind width - 2
					fill: Color.TRANSPARENT
					stroke: bind displayStroke
					strokeWidth: 0.5
				}, Label {
					layoutX: 5
					text: bind if( limit == null or small) value else "{%-9s value}/{%9s limit}"
					textFill: bind displayTextFill
					font: bind displayFont
					layoutInfo: LayoutInfo { height: 15 }
					hpos: HPos.LEFT
					vpos: VPos.CENTER
				}, labelNode = Label {
					layoutY: 15
					text: bind text
					font: bind if( small ) Font.font( "Monospaced", 8 ) else Font.font( "Monospaced", 9 );
				}, Rectangle {
					layoutY: 22
					layoutX: bind labelNode.layoutBounds.width
					width: bind width - labelNode.layoutBounds.width
					height: 3
					fill: bind if(limit==null) Color.TRANSPARENT else barBackgroundFill
				}, Rectangle {
					layoutY: 22
					layoutX: bind labelNode.layoutBounds.width
					width: bind progress * ( width - labelNode.layoutBounds.width )
					height: 3
					fill: bind if(limit==null) Color.TRANSPARENT else barFill
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) { width }
	override function getPrefHeight( width:Float ) { 38 }
}
