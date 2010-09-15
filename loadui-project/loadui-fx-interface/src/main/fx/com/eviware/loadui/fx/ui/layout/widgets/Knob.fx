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
/*
*Knob.fx
*
*Created on mar 22, 2010, 10:44:09 fm
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.TextAlignment;
import javafx.geometry.BoundingBox;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.layout.widgets.support.NumericWidgetBase;
import com.eviware.loadui.fx.ui.resources.RadialLines;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.FxUtils;

import java.lang.NumberFormatException;

/**
 * A Knob which can be turned to change a numeric value.
 *
 * @author dain.nilsson
 */
public class Knob extends NumericWidgetBase {
	
	public-init var skin:Node = if( bounded ) KnobBoundSkin {
		knob: this
		layoutX: bind (width - skin.layoutBounds.width) /2
	} else KnobUnboundSkin {
		knob: this
		layoutX: bind (width - skin.layoutBounds.width) /2
	}
	
	override var styleClass = "knob";
	
	override var layoutBounds = bind lazy BoundingBox {
		minX: 0
		minY: 0
		width: width
		height: height
	}
	
	public def textBox:TextBox = TextBox {
		layoutX: bind (width-textBox.width) / 2 //15
		height: 15
		width: bind 10 + 6 * Math.max( 3, textValue.length() )
		text: bind textValue with inverse
		opacity: 0
		onKeyReleased: function( e:KeyEvent ):Void {
			if( e.code == KeyCode.VK_ENTER )
				textBox.opacity = 0;
		}
	}
	
	def tbf = bind textBox.focused on replace {
		textBox.opacity = if( tbf ) 1 else 0;
	}
	
	var startX:Number;
	var startY:Number;
	var startValue:Number;
	var dragging = false;
	
	var labelNode:Label;
	override function create() {
		def group = Group {
			autoSizeChildren: false
			content: [
				skin, labelNode = Label {
					layoutX: bind width/2 
					layoutY: 45
					text: bind label
					//width: bind width - 2
					//height: bind height - 45
					hpos: HPos.CENTER
					vpos: VPos.TOP
					textWrap: true
					textAlignment: TextAlignment.CENTER
				}, textBox
			]
		}
		
		group
	}
	
	override function getPrefWidth( height:Float ) {
		Math.max( 45, labelNode.getPrefWidth( -1 ) )
	}
	
	override function getPrefHeight( width:Float ) {
		45 + labelNode.getPrefHeight( width )
	}
}
