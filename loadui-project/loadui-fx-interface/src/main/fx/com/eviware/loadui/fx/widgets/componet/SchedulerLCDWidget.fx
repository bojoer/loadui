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

package com.eviware.loadui.fx.widgets.componet;

import javafx.scene.*;
import javafx.fxd.FXDNode;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Math;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.lang.Duration;

import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.util.layout.SchedulerModel;

import java.util.Calendar;
import java.util.Observer;
import java.util.Observable;
import java.util.Iterator;
import java.util.List;

public class SchedulerLCDWidget extends Widget, BaseNode, Resizable, TooltipHolder, Observer {

    def background:FXDNode = FXDNode {
        url:"{com.eviware.loadui.fx.FxUtils.__ROOT__}images/LCD_display_430x75.fxz";
    } 
    
    public-init var model: SchedulerModel on replace {
    	model.addObserver(this);
    	update(model, null);
    }
    
    var duration: Number = 0;
    
    var dailyLines: DailySchedule[] = [];
    var daysVisible: Boolean[] = [];
    
    public override function create() {
    	
    	for(i in [1..7]){
    		insert false into daysVisible;
    		insert DailySchedule {
    			dayIndex: i
    			duration: bind duration
    			visible: bind daysVisible[i-1]
    		} into dailyLines;
    	}
    	
        Group {
            content: [
            	background,
                Group {
                    layoutX: 17 
                    layoutY: 19
                    content:[
	                    createText("Day", 0, 0, false),
	                    createText("Mon", 65, 0),
	                    createText("Tue", 115, 0),
	                    createText("Wed", 165, 0),
	                    createText("Thu", 215, 0),
	                    createText("Fri", 265, 0),
	                    createText("Sat", 315, 0),
	                    createText("Sun", 365, 0),
	                    createText("Duration", 0, 18, false),
	                    createText("Time", 0, 34, false),
	                    createText("0", 40, 34),
	                    createText("12", 65, 34),
	                    createText("0", 90, 34),
	                    createText("12", 115, 34),
	                    createText("0", 140, 34),
	                    createText("12", 165, 34),
	                    createText("0", 190, 34),
	                    createText("12", 215, 34),
	                    createText("0", 240, 34),
	                    createText("12", 265, 34),
	                    createText("0", 290, 34),
	                    createText("12", 315, 34),
	                    createText("0", 340, 34),
	                    createText("12", 365, 34),
	                    createText("0", 390, 34),
	                    Group{
	                    	layoutX: 40
	                    	layoutY: 16
	                    	content:bind [
								Rectangle {
							        x: 0
							        y: -2
							        height: 4
							        width: 350
							        fill: Color.web("#666666")
							        managed: false
							    }
							    createVLine(0, 0, 17),
							    createVLine(25, 0, 7, false),
							    createVLine(50, 0, 17),
							    createVLine(75, 0, 7, false),
							    createVLine(100, 0, 17),
							    createVLine(125, 0, 7, false),
							    createVLine(150, 0, 17),
							    createVLine(175, 0, 7, false),
							    createVLine(200, 0, 17),
							    createVLine(225, 0, 7, false),
							    createVLine(250, 0, 17),
							    createVLine(275, 0, 7, false),
							    createVLine(300, 0, 17),
							    createVLine(325, 0, 7, false),
							    createVLine(350, 0, 17),
							    dailyLines,
							    CurrentPosition{}                    	
	                    	]
	                    }
                    ]	
                }
            ]
        }
    }
    
    function createText(text: String, layoutX: Number, layoutY: Number): Text {
    	createText(text, layoutX, layoutY, true);
    }
    
    function createText(text: String, layoutX: Number, layoutY: Number, centerAlign: Boolean): Text {
    	var textBox: Text = Text {
    		layoutX: bind if(centerAlign) layoutX - textBox.layoutBounds.width/2 else layoutX
    		layoutY: layoutY
            font: Font.font("Arial", 8)
            fill: Color.web("#1ae519")
            content: text
        }
        textBox;
    }

	function createVLine(layoutX: Number, layoutY: Number, height: Number): Line {
		createVLine(layoutX, layoutY, height, true)
	}
	
    function createVLine(layoutX: Number, layoutY: Number, height: Number, centerAlign: Boolean): Line {
	    Line {
	        startX: layoutX
	        endX: layoutX
	        startY: if(centerAlign) layoutY - height/2 else layoutY 
	        endY: if(centerAlign) layoutY + height/2 else layoutY + height 
	        stroke: Color.web("#8C8C8C")
	        managed: false
	    }
    }
    
    override function getPrefWidth (height:Number) {
        430
    }
    
    override function getPrefHeight (width:Number) {
        75
    }
    
    override function update(observable: Observable, arg: Object) {
        FX.deferAction(function(): Void {
            duration = model.getDuration();
            daysVisible = model.getDaysAsBoolean();
            for(d in dailyLines){
            	d.hours = model.getHours();
            	d.minutes = model.getMinutes();
            	d.generate();
            }
        });
    }
}

public class DailySchedule extends Group {

	public-init var dayIndex: Number = 1;
	
	public-init var dayWidth: Number = 50;
	
	public var hours: List;
	
	public var minutes: List;

	public var duration: Number = 0;
	
	public function generate() {
		delete content;
		for(h in hours){
			for(m in minutes){
				create(h as Integer, m as Integer);
			}
		}
	}

	function create(h: Integer, m: Integer): Void {
		insert SchedulePosition{
			dayIndex: dayIndex
			time: h * 60 + m
			duration: bind duration
			visible: bind visible
			minWidth: bind 0.56 * dayWidth / (hours.size() * minutes.size())
    	} into content;
	}
	
}

public class SchedulePosition extends Rectangle {

	public-init var dayIndex: Number = 1;
	
	public-init var dayWidth: Number = 50;
	
	public-init var minWidth: Number = 1;
	
	public var time: Number = 0 on replace {
		x = time * dayWidth / 1440 + dayWidth * (dayIndex - 1);	
	}

	public var duration: Number = 0;
	
	init{
	    y = -9;
        height = 18;
        fill = Color.rgb(0, 96, 182, 0.5);
        managed = false;
	}
	
	override var width = bind Math.max((duration / 60000) * (dayWidth / 1440), minWidth);
	
}

public class CurrentPosition extends Rectangle {

	public-init var dayWidth: Number = 50;
	
	init{
	    y = -13;
        height = 26;
        width = 1;
        fill = Color.web("#FF7F00");
        managed = false;
        
        timeline.playFromStart();
        updatePosition();
	}
	
	var timeline: Timeline = Timeline {
        repeatCount: Timeline.INDEFINITE
        keyFrames: [
	        KeyFrame {
	            time: 60s
	            action: function() {
	            	updatePosition();
	            }
	            canSkip: true
	        }
        ]
    }
    
    function updatePosition(){
		var calendar: Calendar = Calendar.getInstance();
		var day = if(calendar.get(Calendar.DAY_OF_WEEK) == 0) 6 else calendar.get(Calendar.DAY_OF_WEEK) - 2;
		var mins = day * 24 * 60 + calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
	    x = mins * dayWidth / 1440;
    }
	
}

