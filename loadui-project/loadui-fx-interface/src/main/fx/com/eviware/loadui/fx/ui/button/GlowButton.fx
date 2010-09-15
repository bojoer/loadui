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
*GlowButton.fx
*
*Created on apr 14, 2010, 12:18:56 em
*/

package com.eviware.loadui.fx.ui.button;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;

def glowEffect = Glow {
	level: .5
}

public class GlowButton extends BaseNode, TooltipHolder, Resizable {

	public-init var action:function():Void;
	
	public-init var padding:Number = 0;
	
	init {
		addMouseHandler( MOUSE_PRIMARY_CLICKED, function(e:MouseEvent) {
			action();
		} );
	}
	
	override var blocksMouse = true;
	
	override var width on replace {
		contentNode.layoutX = ( width - (contentNode.layoutBounds.width - contentNode.layoutBounds.minX) ) / 2;
	}
	
	override var height on replace {
		contentNode.layoutY = ( height - (contentNode.layoutBounds.height - contentNode.layoutBounds.minY) ) / 2;
	}

	override function create() {
		Group {
			content: [
				Rectangle {
					fill: Color.TRANSPARENT
					width: bind width
					height: bind height
				}, contentNode
			]
			effect: bind if( hover ) glowEffect else null
		}
	}
	
	override function getPrefHeight( width:Float ) {
		contentNode.layoutBounds.height + ( padding * 2 )
	}
	
	override function getPrefWidth( height:Float ) {
		contentNode.layoutBounds.width + ( padding * 2 )
	}
}
