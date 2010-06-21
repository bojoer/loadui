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
*ComponentNode.fx
*
*Created on feb 24, 2010, 14:02:20 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Stack;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.fxd.FXDNode;
import javafx.util.Math;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.StylesheetAware;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Movable;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.button.GlowButton;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.menu.button.MenuBarButton;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.ui.layout.LayoutComponentNode;
import com.eviware.loadui.fx.widgets.BasicTitlebarMenuContent;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.CustomNode;

import com.eviware.loadui.fx.widgets.RunController;
import com.eviware.loadui.api.events.ActionEvent;

import com.eviware.loadui.fx.ui.border.*;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TestCaseNode" );

public function create( testCase:SceneItem, canvas:Canvas ):TestCaseNode {
	TestCaseNode { testCase: testCase, canvas: canvas, id: testCase.getId() }
}

def testCaseGrid = Image { url:"{__ROOT__}images/png/testcase-grid.png" };

/**
 * Node to be displayed in a Canvas, representing a TestCaseNode.
 * It can be moved around the Canvas, and its position will be stored in the project file.
 * 
 * @author dain.nilsson
 */
public class TestCaseNode extends CanvasNode {
	
	/**
	 * The SceneItem to display.
	 */
	public-init var testCase:SceneItem;
	
	override var modelItem = bind lazy testCase;
	
	init {
		if( not ( modelItem instanceof SceneItem ) )
			throw new RuntimeException( "TestCaseNode cannot be initialized without setting modelItem to a SceneItem!" );
	}
	
	override var color = Color.web( "#4B89E0" );
	
	def baseRect = Rectangle { fill: Color.TRANSPARENT, width: bind width, height: height };
	
	protected var runControl:RunController = RunController{
	  	showLimitButton: false
	  	showResetButton: false
	      testcaseLinked: testCase.isFollowProject();
	      canvas:testCase
	      small: true
	}
	
	protected var roundedFrame:Node = ImageView { 
		image: testCaseGrid
	} 
	
	def tNode:TerminalNode = TerminalNode { 
		id: testCase.getStateTerminal().getId() 
		canvas: canvas 
		terminal: testCase.getStateTerminal() 
		fill: bind color 
		layoutX: bind runControl.width/2 + 10
	};
	
	var miniatures:Group;
	
	def vbox:VBox = VBox {
		layoutX: 10
	    layoutY: 20
	    spacing: 5
		content: [runControl, roundedFrame]

	}
								
	override var width = bind vbox.width + 20;
	override var height = 295;
	
	public function refreshMinis() {
	    def currentTestCase = MainWindow.instance.testcaseCanvas.canvasItem;
	    MainWindow.instance.testcaseCanvas.canvasItem = testCase;
	    def scale = bind Math.min( ( width - 30 ) / MainWindow.instance.testcaseCanvas.areaWidth, ( height - (50 + runControl.height) ) / MainWindow.instance.testcaseCanvas.areaHeight );
	    var components = bind MainWindow.instance.testcaseCanvas.components;
	    miniatures = Group {
	    	content: for( component in components ) {
	    		Miniature {
	    			layoutX: bind 10 + component.layoutX * scale
	    			layoutY: bind 20 + runControl.height + component.layoutY * scale
	    			width: bind component.layoutBounds.width * scale
	    			height: bind component.layoutBounds.height * scale
	    			fill: bind component.color
	    		} 
	    	}
	    }
	    runControl.playButton.selected = testCase.isRunning();
	    MainWindow.instance.testcaseCanvas.canvasItem = currentTestCase;
	    
	    runControl.refreshRunner();

	}
	
	override function create() {

		refreshMinis();

RoundedRectBorder {
    arc: 30
    accent: Color.TRANSPARENT
    borderColor: bind if ( selected ) Color.web("#535353", .6) else Color.TRANSPARENT
    borderWidth: 0
    backgroundFill: bind if ( selected ) Color.web("#535353", .6) else Color.TRANSPARENT
    borderBottomWidth: 10
    borderLeftWidth: 10
    borderRightWidth: 10
    base: Color.TRANSPARENT
		node: Group {
			content: [
				handle = BaseNode {
					contentNode: TitlebarPanel {
						backgroundFill: bind backgroundFill
						content: bind [
							baseRect,
							toolbarBoxLeft,
							toolbarBoxRight,
							vbox,
							miniatures,
							Rectangle {
								layoutY: bind height - 20
								height: 20
								width: bind width
								fill: bind footerFill
							}
						]
						titlebarColor: color
						//titlebarEffect: bind if( selected ) Selectable.effect else null
						titlebarContent: Label {
							text: bind label
							layoutX: 15
							width: bind width - 30
							height: bind 30
						}
					}
				},
				tNode
			]
			onMouseClicked: function( e:MouseEvent ) {
				if( e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
					AppState.instance.setActiveCanvas( testCase );
				}
			}
		}
		
}
		
	}
	
	public function getStateTerminalNode():TerminalNode {
	    return tNode;
	}
	
	public function lookupTerminalNode(id: String): TerminalNode {
		if( tNode.id == id ){
			return tNode;
		}
		else{
			return null;
		}
	}
	
	override function release() {
		testCase = null;
	}
	
	override var settingsAction = function() {
		SettingsDialog{}.show(testCase);
	}
	
}

class Miniature extends CustomNode {
	public var fill:Paint;
	public var width:Number;
	public var height:Number;
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: bind fill
					width: bind width
					height: 10
				}, Rectangle {
					fill: Color.web("#DBDBDB")
					width: bind width
					height: bind Math.max( 0, height - 10 )
					y: 10
				}
			]
		}
	}
}
