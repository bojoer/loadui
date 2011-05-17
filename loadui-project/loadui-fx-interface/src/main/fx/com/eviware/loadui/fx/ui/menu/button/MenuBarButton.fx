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
package com.eviware.loadui.fx.ui.menu.button;

import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.fxd.FXDNode;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.dnd.Draggable;

import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.menu.button.MenuBarButton" );

/**
 * Bordeless button, with just graphic. Default behaivor glow when moise enters.
 * 
 * @param graphicUrl path toward graphic shown in button.
 * @param widht 
 * @param height
 */
public class MenuBarButton extends BaseNode, TooltipHolder {
	
	/**
	 * path to the button graphics
	 */
	public var graphicUrl:String;

	public var text:String;
	
	public var backgroundFill: Paint = Color.TRANSPARENT;
	
	public var glowAllowed: Boolean = true;
	
	var glowLevel:Number;
	var glow = Glow {
		level: bind glowLevel
	};
	
	var grafic:FXDNode;
	var textBox:Text;
	 
	override function create():Node {
		var hBox: HBox;
		if(text != null and text != ""){ 
	        hBox = HBox {
	        	nodeVPos: VPos.CENTER
	        	hpos: HPos.CENTER
	        	vpos: VPos.CENTER
	        	spacing: 5
	        	content: [
			    	grafic = FXDNode {
						url: bind graphicUrl
						effect: bind glow
					}
			    	textBox = Text {
			        	font: Font { size:  8 }
			        	content: text
			        }
	        	]
	    	}
		}
		else{
	        hBox = HBox {
	        	nodeVPos: VPos.CENTER
	        	hpos: HPos.CENTER
	        	vpos: VPos.CENTER
	        	spacing: 5
	        	content: [
			    	grafic = FXDNode {
						url: graphicUrl
						effect: bind if(Draggable.currentDraggable == null) glow else null
					}
	        	]
	    	}
		}
		
		return Stack {
			content: [
				Rectangle {
				    x:0
				    y:0
					width: bind ( grafic.layoutBounds.width + textBox.layoutBounds.width + 20 )
					height: bind grafic.layoutBounds.height
					fill: bind backgroundFill
					strokeWidth: 0
					onMouseEntered: function(e:MouseEvent) {
						mouseEntered(e);
					}
					onMouseExited: function(e:MouseEvent) {
						mouseExited(e);
					}
				}
				hBox
			]
		}
	}
	override var onMouseClicked =  function( e: MouseEvent ): Void {
	                           mouseClicked(e);
	                        }
	/**
	 * override this to get onMouseClicked
	 */
	public var mouseClicked = function(event:MouseEvent) {
		log.debug("Mouse Clicked");
	}
	
	/**
	 * override for onMouseEntered
	 */
	public function mouseEntered(event:MouseEvent) {
		//log.debug("Mouse Entered");
		if(glowAllowed){
			glowLevel = .5;
		}
	}
	
	/**
	 * override for onMouseExited
	 */
	public function mouseExited(event:MouseEvent) {
		//log.debug("Mouse Exited");
		glowLevel = 0.0;
	}
	
	
}
