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
*TestCaseMenu.fx
*
*Created on jun 1, 2010, 13:25:45 em
*/

package com.eviware.loadui.fx.ui.menu;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.ui.menu.button.*;
import com.eviware.loadui.fx.widgets.TrashHole;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.popup.SeparatorMenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.popup.SubMenuItem;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.widgets.MiniRunController;
import com.eviware.loadui.fx.widgets.RunController;
import com.eviware.loadui.fx.summary.SummaryReport;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;

import com.eviware.loadui.api.counter.CounterHolder;

public class TestCaseMenu extends HBox {
	def listener = new SummaryListener();

	public var testCase: SceneItem on replace oldtestCase = newTestCase {
		//workspaceLabel = project.getWorkspace().getLabel();
		projectLabel = testCase.getProject().getLabel();
		testCaseLabel = testCase.getLabel();
		summaryEnabled = false;
		
		if( oldtestCase != null )
			oldtestCase.removeEventListener( BaseEvent.class, listener );
		if( newTestCase != null )
			newTestCase.addEventListener( BaseEvent.class, listener );
	}
	
	var workspaceLabel:String = "Workspace";
	var projectLabel:String;
	var testCaseLabel:String;
	
	override var spacing = 3;
	override var nodeVPos = VPos.CENTER;
	
	public var tcMenuFill: Paint = Color.TRANSPARENT;
	public var tcMenuClosedTextFill: Paint = Color.web("#666666");
	public var tcMenuOpenedTextFill: Paint = Color.web("#4d4d4d");
	public var tcMenuClosedArrowFill: Paint = Color.web("#666666");
	public var tcMenuOpenedArrowFill: Paint = Color.web("#4D4D4D");
	public var tcMenuClosedFont: Font = Font{name:"Arial", size:10};
	public var tcMenuOpenedFont: Font = Font{name:"Arial", size:18};
	
	public var projectMenuClosedTextFill: Paint = Color.web("#666666");
	public var projectMenuClosedArrowFill: Paint = Color.web("#666666");
	public var projectMenuClosedFont: Font = Font{name:"Arial", size:10};
	public var workspaceMenuClosedTextFill: Paint = Color.web("#666666");
	public var workspaceMenuClosedArrowFill: Paint = Color.web("#666666");
	public var workspaceMenuClosedFont: Font = Font{name:"Arial", size:10};
	
	override var layoutInfo = LayoutInfo {
		hgrow: Priority.ALWAYS
		vgrow: Priority.NEVER
		hfill: true
		vfill: false
		height: 90
	}
	
	var popup:PopupMenu;
	var summaryEnabled = false;
	
	var testCaseLabelTruncated: Boolean = false on replace {
		if(not testCaseLabelTruncated){
			tcMenuClosedTextFill = Color.web("#666666");
			tcMenuOpenedTextFill = Color.web("#4d4d4d");
		}
		else{
			tcMenuClosedTextFill = LinearGradient {
				endY: 0
				stops: [
					Stop { offset: 0, color: Color.rgb( 0x66, 0x66, 0x66, 1.0 ) },
					Stop { offset: 0.8, color: Color.rgb( 0x66, 0x66, 0x66, 1.0 ) },
					Stop { offset: 0.9, color: Color.rgb( 0x66, 0x66, 0x66, 0.7 ) },
					Stop { offset: 1.0, color: Color.rgb( 0x66, 0x66, 0x66, 0.0 ) },
				]
			}
			tcMenuOpenedTextFill = LinearGradient {
				endY: 0
				stops: [
					Stop { offset: 0, color: Color.rgb( 0x4d, 0x4d, 0x4d, 1.0 ) },
					Stop { offset: 0.8, color: Color.rgb( 0x4d, 0x4d, 0x4d, 1.0 ) },
					Stop { offset: 0.9, color: Color.rgb( 0x66, 0x66, 0x66, 0.7 ) },
					Stop { offset: 1.0, color: Color.rgb( 0x4d, 0x4d, 0x4d, 0.0 ) },
				]
			};
		}
	};
	
