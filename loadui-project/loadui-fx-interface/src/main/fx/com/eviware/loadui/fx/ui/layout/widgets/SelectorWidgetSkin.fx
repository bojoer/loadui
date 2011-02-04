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
/*
*SelectorWidgetSkin.fx
*
*Created on mar 26, 2010, 13:38:03 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.control.Skin;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import com.eviware.loadui.fx.FxUtils.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.fxd.FXDNode;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.Group;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class SelectorWidgetSkin extends Skin {
    def radioButton:RadioButton = bind control as RadioButton;
    def SELECTED = "{__ROOT__}images/options/radio_button_enabled.fxz";
    def NOTSELECTED = "{__ROOT__}images/options/radio_button_disabled.fxz";
    
    var hbox:HBox;
    public var showLabels:Boolean;
    
    
     
    public override function contains(localX:Number, localY:Number): Boolean {
        return node.contains(localX, localY);
    }
     
    public override function intersects(localX: Number, localY: Number, localWidth: Number, localHeight: Number): Boolean {
       return node.intersects(localX, localY, localWidth, localHeight);
    }
    
    override function getPrefHeight( width:Float ) {
        hbox.getPrefHeight(width)
    }
    	
    	override function getPrefWidth( height:Float ) {
    	    hbox.getPrefWidth(height)
    	}
    
    init {
        	var label:Label;
        	hbox = HBox {
        	    	layoutX:0
        	    	layoutY:0
        	                vpos: VPos.CENTER
        	                spacing: 15
        	            	content: [
        	            		FXDNode {
        	            		    layoutY: 10
        	            			url: bind if (radioButton.selected) SELECTED else NOTSELECTED
        	            		},
        	            		label = Label {
        	            		    vpos: VPos.CENTER
        	                		text:bind if (showLabels) radioButton.text else null
        	                		graphic:bind radioButton.graphic
        	                		font: Font { size: 10 }
        	            	}]
        	            	
        	            }
            node = Group {
               content: [
               		hbox,
            	Rectangle {
            		height: bind hbox.height
            		width: bind hbox.width + label.width
            		fill:Color.TRANSPARENT
            		onMouseClicked: function(me: MouseEvent) {
            		            		radioButton.selected = true;
            		            	}
            	} ]
            	
            }
        }
}
