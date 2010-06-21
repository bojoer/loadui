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

import com.eviware.loadui.impl.layout.IntervalObservableModel;

public class Interval extends Panel, Resizable, Observer {

    public-init var model:IntervalObservableModel;

    def startPeriodText:String = "Start at";
    var startPeriodText2:String = "0:00";
    def endPeriodText:String = "Duration";
    var endPeriodText2:String = "0:00";
    var endLimitText:String ="... min";
    def startY = 40;
    def endY = 63;
    var periodWidth = 0;
    var progresWidth = 0;
    var progressSpeed:Duration = 1s;
    var mainLength = 210;
    
    var timeline: Timeline = Timeline {
        repeatCount: Timeline.INDEFINITE
        keyFrames: [
        KeyFrame {
            time: bind progressSpeed
            action: function() {
                progresWidth += 1;
            }
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
    
    var period:Rectangle = Rectangle {
        x: 100
        y: 45
        height: 15
        width: bind periodWidth
        fill: Color.web("#113659", .5)
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
        font: Font.font("Arial", 8)
        fill: Color.web("#1ae519")
        managed: false
        layoutX: 30
        layoutY: 65
    }
    
    var endText:Text = Text {
        content: bind endLimitText
        font: Font.font("Arial", 8)
        fill: Color.web("#1ae519")
        managed: false
        layoutX: bind 250 - endText.layoutBounds.width - 10
        layoutY: 65
    }
    
    var startPeriod:Text = Text {
        content: startPeriodText
        font: Font.font("Arial", 8)
        fill: Color.web("#1ae519")
        layoutX: 70
        layoutY: 20
    }
    
    var startPeriod2:Text = Text {
        content: bind startPeriodText2
        font: Font.font("Arial", 8)
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
        font: Font.font("Arial", 8)
        fill: Color.web("#1ae519")
        layoutX: 170
        layoutY: 20
    }
    
    var endPeriod2:Text = Text {
        content: bind endPeriodText2
        font: Font.font("Arial", 8)
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
        startPeriodText2 = "{Math.round(model.getStart()/60)}:{model.getStart() mod 60} min";
        endPeriodText2 = "{Math.round(model.getDuration()/60)}:{model.getDuration() mod 60} min";
        if (model.getInterval() > -1 ) {
            endLimitText = "{Math.round(model.getInterval()/60)}:{model.getInterval() mod 60} min";
        }
        
        if( model.getStart() == 0) {
            positionNode(startPeriodLine, 30, startPeriodLine.boundsInParent.minY );
            } else {
            var moveX = 0;
            if ( model.getInterval() > -1 ) {
                moveX = (model.getStart() * mainLength)/model.getInterval() + 30;
                } else {
                moveX = (model.getStart() * mainLength)/10000 + 30;
            }
            if ( moveX <= 240 )
            positionNode(startPeriodLine, moveX, startPeriodLine.boundsInParent.minY );
        }
        
        if( model.getDuration() == 0) {
            positionNode(endPeriodLine, 30, endPeriodLine.boundsInParent.minY );
            periodWidth = 0;
        } else {
            var moveX = 0;
                periodWidth = (model.getDuration() * mainLength)/model.getInterval();
                moveX = ((model.getStart() + model.getDuration())* mainLength)/model.getInterval() + 30;
            if ( moveX <= 240 )
            positionNode(endPeriodLine, moveX, endPeriodLine.boundsInParent.minY );
        }
        if ( periodWidth + startPeriodLine.boundsInParent.maxX < endLine.boundsInParent.minX ) {
        	resizeNode(period, periodWidth, 15);
        }
        positionNode( period, startPeriodLine.boundsInParent.maxX, period.boundsInParent.minY );
        
        positionNode(startPeriod, startPeriodLine.boundsInParent.minX - 40, startPeriod.boundsInParent.minY );
        positionNode(startPeriod2, startPeriodLine.boundsInParent.minX - 40, startPeriod2.boundsInParent.minY );
        if( endPeriodLine.boundsInParent.maxX + 1 < mainLength) {
        	positionNode(endPeriod, endPeriodLine.boundsInParent.maxX + 1, endPeriod.boundsInParent.minY );
        	positionNode(endPeriod2, endPeriodLine.boundsInParent.maxX + 1, endPeriod2.boundsInParent.minY )
        	}
        else {
        	positionNode(endPeriod, mainLength, endPeriod.boundsInParent.minY );
        	positionNode(endPeriod2, mainLength, endPeriod2.boundsInParent.minY );
        	}
        
    }
    
    override function getPrefWidth (height:Number) {
        250
    }
    
    override function getPrefHeight (width:Number) {
        80
    }
    
    override function update(observable: Observable, arg: Object) {
        FX.deferAction(
        function(): Void {
            
            if( model.isRunning() ) {
                if ( not timeline.running ) {
                		timeline.repeatCount = mainLength;
                		progressSpeed = Duration.valueOf((model.getInterval() * 1000)/mainLength );
                		timeline.playFromStart();
                } 
            } else {
                timeline.stop();
                progresWidth = 0;
            }
            onLayout();
        });
    }
};