	var projectLabelTruncated: Boolean = false on replace {
		if(not projectLabelTruncated){
			projectMenuClosedTextFill = Color.web("#666666");
		}
		else{
			projectMenuClosedTextFill = LinearGradient {
				endY: 0
				stops: [
					Stop { offset: 0, color: Color.rgb( 0x66, 0x66, 0x66, 1.0 ) },
					Stop { offset: 0.75, color: Color.rgb( 0x66, 0x66, 0x66, 1.0 ) },
					Stop { offset: 0.85, color: Color.rgb( 0x66, 0x66, 0x66, 0.75 ) },
					Stop { offset: 1.0, color: Color.rgb( 0x66, 0x66, 0x66, 0.0 ) },
				]
			}
		}
	}
	
	var truncTestCaseLabel: String;
	var truncProjectLabel: String;
	var t: String = bind testCaseLabel on replace {
		var tcWidth: Number = 0;
		testCaseLabelTruncated = false;
		var tmp: String = "";
		for(i in [0..testCaseLabel.length()-1]){
			tmp = "{tmp}{testCaseLabel.substring(i, i + 1)}";
			var tmpText: Text = Text {
				content: tmp
			}
			def tcPrevWidth: Number = tcWidth;
			tcWidth = tmpText.boundsInLocal.width * 18/12;
			if(tcWidth > 120){
				truncTestCaseLabel = tmp.substring(0, tmp.length() - 1);
				testCaseLabelTruncated = true;
				tcWidth = tcPrevWidth;
				break;
			}
		}
		if(not testCaseLabelTruncated){
			truncTestCaseLabel = testCaseLabel;
		}
		
		tmp = "";
		for(i in [0..projectLabel.length()-1]){
			tmp = "{tmp}{projectLabel.substring(i, i + 1)}";
			var tmpText: Text = Text {
				content: tmp
			}
			if(tmpText.boundsInLocal.width > 10 + tcWidth * 12 / 10){
				truncProjectLabel = tmp.substring(0, tmp.length() - 1);
				projectLabelTruncated = true;
				break;
			}
		}
		if(not projectLabelTruncated){
			truncProjectLabel = projectLabel;
		}
	}
	
	var menu:Menu;
	
