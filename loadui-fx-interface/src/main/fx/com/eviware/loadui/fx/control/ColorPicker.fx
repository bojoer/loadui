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
package com.eviware.loadui.fx.control;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.geometry.VPos;

import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.CustomMenuItem;

import com.eviware.loadui.fx.FxUtils;

def COLORS = [
	Color.RED,
	Color.BLUE,
	Color.GREEN,
	Color.YELLOW,
	Color.RED,
	Color.BLUE,
	Color.GREEN,
	Color.YELLOW,
	Color.RED,
	Color.BLUE,
	Color.GREEN,
	Color.YELLOW,
	Color.RED,
	Color.BLUE,
	Color.GREEN,
	Color.YELLOW
];

/**
 * A control for picking a color.
 *
 * @author dain.nilsson
 */
public class ColorPicker extends MenuButton {
	public var color:Color = Color.RED on replace {
		textbox.text = FxUtils.colorToWebString( color ).substring( 1 );
		onReplace( color );
	}
	
	public var onReplace: function( color:Color ):Void;
	
	override var styleClass = "color-picker";
	
	override var graphic = Rectangle {
		width: 6, height: 10, fill: bind color
	}
	
	def textbox = TextBox {
		columns: 6
		selectOnFocus: true
		text: FxUtils.colorToWebString( color ).substring( 1 )
		layoutInfo: LayoutInfo { width: 50, hfill: false }
	}
	
	def textboxText = bind textbox.text on replace {
		try {
			color = Color.web( "#{textboxText}" );
		} catch( e ) {
		}
	}
	
	init {
		items = CustomMenuItem {
			hideOnClick: false
			node: VBox {
				snapToPixel: true
				spacing: 10
				content: [
					HBox {
						spacing: 5
						nodeVPos: VPos.CENTER
						content: [
							Rectangle { width: 18, height: 18, fill: bind color },
							Label { text: "Pick a color    Hex #"},
							textbox
						]
					}, HBox {
						spacing: 1
						content: for( c in COLORS ) Rectangle { width: 15, height: 15, fill: c, onMouseClicked: function( event ):Void {
							color = c;
						} }
					}
				]
			} 
		}
	}
}