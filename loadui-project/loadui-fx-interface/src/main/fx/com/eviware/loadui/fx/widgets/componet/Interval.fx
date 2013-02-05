/* 
 * Copyright 2011 SmartBear Software
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
*IntervalLCD.fx
*
*Created on May 4, 2010, 13:11:01 PM
*/

package com.eviware.loadui.fx.widgets.componet;

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.fxd.FXDNode;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import javafx.util.Math;

import java.util.Observer;
import java.util.Observable;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.lang.Duration;

import com.eviware.loadui.util.layout.IntervalModel;

public class Interval extends Panel, Resizable, Observer {

    public-init var model:IntervalModel on replace {
    	update( model, null );
    }

    def startPeriodText:String = "Start at";
    var startPeriodText2:String = "0:00";
    def endPeriodText:String = "Duration";
    var endPeriodText2:String = "0:00";
    var endLimitText:String ="... min";
    def startY = 40;
    def endY = 63;
    var progresWidth = 0;
    var progressSpeed:Duration = 1s;
    var mainLength = 210;
    
    var timeline: Timeline = Timeline {
        repeatCount: Timeline.INDEFINITE
        keyFrames: [
        KeyFrame {
            time: 0.01s //bind progressSpeed
            action: function() {
                progresWidth = if(model.getEnd() > 0 ) model.getPosition() * mainLength / model.getEnd() else 0;
            }
            canSkip: true
        }
        ]
    };
    
    var progress:Rectangle = Rectangle {
        x: 30
        y: 50
        height: 3
        width: bind progresWidth
        fill: Color.web("#62bd06")
    }
    
    var mainRec:Rectangle = Rectangle {
        x: 30
        y: 50
        height: 3
        width: mainLength
        fill: Color.GRAY
        managed: false
    }
    
    var startLine:Line = Line {
        startX: 30
        endX: 30
        startY: 45
        endY: 58
        stroke: Color.GRAY
        managed: false
    }
    
    
    var endLine:Line = Line {
        startX: 240
        endX: 240
        startY: 45
        endY: 58
        stroke: Color.GRAY
        managed: false
    } 
    
    var startText:Text = Text {
        content: "0:0 min"
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        managed: false
        layoutX: 30
        layoutY: 65
    }
    
    var endText:Text = Text {
        content: bind endLimitText
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        managed: false
        layoutX: bind 250 - endText.layoutBounds.width - 10
        layoutY: 65
    }
    
    var startPeriod:Text = Text {
        content: startPeriodText
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        layoutX: 70
        layoutY: 20
    }
    
    var startPeriod2:Text = Text {
        content: bind startPeriodText2
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        layoutX: 70
        layoutY: 30
    }
    
    var startPeriodLine:Line = Line {
        startX: 100
        endX: 100
        startY: startY
        endY: endY
        stroke: Color.GRAY
    }
    
    var endPeriod:Text = Text {
    	  content: endPeriodText
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        layoutX: 170
        layoutY: 20
    }
    
    var endPeriod2:Text = Text {
        content: bind endPeriodText2
        font: Font.font("Amble", 8)
        fill: Color.web("#1ae519")
        layoutX: 170
        layoutY: 30
    }
    
    var endPeriodLine:Line = Line {
        startX: 150
        endX: 150
        startY: startY
        endY: endY
        stroke: Color.GRAY
    }
    
    var period:Rectangle = Rectangle {
        x: 30
        y: 45
        height: 15
        width: 0
        fill: Color.web("#113659", .5)
    }
    
    init {
        model.addObserver(this);
        
        insert mainRec into content;
        insert progress into content;
        insert startPeriod into content;
        insert startPeriod2 into content;
        insert startPeriodLine into content;
        insert endPeriod into content;
        insert endPeriod2 into content;
        insert endPeriodLine into content;
        insert startLine into content;
        insert endLine into content;
        insert startText into content;
        insert endText into content;
        insert period into content;
    }
    
    override var onLayout = function():Void {
        resizeContent(); // will set all content to preferred sizes
        def startTime = model.getStart() / 1000;
        def duration = ( model.getStop() - model.getStart() ) / 1000;
        def endTime = model.getEnd() / 1000;
        startPeriodText2 = "{Math.round(startTime/60)}:{startTime mod 60} min";
        endPeriodText2 = if(model.isInfinite()) "Infinite" else "{Math.round(duration/60)}:{duration mod 60} min";
        endLimitText = if(model.isInfinite()) "Infinite" else "{Math.round(endTime/60)}:{endTime mod 60} min";
        
        if( model.getEnd() > 0 ) {
        		positionNode( startPeriodLine, 30 + model.getStart() * mainLength / model.getEnd(), startPeriodLine.boundsInParent.minY );
        		positionNode( endPeriodLine, 30 + model.getStop() * mainLength / model.getEnd(), endPeriodLine.boundsInParent.minY );
        } else {
        		positionNode( startPeriodLine, 30, startPeriodLine.boundsInParent.minY );
        		positionNode( endPeriodLine, 30, endPeriodLine.boundsInParent.minY );
        }
        period.width = endPeriodLine.boundsInParent.minX - startPeriodLine.boundsInParent.maxX;
        positionNode( period, startPeriodLine.boundsInParent.maxX, period.boundsInParent.minY );
        
        if( model.getStart() < model.getStop() ) {
        		positionNode( startPeriod, startPeriodLine.boundsInParent.minX - 40, startPeriod.boundsInParent.minY );
        		positionNode( startPeriod2, startPeriodLine.boundsInParent.minX - 40, startPeriod2.boundsInParent.minY );
        } else {
        		positionNode( startPeriod, -10, startPeriod.boundsInParent.minY );
        		positionNode( startPeriod2, -10, startPeriod2.boundsInParent.minY );
        }
        
        if( endPeriodLine.boundsInParent.maxX + 1 < mainLength) {
				positionNode( endPeriod, endPeriodLine.boundsInParent.maxX + 1, endPeriod.boundsInParent.minY );
				positionNode( endPeriod2, endPeriodLine.boundsInParent.maxX + 1, endPeriod2.boundsInParent.minY );
        } else {
				positionNode( endPeriod, mainLength, endPeriod.boundsInParent.minY );
				positionNode( endPeriod2, mainLength, endPeriod2.boundsInParent.minY );
        }
    }
    
    override function getPrefWidth (height:Number) {
        250
    }
    
    override function getPrefHeight (width:Number) {
        80
    }
    
    override function update(observable: Observable, arg: Object) {
        FX.deferAction(function(): Void {
            progresWidth = if(model.getEnd() > 0 ) model.getPosition() * mainLength / model.getEnd() else 0;
            
            if( model.isRunning() and not timeline.running ) {
            	timeline.playFromStart();
            } else if( not model.isRunning() and timeline.running ) {
            	timeline.stop();
            }
            
            onLayout();
        });
    }
};
