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
package com.eviware.loadui.fx.ui.border;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;

import javafx.scene.Group;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.LayoutInfo;
import javafx.stage.Stage;

import org.jfxtras.scene.shape.ResizableRectangle;

import org.jfxtras.scene.border.*;

public class RoundedRectBorder extends XRaisedBorder {
    override  var backgroundFill = Color.WHITE;
    /**
     * Defines the arc for the corrners
     *
     * @defaultvalue 20
     * @css arc
     */
    public var arc: Number = 20.0;

    override var clip = Rectangle {
        width: bind widthOfBorder
        height: bind heightOfBorder
        arcWidth: bind arc
        arcHeight: bind arc
    };
    override var background = ResizableRectangle {
        fill: bind backgroundFill
        arcWidth: bind arc
        arcHeight: bind arc
    };

    override var border = Group {
        layoutX: bind borderX
        layoutY: bind borderY
        content: [
            Rectangle {
                //layoutX: 1
                //layoutY: 1
                width: bind widthOfBorder
                height: bind heightOfBorder
                fill: bind null
                arcWidth: bind arc
                arcHeight: bind arc
           //     stroke: bind if(raised) shadow else highlight
            },
            Rectangle {
                layoutX: 1
                layoutY: 1
                width: bind widthOfBorder - 2
                height: bind heightOfBorder - 2
                fill: null
                arcWidth: bind arc
                arcHeight: bind arc
         //       stroke: bind if(raised) highlight  else shadow
            },
        ]
    };

    public override function doBorderLayout(x:Number, y: Number,
                                    w: Number, h: Number) :  Void {
        widthOfBorder = w;
        heightOfBorder = h;
        borderY = y;
        borderX = x;
        //border = rectBoder;
    }
}

