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
*TooltipHolder.fx
*
*Created on mar 11, 2010, 10:49:48 fm
*/

package com.eviware.loadui.fx.ui.popup;

import com.eviware.loadui.fx.ui.node.BaseMixin;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;

import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Math;

function getTextNode( label:String ) {
	Label {
		layoutX: 4
		layoutY: 4
		textWrap: true
		text: label
	}
}

def rectNode = Rectangle {
	fill: Color.WHITE
	stroke: Color.BLACK
	layoutX: 0.5
	layoutY: 0.5
}
def tooltipNode = Group {
	content: rectNode
}

var x:Number;
var y:Number;

def appear = Timeline {
	keyFrames: [
		KeyFrame { time: 0s, values: tooltipNode.opacity => 0 },
		KeyFrame { time: 0.5s, values: tooltipNode.opacity => 0, action: function() {
			tooltipNode.layoutX = Math.min( tooltipNode.scene.width - rectNode.width, x );
			tooltipNode.layoutY = Math.min( tooltipNode.scene.height - rectNode.height, y + 18 );
			tooltipNode.toFront();
		} },
		KeyFrame { time: 1s, values: tooltipNode.opacity => 1 }
	]
}

/**
 * The currently displayed tooltip belongs to this TooltipHolder.
 */
public-read var currentHolder:TooltipHolder;

/**
 * Mixin class for giving BaseNodes a tooltip which is displayed when the user hovers the mouse cursor over the node.
 *
 * @author dain.nilsson
 */
public mixin class TooltipHolder extends BaseMixin {
	/**
	 * The text to display for this nodes tooltip. If it is null or an empty String, no tooltip will be displayed.
	 */
	public var tooltip:String;
	
	init {
		(this as BaseNode).addMouseHandler( MOUSE_ENTERED, function( e:MouseEvent ) {
			if( not e.primaryButtonDown )
				showTooltip( e );
		} );
		(this as BaseNode).addMouseHandler( MOUSE_EXITED, hideTooltip );
		(this as BaseNode).addMouseHandler( MOUSE_PRESSED, hideTooltip );
		(this as BaseNode).addMouseHandler( MOUSE_MOVED, function( e:MouseEvent ):Void {
			x = e.sceneX;
			y = e.sceneY;
		} );
	}
	
	function hideTooltip( e:MouseEvent ) {
		if( currentHolder == this ) {
			currentHolder = null;
			delete tooltipNode from BaseNode.overlay.content;
		}
	}
	
	function showTooltip( e:MouseEvent ) {
		if( tooltip == null or tooltip == "" or currentHolder == this )
			return;
		
		if( currentHolder != null ) {
			delete tooltipNode from BaseNode.overlay.content;
		} else {
			appear.playFromStart();
		}
		
		def textNode = getTextNode( tooltip );
		rectNode.width = textNode.getPrefWidth(-1) + 6;
		rectNode.height = textNode.getPrefHeight(-1) + 6;
		tooltipNode.content = [ rectNode, textNode ];
		
		currentHolder = this;
		
		insert tooltipNode into BaseNode.overlay.content;
		
		//FX.deferAction( function():Void {
			//rectNode.width = textNode.getPrefWidth(-1) + 6;
			//rectNode.height = textNode.getPrefHeight(-1) + 6;
		//} );
	}
}
