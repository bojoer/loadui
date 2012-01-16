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
import javafx.scene.control.CheckBox;
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
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.util.Math;
import javafx.scene.text.Font;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.resources.PlayShape;
import com.eviware.loadui.fx.ui.resources.SlashShape;
import com.eviware.loadui.fx.ui.Kitt;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.dialogs.SetCanvasLimitsDialog;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.FormattingUtils;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.util.TestExecutionUtils;

import java.util.EventObject;
import javafx.fxd.FXDNode;
import com.eviware.loadui.api.model.SceneItem;

import javafx.scene.control.Tooltip;

import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.dialogs.ProjectSettingsDialog;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.RunController" );

public class RunController extends BaseNode, Resizable, TimerController {

	override var styleClass = "run-controller";
	
	var stopped = true;

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
				font: Font.font("Amble", 8)
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
	var requestCount:Integer = 0;
	var failureCount:Integer = 0;
	
	def counterUpdater = Timeline {
		repeatCount: Timeline.INDEFINITE
		keyFrames: KeyFrame {
			time: 500ms
			action: function() {
				time = canvas.getCounter( CanvasItem.TIMER_COUNTER ).get();
				requestCount = canvas.getCounter( CanvasItem.REQUEST_COUNTER ).get();
				failureCount = canvas.getCounter( CanvasItem.FAILURE_COUNTER ).get();
			}
		}
	}
	
	var resetButton:Button;
	var limitButton:Button;
	var cancelling = false;
	def playButtonState = bind playButton.selected on replace {
		if( playButton.armed and not cancelling) {
			if( playButtonState ) {
				if( not canvas.isStarted() ) {
					canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
				}
				
				if( canvas.getProject().getAttribute( ProjectSettingsDialog.IGNORE_INVALID_CANVAS, "false" ) == "false" ) {
					var valid:Boolean = checkForTriggerAndRunner(canvas);
				
					if (not valid and canvas instanceof ProjectItem) {
						var project:ProjectItem = canvas as ProjectItem;
						for (testcase in project.getScenes()) {
							if (checkForTriggerAndRunner(testcase)) {
								valid = true;
								break;
							}
						}
					}
					
					if (not valid) {
						var type = if (canvas instanceof ProjectItem) "Project" else "Scenario";
						var checkbox:CheckBox;
						var dlg:Dialog = Dialog {
							title: "Start {type}";
							content: [
								Label { text: "Your {type} currently does not seem to generate any load, \n be sure to add a Generator and connect it to a Runner component to get going!" }
								checkbox = CheckBox {
									selected: false
									text: "Don't show this dialog again"
								}
							]
							onOk: function():Void {
								if( checkbox.selected ) canvas.getProject().setAttribute( ProjectSettingsDialog.IGNORE_INVALID_CANVAS, "true" );
								if(TestExecutionUtils.startCanvas( canvas ) == null) FX.deferAction(function():Void { playButton.selected = false } );
								dlg.close();
							}
							
							onClose: function():Void {
								playButton.selected = false;
							}
						}
					} else {
						if(TestExecutionUtils.startCanvas( canvas ) == null) FX.deferAction(function():Void { playButton.selected = false } );
					}
				} else {
					if(TestExecutionUtils.startCanvas( canvas ) == null) FX.deferAction(function():Void { playButton.selected = false } );
				}
			} else {
				TestExecutionUtils.stopCanvas( canvas );
				stopped = true;
			}
		}
	}
	
	def runningTask = new RunningTask();
	def testRunner = BeanInjector.getBean( TestRunner.class ) on replace {
		testRunner.registerTask( runningTask, Phase.START, Phase.STOP );
	}
	
	init {
		items = [
			playButton = ToggleButton {
				layoutInfo: LayoutInfo { height: 33, width: 33, margin: Insets { top: -2, bottom: 2 } }
				styleClass: "execution-button"
				selected: false
				graphic: ExecutionGraphic { layoutInfo: LayoutInfo { height: 33, width: 33 }, running: bind (playButton.armed or playButton.selected) }
				tooltip:Tooltip { text:"Play/Stop" }
			},
			if (testcase) linkButton else null,
			Limiter {
				small: small
				text: "Time"
				width: if (not small) 105 else 55
				progress: bind if(timeLimit > 0) Math.min( (time as Number) / timeLimit, 1.0) else 0
				value: bind FormattingUtils.formatTime(time)
				limit: bind if(timeLimit > 0) FormattingUtils.formatTime(timeLimit) else null
				layoutInfo: LayoutInfo {
					hpos:HPos.CENTER
				}
			}, Limiter {
				small: small
				text: "Requests"
				width: if (not small) 105 else 55
				progress: bind if(requestLimit > 0) Math.min( (requestCount as Number) / requestLimit, 1.0) else 0
				value: bind "{requestCount}"
				limit: bind if(requestLimit>0) "{requestLimit}" else null
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
	
	function checkForTriggerAndRunner(canvas:CanvasItem):Boolean {
		var foundTrigger:Boolean = false;
		var foundRunner:Boolean = false;
		for (comp in canvas.getComponents()) {
			var component:ComponentItem = comp as ComponentItem;
			if (component.getCategory().equalsIgnoreCase(RunnerCategory.CATEGORY) or component.getType().contains("Runner")) {
				foundRunner = true;
			} else if (component.getCategory().equalsIgnoreCase(GeneratorCategory.CATEGORY)) {
				foundTrigger = true;
			}
			if (foundTrigger and foundRunner) {
				break;
			}
		}
		foundTrigger and foundRunner;
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
}

class RunningTask extends TestExecutionTask {
	override function invoke( execution, phase ):Void {
		if( execution.getCanvas() == canvas ) {
			if( phase == Phase.START ) {
				FxUtils.runInFxThread( function():Void {
					playButton.selected = canvas.isRunning();
				} );
			} else if( phase == Phase.STOP ) {
				FxUtils.runInFxThread( function():Void {
					playButton.selected = false;
				} );
			}
		}
	}
}