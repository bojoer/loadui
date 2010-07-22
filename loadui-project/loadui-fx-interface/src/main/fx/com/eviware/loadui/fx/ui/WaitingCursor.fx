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
*WaitingCursor.fx
*
*Created on apr 27, 2010, 10:30:00 fm
*/

package com.eviware.loadui.fx.ui;

import javafx.scene.CustomNode;
import javafx.scene.Group;
import com.eviware.loadui.fx.FxUtils.*;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Label;

import javafx.scene.input.MouseEvent;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.awt.MouseInfo;
import java.awt.PointerInfo;


public var overlay: Group = null;

public class WaitingCursor extends CustomNode {
    
    var innerGroup:Group;
    var mainGroup:Group;
    var txt = "";
    var waiting = false;
    override function create() {
        mainGroup = Group {
            visible: bind waiting
        	content: [
        		Rectangle {
        					width: bind overlay.scene.width
        					height: bind overlay.scene.height
        					blocksMouse: bind waiting
        					opacity: 0.1
        					onMouseMoved:function(me:MouseEvent) {
        					        	      	           innerGroup.layoutX = me.sceneX + 15;
        					        	      	                   		   	innerGroup.layoutY = me.sceneY - 15;
        					        	        	   		}
        				},
        		innerGroup = Group {
        			content: [ 
        				ImageView {
        					image: Image { url: "{__ROOT__}images/png/hour-glass-cursor.png" }	 
        				},
        				Label {
        				    translateX: 20
        		    		text: bind txt
        				}
        			]
        		}
        	]
        	
        }
    }
    
    public function startWait(waitText:String, x, y) {
        if (not waiting) {
       		waiting = true;
       		insert mainGroup into overlay.content;
       		var point:PointerInfo = MouseInfo.getPointerInfo();
       		              
       		              innerGroup.layoutX = x + 15;
       		              innerGroup.layoutY = y - 15;
        }
        txt = waitText;
    }
    
     public function stopWait() {
         if (waiting) {
            waiting = false;
            txt = "";
            delete mainGroup from overlay.content;
         }
     }
}