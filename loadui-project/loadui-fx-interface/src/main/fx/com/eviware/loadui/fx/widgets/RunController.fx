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
*RunController.fx
*
*Created on apr 22, 2010, 10:20:22 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.util.Math;
import javafx.scene.text.Font;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.StylesheetAware;
import com.eviware.loadui.fx.ui.node.BaseNode;
//import com.eviware.loadui.fx.ui.button.ToggleButton;
import com.eviware.loadui.fx.ui.resources.PlayShape;
import com.eviware.loadui.fx.ui.resources.SlashShape;
import com.eviware.loadui.fx.ui.Kitt;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.dialogs.SetCanvasLimitsDialog;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.component.categories.SamplerCategory;
import com.eviware.loadui.fx.FxUtils.*;

import java.util.EventObject;
import javafx.fxd.FXDNode;
import com.eviware.loadui.api.model.SceneItem;

import javafx.scene.control.Tooltip;

public class RunController extends BaseNode, Resizable, StylesheetAware, TimerController {

	override var styleClass = "run-controller";

	public var innerShadowColor:Color = Color.web("#777777");
	public var backgroundFill:Paint = Color.web("#8B8C8F");
	public var separatorFill:Paint = LinearGradient {
		endY: 0
		stops: [
			Stop { offset: 0, color: Color.web("#6E6E6E") },
			Stop { offset: 1, color: Color.web("#949494") }
		]
	};
	public var activeShapeFill:Paint = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.web("#BCBEC0") },
			Stop { offset: 1, color: Color.web("#D9D9D9") }
		]
	};
	public var inactiveShapeFill:Paint = Color.web("#68696C");

	
	public var testcase:Boolean = bind (canvas instanceof SceneItem);
	public var small = false;
	public var testcaseLinked:Boolean = true;
	public var showResetButton:Boolean = true;
	public var showLimitButton:Boolean = true;
	
	public var items:Node[] on replace {
			hbox.content = for( i in [0..<sizeof items] ) [
				if(i>0) Rectangle { width: 3, height: 25, fill: bind separatorFill } else null,
				items[i]
			];
		}
	
	def activeLinkUrl = "{__ROOT__}images/testcase_link_active.fxz";
	def defaultLinkUrl = "{__ROOT__}images/testcase_link_default.fxz";
	
	def linkButton:Group = Group {
		layoutY: -10
		content: [
			FXDNode {
				layoutX: 3
	    		url: bind if (testcaseLinked) activeLinkUrl else defaultLinkUrl
	    		onMousePressed: function( e:MouseEvent ) {
	        		testcaseLinked = not testcaseLinked;
	    		}
	    		onMouseReleased: function( e:MouseEvent ) {
	        		(canvas as SceneItem).setFollowProject(testcaseLinked);
	    		} 
			},
			Label {
				layoutY: 10
				text:"link"
				font: Font.font("Arial", 8)
				layoutInfo: LayoutInfo{width: 28}
				hpos: HPos.CENTER
			}
		]
	}
	
	override var canvas on replace {
		if(testcase) testcaseLinked = (canvas as SceneItem).isFollowProject();
	}
	
	def hbox = HBox {
		layoutY: 6
		width: bind width
		height: bind height - 10
		spacing: 9
		hpos: HPos.CENTER
		vpos: VPos.CENTER
		nodeVPos: VPos.CENTER
		nodeHPos: HPos.CENTER
	}
	
	
	
	var time:Integer = 0;
	var sampleCount:Integer = 0;
	var failureCount:Integer = 0;
	
	def counterUpdater = Timeline {
		repeatCount: Timeline.INDEFINITE
		keyFrames: KeyFrame {
			time: 500ms
			action: function() {
				time = canvas.getCounter( CanvasItem.TIMER_COUNTER ).get();
				sampleCount = canvas.getCounter( CanvasItem.SAMPLE_COUNTER ).get();
				failureCount = canvas.getCounter( CanvasItem.FAILURE_COUNTER ).get();
			}
		}
	}
	
	public-read var stopButton:Button;
	var resetButton:Button;
	var limitButton:Button;
	
	def playButtonState = bind playButton.selected on replace {
		if( playButton.armed ) {
			if( playButtonState ) {
				if( stopped or not canvas.isStarted() ) {
					canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
					stopped = false;
				}
				
				canvas.triggerAction( CanvasItem.START_ACTION );
				
			} else {
				canvas.triggerAction( CanvasItem.STOP_ACTION );
			}
		}
	}
	
	var stopped = true;
	
	init {
		items = [
			Group {
				content: [
					HBox {
						spacing: 4
						content: [
							playButton = ToggleButton {
							    tooltip:Tooltip {
							    	text:"Play/Pause"
							    }
								styleClass: "run-controller-button"
								layoutInfo: LayoutInfo { height: 20 }
								selected: false
								graphic: Group {
									content: [
										PlayShape {
											width: 8
											height: 10
											fill: bind if(playButton.armed or playButton.selected) activeShapeFill else inactiveShapeFill
										}, Rectangle {
											layoutX: 15
											width: 3
											height: 10
											fill: bind if(playButton.armed or playButton.selected) activeShapeFill else inactiveShapeFill
										}, Rectangle {
											layoutX: 22
											width: 3
											height: 10
											fill: bind if(playButton.armed or playButton.selected) activeShapeFill else inactiveShapeFill
										}
									]
								}
							}, stopButton = Button {
							    tooltip:Tooltip {
							    	text:"Stop"
							    }
								styleClass: "run-controller-button"
								layoutInfo: LayoutInfo { height: 20, width: 20 }
								graphic: Rectangle {
									width: 10
									height: 10
									fill: bind if(stopButton.armed) activeShapeFill else inactiveShapeFill
								}
								action: function() {
								    if (stopped)
								    	canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
									playButton.selected = false;
									stopped = true;
									canvas.triggerAction( CanvasItem.COMPLETE_ACTION );
									
								}
							}
						]
					}, if (not testcase) {
						Kitt {
							layoutY: 25
							width: 65
							height: 5
							state: bind state
						}
					} else {
						Rectangle {
						    layoutY: 25
						    width: 65
						    height: 5
						    fill: Color.TRANSPARENT
						} 
						
					}
				]
			/*}, Group {
				content: [
					Rectangle {
						width: 200
						height: 30
					}, Label {
						styleClass: "displayLabel"
						layoutX: 5
						width: 200
						text: "Time             Samples          Failures"
					}, Label {
						styleClass: "displayValue"
						layoutX: 5
						width: 200
						layoutY: 15
						text: bind "{formatSeconds(time)}        {%-20d sampleCount} {failureCount}"
					}
				]*/
			}, if (testcase) linkButton else null, 
			Limiter {
			    small: small
				text: "Time"
				width: if (not small) 105 else 55
				progress: bind if(timeLimit > 0) Math.min( (time as Number) / timeLimit, 1.0) else 0
				value: bind formatSeconds(time)
				limit: bind if(timeLimit > 0) formatSeconds(timeLimit) else null
				layoutInfo: LayoutInfo {
				    hpos:HPos.CENTER
				}
			}, Limiter {
			    small: small
				text: "Samples"
				width: if (not small) 105 else 55
				progress: bind if(sampleLimit > 0) Math.min( (sampleCount as Number) / sampleLimit, 1.0) else 0
				value: bind "{sampleCount}"
				limit: bind if(sampleLimit>0) "{sampleLimit}" else null
				layoutInfo: LayoutInfo {
					hpos:HPos.CENTER
				}
			}, Limiter {
			    //layoutX: -1
			    small: small
				text:  "Failures"
				width: if (not small) 105 else 55
				progress: bind if(failureLimit > 0) Math.min( (failureCount as Number) / failureLimit, 1.0) else 0
				value: bind "{failureCount}"
				limit: bind if(failureLimit>0) "{failureLimit}" else null
				layoutInfo: LayoutInfo {
					hpos:HPos.CENTER
				}
			}, resetButton = if (showResetButton) {
				Button {
				    tooltip:Tooltip {
				    	text:"Reset"
				    }
					styleClass: "run-controller-button"
					layoutInfo: LayoutInfo { height: 20 hpos: HPos.CENTER }
					text: "Reset"
					action: function():Void {
						canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
					}	
				}
			} else null, 
			limitButton = if (showLimitButton) {
				Button {
				    tooltip:Tooltip {
				    	text:"Limit"
				    }
					styleClass: "run-controller-button"
					layoutInfo: LayoutInfo { height: 20 hpos: HPos.CENTER }
					text: "Limit"
					action: function():Void {
						SetCanvasLimitsDialog { runController: this };
					}	
					
				}
			} else null
		];
		
		counterUpdater.play();
	}
	
	function formatSeconds( total:Integer ) {
		var seconds = total;
		def hours = seconds / 3600;
		seconds -= hours*3600;
		def minutes = seconds / 60;
		seconds -= minutes*60;
		
		"{%02d hours}:{%02d minutes}:{%02d seconds}"
	}
	
	override var blocksMouse = true;

	override function create() {
		Group {
			content: [
				Rectangle {
					width: bind width
					height: bind height
					arcWidth: 16
					arcHeight: 16
					fill: bind backgroundFill
					effect: InnerShadow {
                  radius: 5
						color: bind innerShadowColor
					}
				}, hbox
			]
		}
	}
	
	override function getPrefWidth( height:Float ) { hbox.getPrefWidth( height ) + 30 }
	override function getPrefHeight( width:Float ) { 38 }
	
	public function refreshRunner() {
	    timeLimit = canvas.getLimit( CanvasItem.TIMER_COUNTER );
	    		sampleLimit = canvas.getLimit( CanvasItem.SAMPLE_COUNTER );
	    		failureLimit = canvas.getLimit( CanvasItem.FAILURE_COUNTER );
	    		
	    		playButton.selected = canvas.isRunning();
	}
	
}