	init {
		var menuContent:Node;
		
		content = [
			ImageView {
				image: Image {
					url: "{__ROOT__}images/png/toolbar-background.png"
					width: width
				}
				fitWidth: bind width
				managed: false
			}, Rectangle {
				width: 78
				height: 93
				fill: Color.rgb( 0, 0, 0, 0.1 )
				managed: false
			}, Rectangle {
				width: bind width
				height: 20
				fill: Color.TRANSPARENT
				managed: false
				onMousePressed: function ( e:MouseEvent ) { 
					// This is disabled until issue LUCO-620 is fixed.
					//AppState.instance.displayWorkspace(); 
				}
			}, Rectangle {
				width: bind width
				height: 20
				layoutY: 20
				fill: Color.TRANSPARENT
				managed: false
				onMousePressed: function ( e:MouseEvent ) {
				   // This is disabled until issue LUCO-620 is fixed.
					//AppState.instance.setActiveCanvas( testCase.getProject() );
				}
			}, Label {
				layoutInfo: LayoutInfo {
					width: 95
				}
			}, VBox {
				content: [
					Label {
						text: bind workspaceLabel
						textFill: bind workspaceMenuClosedTextFill
						font: bind workspaceMenuClosedFont
						layoutInfo: LayoutInfo {
							height: 20
							margin: Insets { left: 3 }
						}
					}, HBox {
						nodeVPos: VPos.CENTER;
						content: [
							Label {
								text: bind truncProjectLabel
								textFill: bind projectMenuClosedTextFill
								font: bind projectMenuClosedFont
								layoutInfo: LayoutInfo {
									height: 20
									margin: Insets { left: 3 }
									width: bind menu.layoutBounds.width
								}
							}, MiniRunController {
								canvas: bind testCase.getProject()
							}
						]
					}, HBox {
						layoutInfo: LayoutInfo {
							hgrow: Priority.ALWAYS
							vgrow: Priority.NEVER
							hfill: true
							vfill: false
							height: 50
						}
						spacing: 3
						nodeVPos: VPos.CENTER
						content: [
							menu = Menu {
								contentNode: Group {
									content: [
										Rectangle {
											width: bind menuContent.boundsInLocal.width + 6
											height: bind menuContent.boundsInLocal.height + 6
											fill: bind tcMenuFill
										}, menuContent = HBox {
											layoutX: 3
											layoutY: 3
											nodeVPos: VPos.CENTER
											spacing: 5 
											content: [
												Label {
													textFill: bind if( popup.isOpen ) tcMenuOpenedTextFill else tcMenuClosedTextFill
													text: bind truncTestCaseLabel                      
													font: bind tcMenuOpenedFont
												} 
												MenuArrow { 
													fill: bind if( popup.isOpen ) tcMenuOpenedArrowFill else tcMenuClosedArrowFill
													rotate: 90
												}
											]
										}
									]
								}
								menu: popup = PopupMenu {
									items: [
										ActionMenuItem {
											text: "Rename"
											action: function() { 
												RenameModelItemDialog { modelItem: MainWindow.instance.testcaseCanvas.canvasItem as SceneItem }
											}
										}
										ActionMenuItem {
											text: "Clone"
											action: function() {
												def copy = testCase.getCanvas().duplicate( testCase ) as SceneItem;
												def layoutX = Integer.parseInt( testCase.getAttribute( "gui.layoutX", "0" ) ) + 50;
												def layoutY = Integer.parseInt( testCase.getAttribute( "gui.layoutY", "0" ) ) + 50;
												copy.setAttribute( "gui.layoutX", "{ layoutX as Integer }" );
												copy.setAttribute( "gui.layoutY", "{ layoutY as Integer }" );
												AppState.instance.setActiveCanvas( copy );
											}
										}
										ActionMenuItem {
											text: "Delete"
											action: function() { 
												DeleteModelItemDialog { 
													modelItem: MainWindow.instance.testcaseCanvas.canvasItem as SceneItem
													onOk: function(): Void {
														AppState.instance.setActiveCanvas( testCase.getProject() );
													} 
												} 
											}
										}
										SeparatorMenuItem{}
										ActionMenuItem {
											text: "Settings"
											action: function() { 
												new SettingsDialog().show(testCase); 
											}
										}
										SeparatorMenuItem{}
										ActionMenuItem {
											text: "Close"
											action: function() {
												AppState.instance.setActiveCanvas( testCase.getProject() );
											}
										}
									]
								}
							}, RunController {
								testcase: true
								canvas: bind testCase
							}, Label {
								layoutInfo: LayoutInfo {
									hgrow: Priority.ALWAYS
									hfill: true 
								}
							}, SeparatorButton {
								height: bind height;
							}, TrashHole {
							}, SeparatorButton {
								height: bind height;
							}, MenubarButton {
								shape: "M0,0 L0,12 10,12, 10,0 0,0 M4,13 L4,16 14,16 14,4 11,4 11,13 4,13"
								tooltip: Tooltip { text: ##[WRENCH]"Summary Report" }
								action: function() {
									if( testCase.getSummary() != null ) {
										println("Viewing TestCase summary");
										SummaryReport{ select: testCase.getLabel(), summary: testCase.getSummary() }
									} else if( testCase.getProject().getSummary() != null ) {
										println("Viewing Project summary");
										SummaryReport{ select: testCase.getProject().getLabel(), summary: testCase.getProject().getSummary() }
									} else {
										println("No summary available");
									}
								}
								disable: bind not summaryEnabled
							}, MenubarButton {
								shape: "M0.93,5.41 C1.87,6.34 3.24,6.56 4.39,6.08 L6.63,8.50 L10.79,12.73 L12.17,14.55 C12.19,14.58 12.20,14.61 12.22,14.65 C12.29,14.77 12.38,14.87 12.48,14.96 L12.49,14.96 L12.49,14.96 C12.95,15.40 13.64,15.50 14.19,15.17 C14.85,14.76 15.04,13.87 14.61,13.17 C14.50,12.99 14.36,12.85 14.20,12.73 L12.78,11.51 L8.49,7.06 L6.08,4.39 C6.96,2.28 6.96,2.47 5.40,0.92 C4.60,0.13 3.49,-0.15 2.46,0.08 L4.20,1.81 L4.19,3.14 L3.15,4.19 L1.81,4.21 L0.08,2.48 C-0.15,3.50 0.13,4.61 0.93,5.41 Z M12.86,12.99 C13.36,12.68 14.01,12.84 14.32,13.33 C14.62,13.83 14.47,14.49 13.97,14.79 C13.47,15.10 12.82,14.94 12.51,14.45 C12.20,13.95 12.36,13.29 12.86,12.99 Z"
								tooltip: Tooltip { text: ##[SETTINGS]"Settings" }
								action: function():Void { new SettingsDialog().show(testCase) }
							}, MenubarButton {
								shape: "M9.40,9.51 L6.80,9.51 L6.80,9.25 C6.80,8.81 6.85,8.45 6.95,8.18 C7.05,7.90 7.20,7.65 7.40,7.42 C7.60,7.19 8.04,6.79 8.73,6.22 C9.10,5.92 9.29,5.64 9.29,5.39 C9.29,5.14 9.21,4.95 9.06,4.81 C8.92,4.67 8.69,4.60 8.39,4.60 C8.07,4.60 7.80,4.71 7.59,4.92 C7.38,5.13 7.24,5.51 7.18,6.04 L4.53,5.71 C4.62,4.74 4.97,3.95 5.59,3.36 C6.21,2.76 7.16,2.47 8.43,2.47 C9.43,2.47 10.23,2.67 10.84,3.09 C11.67,3.65 12.08,4.40 12.08,5.33 C12.08,5.72 11.98,6.09 11.76,6.45 C11.55,6.81 11.11,7.25 10.45,7.77 C9.99,8.14 9.70,8.43 9.58,8.65 C9.46,8.87 9.40,9.16 9.40,9.51 Z M6.71,10.20 L9.49,10.20 L9.49,12.66 L6.71,12.66 Z"
								tooltip: Tooltip { text: ##[HELP]"Help Page" }
								action: function():Void { openURL("http://www.eviware.com") }
							}, MenubarButton {
								shape: "M11.94,1.71 L10.24,-0.00 5.97,4.26 1.71,-0.00 -0.00,1.71 4.26,5.97 -0.00,10.24 1.71,11.94 5.97,7.68 10.24,11.94 11.94,10.24 7.68,5.97 11.94,1.71"
								tooltip: Tooltip { text: ##[CLOSE_PROJECT]"Close Project" }
								action: function():Void { AppState.instance.setActiveCanvas( testCase.getProject() ) }
				         }, Label {
								layoutInfo: LayoutInfo {
									width: 10
								}
							}
						]
					}
				]
			}
		];
	}
}

class SummaryListener extends EventHandler {
	override function handleEvent( e:EventObject ) { 
    	var event = e as BaseEvent;
    	if(event.getKey().equals(CanvasItem.START_ACTION)){
    		runInFxThread( function(): Void { summaryEnabled = false } );
    	} 
    	else if(event.getKey().equals(CanvasItem.SUMMARY)){
    		runInFxThread( function(): Void { summaryEnabled = testCase.getSummary() != null } );
    	}
    	else if(event.getKey().equals(ModelItem.LABEL)){
			runInFxThread( function(): Void {
		 		if(event.getSource() instanceof SceneItem){
		 			testCaseLabel = testCase.getLabel();
		 		}
			});
		}
		else if (event.getKey().equals(CounterHolder.COUNTER_RESET_ACTION)){
			runInFxThread( function(): Void { summaryEnabled = false } );
		}
	}
}