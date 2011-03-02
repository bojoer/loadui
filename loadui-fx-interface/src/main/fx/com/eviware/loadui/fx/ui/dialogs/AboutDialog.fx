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
package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.util.Properties;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.LoadUI;

import java.lang.Exception;
import java.lang.System;

public class AboutDialog {
	var group:Group;
	var modalLayer:Node;
	postinit {
		def version = System.getProperty(LoadUI.BUILD_NUMBER);
		def date = System.getProperty(LoadUI.BUILD_DATE);
	
		def scene = AppState.byName("MAIN").scene;
		def items:Node[] = [
			modalLayer = Rectangle {
				width: bind scene.width
				height: bind scene.height
				fill: Color.TRANSPARENT
				blocksMouse: true
				onMousePressed: function( e:MouseEvent ) {
					delete group from AppState.byScene( scene ).overlay.content;
					delete modalLayer from AppState.byScene( scene ).overlay.content;
				}
			}, group = Group {
				layoutX: bind ((scene.width - group.layoutBounds.width) / 2) as Integer
				layoutY: bind ((scene.height - group.layoutBounds.height) / 2) as Integer
				content: [
					Rectangle {
						layoutY: 70
						width: 600
						height: 320
						arcWidth: 20
						arcHeight: 20
						fill: Color.rgb( 0, 0, 0, 0.65 )
						effect: DropShadow {
							radius: 35
							color: Color.rgb( 0, 0, 0 )
						}
					}, ImageView {
						image: Image { url: "{__ROOT__}images/png/icon-clear.png" }
						layoutX: 70
					}, Label {
						layoutX: 20
						layoutY: 230
						textFill: Color.WHITE
						text: "loadUI Version {LoadUI.VERSION}\r\n\r\nBuild version: {if(version != null) version else '[internal]'}\r\nBuild date: {if(date != null) date else '0000-00-00 00:00'}\r\n\r\nCopyright 2011 eviware software ab\r\neviware and loadUI are trademarks of Eviware Software AB"
					}, Hyperlink {
						layoutX: 20
						layoutY: 340
						style: "-fx-text-fill: #ffffff"
						text: "www.loadui.org"
						action: function():Void { openURL("http://www.loadui.org") }
					}, Hyperlink {
						layoutX: 20
						layoutY: 360
						style: "-fx-text-fill: #ffffff"
						text: "www.eviware.com"
						action: function():Void { openURL("http://www.eviware.com") }
					}
				]
			}
		];

		insert items into AppState.byScene( scene ).overlay.content;
	}
}