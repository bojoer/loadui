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
*FeedDisplay.fx
*
*Created on may 11, 2010, 11:46:53 am
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.slf4j.LoggerFactory;
import javafx.scene.Node;
import com.eviware.loadui.fx.ui.node.BaseNode;
import javafx.scene.Group;

import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.scene.layout.Resizable;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Hyperlink;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import com.eviware.loadui.fx.FxUtils;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.HPos;
import javafx.date.DateTime;

import java.text.SimpleDateFormat;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.NewsItem" );

/**
 * A component to display items from the Eviware feed
 */
public class NewsItem extends BaseNode, Resizable {
	def titleColor:Color = Color.web("#C9F601");

	public var title:String;
	public var text:String;
	public var image:String;
	public var url:String;
	public var date:DateTime;

	var hl: Hyperlink;
	var tb: Label;
	var enc: Image;
	
	def df = new SimpleDateFormat("yyyy-MM-dd");
	 def vbox:VBox = VBox {
	     	width: bind 270
			height: bind hl.height + enc.height + tb.height + 100
			spacing: 5
			content: [
	 	         	Label {
		 	         	    width: bind 280
		 	         	    textWrap: true
	 	         		    text: title;
	 	         		    textFill: Color.web("#FFFF00")
	 	         		    font: Font.font("Amble", FontWeight.BOLD, 16)
	 	         		    layoutInfo: LayoutInfo {
	 	         		    	hpos:HPos.LEFT
	 	         		        }
 	         		},
	 	         	ImageView {
	 	         	    image: enc = Image {
	 	         	        url: image
	 	         	    }
	 	         	    layoutInfo: LayoutInfo {
	 	         	        hpos:HPos.CENTER
	 	         	    }
	 	         	},
	 	         	tb = Label {
	 	         	    style: "-fx-text-fill: #ffffff;"
	 	         	    text: if (text.length() > 200) text.substring(0, 200) else text
	 	         	    textWrap: true
	 	         	    font: Font.font("Amble", 12)
	 	         	  //  editable: false
	 	         	    width: bind 280
	 	         	    //height: bind 120
	 	         	},
	 	         	Hyperlink {
	 	         		text: "Read More >";
 		                style: "-fx-text-fill: #006b33"
 		                action: function() {
 		                              FxUtils.openURL(url);
 		                       }
 		            },
	 	         	Label {
	 	         		 text: df.format(date.instant)
	 	       		     textFill: Color.GREY
	 	            },
 	         		Rectangle {
 	         			width: bind width -10
 	         		 	height: bind 2
 	         		 	fill: Color.web("#232323")
 	         		 }
	 	         ]
	 	     }
	 
	 override function create() {
	     vbox
	 }	
	 
	 override function getPrefWidth( height:Float ) { 
	 	vbox.getPrefWidth( height )
	 }
	 	
	 override function getPrefHeight( width:Float ) { 
	     
	     vbox.height
	 }
}
