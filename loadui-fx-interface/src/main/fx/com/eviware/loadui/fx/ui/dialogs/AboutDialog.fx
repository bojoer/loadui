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

import java.lang.Exception;

public class AboutDialog {
	var group:Group;
	var modalLayer:Node;
	postinit {
		def buildinfo = new Properties();
		try {
			buildinfo.load( getClass().getResourceAsStream("/properties/buildinfo.txt") );
		} catch( e:Exception ) {
		}
		def version = buildinfo.get("build.number");
		def date = buildinfo.get("build.date");
	
		def scene = Dialog.overlay.scene;
		def items:Node[] = [
			modalLayer = Rectangle {
				width: bind scene.width
				height: bind scene.height
				fill: Color.TRANSPARENT
				blocksMouse: true
				onMousePressed: function( e:MouseEvent ) {
					delete group from Dialog.overlay.content;
					delete modalLayer from Dialog.overlay.content;
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
					//	blocksMouse: true ( allow closing if you click in about area ( luco-786 )
						effect: DropShadow {
							radius: 35
							color: Color.rgb( 0, 0, 0 )
						}
					}, ImageView {
						image: Image { url: "{__ROOT__}images/png/icon-clear.png" }
						layoutX: 70
					}, ImageView {
						image: Image { url: "{__ROOT__}images/png/beta-clear.png" }
						layoutX: 320
						layoutY: 70
					}, Label {
						layoutX: 20
						layoutY: 230
						textFill: Color.WHITE
						text: "loadUI Version 1.0 beta 1\r\n\r\nBuild version: {if(version != null) version else '[internal]'}\r\nBuild date: {if(date != null) date else '0000-00-00 00:00'}\r\n\r\nCopyright 2010 Eviware Software AB\r\neviware and loadUI are trademarks of Eviware Software AB"
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

		insert items into Dialog.overlay.content;
	}
}