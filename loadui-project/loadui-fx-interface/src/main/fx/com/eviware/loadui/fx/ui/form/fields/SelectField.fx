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
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.geometry.VPos;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.resources.MenuArrow;

/**
 * Constructs a new SelectField using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
     SelectField { id:id, label:label, value:value }
}

/**
 * A FormField for any type of value, with a fixed selection of choices. 
 * 
 * @author dain.nilsson
 */
public class SelectField extends CustomNode, FormField {

	def popup = PopupMenu {
		minWidth: bind width
	};

	/**
	 * The available options to choose from.
	 */
	public var options:Object[] on replace {
		resetItems()
	}
	
	/**
	 * Provides a String representation of each option to display.
	 * The default value simply utilizes the toString-method of each object.
	 */
	public var labelProvider = function( o:Object ):String {
		"{o}"
	} on replace {
		resetItems()
	}
	
	function resetItems():Void {
		popup.items = for( option in options ) ActionMenuItem {
			text: labelProvider( option )
			action: function() { value = option }
		}
	}
	
	override var layoutBounds = bind rectNode.layoutBounds;
	
	var menuNode:Menu;
	var labelNode:Label;
	var rectNode:Rectangle;
	override function create() {
		Group {
			content: [
				rectNode = Rectangle {
					width: bind width
					height: bind height
					fill: Color.WHITE
					stroke: Color.rgb( 0x90, 0x90, 0x90 )
					strokeWidth: 2
					arcWidth: 5
					arcHeight: 5
				}, menuNode = Menu {
					noHighlight: true
					contentNode: Group {
						content: [
							Rectangle {
								width: bind width
								height: bind height
								fill: Color.TRANSPARENT
							}, MenuArrow {
								fill: Color.rgb( 0x90, 0x90, 0x90 )
								rotate: 90
								layoutY: bind height / 2
								layoutX: bind width - height / 2
							}, labelNode = Label {
								text: bind labelProvider( value )
								vpos: VPos.CENTER
								textWrap: false
								layoutX: 3
								layoutY: 3
								width: bind width - height
								height: bind height - 6
							}
						]
					}
					menu: bind if(sizeof options > 0) popup else null
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) {
		Math.max( popup.layoutBounds.width, labelNode.getPrefWidth( height ) + height )
	}
	
	override function getPrefHeight( width:Float ) {
		labelNode.getPrefHeight( width ) + 6
	}
} 
