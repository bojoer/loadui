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

import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.widgets.canvas.TerminalNode;
import com.eviware.loadui.fx.ui.node.BaseMixin;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;

import javafx.animation.transition.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.util.Math;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.ui.dnd.Draggable;

/**
 * The currently displayed tooltip belongs to this TooltipHolder.
 */
public-read var currentHolder:TooltipHolder;

/**
 *
 * @author henrik.olsson
 */
public class Balloon extends Stack {

//	def tooltipNode = Group {
//		content: vBox
//	}
	public def fading = FadeTransition {
     duration: 0.4s node: this 
     fromValue: 1.0 toValue: 0.0
 	};
	
	var x:Number;
	var y:Number;
	
	override var layoutInfo = LayoutInfo { 
		minWidth: 50, 
		width: 100,
		maxWidth: 180,
		vfill: false,
		hfill: true
	};
	
	var innerVBox:VBox;
	
	/**
	* The text to display for this nodes tooltip. If it is null or an empty String, no tooltip will be displayed.
	*/

	public-init var terminalNode:TerminalNode;
        
//        init {
//                (this as BaseNode).addMouseHandler( MOUSE_ENTERED, function( e:MouseEvent ) {
//                        if( not e.primaryButtonDown )
//                                showTooltip( e );
//                } );
//                (this as BaseNode).addMouseHandler( MOUSE_EXITED, hideTooltip );
//                (this as BaseNode).addMouseHandler( MOUSE_PRESSED, hideTooltip );
//                (this as BaseNode).addMouseHandler( MOUSE_MOVED, function( e:MouseEvent ):Void {
//                        x = e.sceneX;
//                        y = e.sceneY;
//                } );
//        }
        
	init {
		
		def header:String = terminalNode.terminal.getLabel();
		
		def headerNode:Label = Label {
			textWrap: false
			text: header
			styleClass: "balloon-header"
			//font: Font{ size: 11, embolden: true }
		};
		
		insert [
			Stack {
				layoutInfo: LayoutInfo { margin: Insets { top: 5, bottom: 5 } }
				content: [ 
					Region {
						styleClass: "balloon-frame"
					}, innerVBox=VBox {
						content: headerNode, 
						spacing: 6,
						layoutInfo: LayoutInfo { 
							margin: Insets { top: 10, right: 10, bottom: 10, left: 10 }
							minWidth: 50
							width: 100
							maxWidth: 250
						}
					}
				]
			}, if( terminalNode.terminal instanceof OutputTerminal ) Region {
				styleClass: "balloon-arrow-up",
				layoutInfo: LayoutInfo { width: 8, height: 5, vfill: false, hfill: false, hpos: HPos.CENTER, vpos: VPos.TOP }
			} else Region {
				styleClass: "balloon-arrow-down",
				layoutInfo: LayoutInfo { width: 8, height: 5, vfill: false, hfill: false, hpos: HPos.CENTER, vpos: VPos.BOTTOM }
			}
		] into content;
		
		
		var text:String = null;
		
		if ( terminalNode.terminal.getDescription() != null ) {
			text = terminalNode.terminal.getDescription();
		}
		
		if( text != null )
		{
			insert Label {
				textWrap: true
				text: text
				//font: Font { size: 11 }
				styleClass: "balloon-text"
			} into innerVBox.content;
		}
	}
}
