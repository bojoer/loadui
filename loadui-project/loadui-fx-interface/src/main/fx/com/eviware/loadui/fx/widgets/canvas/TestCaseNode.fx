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

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import com.javafx.preview.control.MenuItem;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.widgets.RunController;
import com.eviware.loadui.fx.ui.dialogs.TestCaseSettingsDialog;
import com.eviware.loadui.fx.dialogs.CloneTestCaseDialog;
import com.eviware.loadui.fx.util.ImageUtil.*;
import com.eviware.loadui.fx.util.ImageUtil;
import com.eviware.loadui.fx.AppState;

import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;

public function create( testCase:SceneItem, canvas:Canvas ):TestCaseNode {
	TestCaseNode { testCase: testCase, canvas: canvas, id: testCase.getId() }
}

def testCaseGrid = Image { url: "{__ROOT__}images/png/testcase-grid.png" };

/**
 * Node to be displayed in a Canvas, representing a TestCaseNode.
 * It can be moved around the Canvas, and its position will be stored in the project file.
 * 
 * @author dain.nilsson
 */
public class TestCaseNode extends CanvasObjectNode {
	
	/**
	 * The SceneItem to display.
	 */
	public-init protected var testCase:SceneItem on replace {
		canvasObject = testCase;
		//runController.canvas = testCase;
	}
	
	override function release() {
		testCase = null;
	}
	
	override var colorStr = "#4b89e0";
	
	var miniature: Image = null;
	var runController:RunController;
	
	public function loadMiniature():Void {
		def base64: String = testCase.getAttribute( "miniature", "" );
		if( base64.length() > 0 ) {
			miniature = ImageUtil.base64ToFXImage( base64 );
		}
	}
	
	override function create():Node {
		loadMiniature();
		def dialog = super.create();
		body.content = [
			runController = RunController {
			  	showLimitButton: false
			  	showResetButton: false
		      testcaseLinked: testCase.isFollowProject();
		      canvas: bind testCase
		      small: true
			}, Stack {
				visible: bind not compact
				managed: bind not compact
				layoutInfo: LayoutInfo { margin: Insets { top: 5 } }
				content: bind [
					ImageView { 
						image: testCaseGrid
						blocksMouse: true
						onMouseClicked: function( e:MouseEvent ) {
							if( e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
							    open();
							}
						}
					}, ImageView {
						image: miniature
					}
				]
			}
		];
		
		dialog;
	}
	
	override var onSettings = function() {
		new TestCaseSettingsDialog().show( testCase );
	}
	
	override var onClone = function():Void {
		CloneTestCaseDialog { canvasObject: canvasObject };
	}
	
	function open() {
			AppState.byName("MAIN").blockingTask( function():Void {
			AppState.byName("MAIN").setActiveCanvas( testCase );
		}, null, "Initializing Scenario." );
	}
	
	override var menuItems: Node[] = [
		MenuItem {
			text: "Open"
			action: function() { open() }
		}, Separator {
		}, MenuItem {
			text: ##[RENAME]"Rename"
			action: function() { RenameModelItemDialog { 
				labeled: canvasObject
				uniqueInList: for( s in testCase.getProject().getChildren()) s
				uniqueNameWarningText: "Test case with the specified name already exist in project!" 
			} }
		}, MenuItem {
			text: ##[CLONE]"Clone"
			action: function() { onClone() }
		}, MenuItem {
			text: ##[DELETE]"Delete"
			action: function() { DeleteModelItemDialog { modelItem: canvasObject } }
		}, Separator {
		}, MenuItem {
			text: ##[SETTINGS]"Settings"
			action: function() { onSettings() }
		}
	];
}