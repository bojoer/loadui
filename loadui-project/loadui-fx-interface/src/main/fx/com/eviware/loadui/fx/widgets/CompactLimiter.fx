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
/*
*CompactLimiter.fx
*
*Created on May 11, 2010, 16:10:36 PM
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.geometry.HPos;

import com.eviware.loadui.fx.ui.node.BaseNode;

public class CompactLimiter extends BaseNode, Resizable {

	public var displayFill:Paint = Color.web("#4d4d4d");
	public var displayStroke:Paint = Color.web("#919294");
	public var displayFont:Font = Font.font( "monospaced", 8 );
	public var displayTextFill:Paint = Color.web("#c9c9c9");
	public var limitFill:Paint = Color.web("#000000");
	public var limitStroke:Paint = Color.web("#606060");
	public var barBackgroundFill:Paint = Color.web("#666666");
	public var barFill:Paint = Color.web("#77ef0e");

	
	public var value:String;
	public var limit:String = null;
	public var progress:Number = 0;
	
	
	override function create() {
		Group {
			content: [
				Rectangle {
					height: 13
					width: bind width
					fill: bind displayFill
					//stroke: bind displayStroke
				},Rectangle {
					layoutX: 1
					layoutY: 1
					height: 13 - 2
					width: bind width - 2
					fill: Color.TRANSPARENT
					stroke: bind displayStroke
					strokeWidth: 0.5
				}, Label {
					layoutX: 6//4
					text: bind if( limit == null ) value else "{%-8s value} / {%8s limit}"
					textFill: bind displayTextFill
					font: bind displayFont
					height: 10
					width: bind width - 10
				}, Rectangle {
					layoutY: 10
					width: bind width 
					height: 3
					fill: bind if(limit==null) Color.TRANSPARENT else barBackgroundFill
				}, Rectangle {
					layoutY: 10
					width: bind progress * width 
					height: 3
					fill: bind if(limit==null) Color.TRANSPARENT else barFill
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) { width }
	override function getPrefHeight( width:Float ) { 15 }
};
