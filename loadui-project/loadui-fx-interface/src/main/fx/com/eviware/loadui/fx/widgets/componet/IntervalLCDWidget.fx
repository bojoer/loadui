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
*IntervalWidget.fx
*
*Created on May 4, 2010, 11:08:33 AM
*/

package com.eviware.loadui.fx.widgets.componet;

import javafx.scene.*;
import javafx.fxd.FXDNode;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.util.layout.IntervalModel;

//import com.eviware.loadui.fx.FxUtils;

public class IntervalLCDWidget extends Widget, BaseNode, Resizable, TooltipHolder {

    def background:FXDNode = FXDNode {
        url:"{com.eviware.loadui.fx.FxUtils.__ROOT__}images/LCD_display_320x80.fxz";
    } 
    
    public-init var model:IntervalModel;
    
    public override function create() {
        Stack {
            content: [
            background,
            HBox {
                content: [
                VBox {
                    translateY: 20 
                    translateX: 10
                    spacing: 15
                    content:[
                    Text {
                        font: Font.font("Arial", 8)
                        fill: Color.web("#1ae519")
                        content: "Interval"
                    },
                    Text {
                        font: Font.font("Arial", 8)
                        fill: Color.web("#1ae519")
                        content: "Total test duration"
                    }
                    ]	
                },Interval {
                	model: model
                }
                ]
            }
            ]
        }
    }
    
    override function getPrefWidth (height:Number) {
        320
    }
    
    override function getPrefHeight (width:Number) {
        80
    }
    
};
