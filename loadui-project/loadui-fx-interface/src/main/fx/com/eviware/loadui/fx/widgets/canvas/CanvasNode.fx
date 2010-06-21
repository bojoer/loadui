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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
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
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.menu.button.MenuBarButton;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.ui.layout.LayoutComponentNode;
import com.eviware.loadui.fx.widgets.BasicTitlebarMenuContent;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.popup.SeparatorMenuItem;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.CanvasNode" );

/**
 * Node to be displayed in a Canvas, representing a ModelItem.
 * It can be moved around the Canvas, and its position will be stored in the project file.
 * 
 * @author dain.nilsson
 */
public abstract class CanvasNode extends BaseNode, StylesheetAware, Selectable, Movable, ModelItemHolder, EventHandler {
	
	/**
	 * The Canvas that contains the ComponentNode.
	 */
	public-init var canvas:Canvas;
	
	//CSS
	public var backgroundFill:Paint = Color.web("#DBDBDB");
	public var separatorStroke:Paint = Color.web("#C6C6C6");
	public var footerFill:Paint = Color.web("#D3D3D3");
	public var roundedFrameStroke:Paint = Color.web("#898989");
	public var roundedFrameFill:Paint = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.web("#DEDEDE") },
			Stop { offset: 0.4, color: Color.web("#C9C9C9") },
			Stop { offset: 1, color: Color.web("#B5B5B5") }
		]
	};
	public var menuFill:Paint = Color.web("#777777");
	
	
	public-read protected var color:Color = Color.web( "#26a8f9" );
	
	protected var label:String;
	
	protected var settingsAction: function(): Void;
	
	protected var width:Number;
	protected var height:Number;
	
	def menuNode:Menu = Menu {
		tooltip: bind label
		contentNode: Group {
			content: [
				Label {
					textFill: bind if( menuNode.menu.isOpen ) Color.WHITE else menuFill
					text: "Menu"
					height: 15
					vpos: VPos.CENTER
					textWrap: false
				}, MenuArrow {
					fill: bind if( menuNode.menu.isOpen ) Color.WHITE else menuFill
					rotate: 90
					layoutY: 7.5
					layoutX: 40
				}, Rectangle {
					width: 50
					height: 15
					layoutX: -3
					layoutY: -3
					fill: Color.TRANSPARENT
				}
			]
		}
		menu: PopupMenu {
			items: [
				ActionMenuItem {
					text: ##[DELETE]"Rename"
					action: function() { RenameModelItemDialog { modelItem: modelItem } }
				},
				ActionMenuItem {
					text: ##[CLONE]"Clone"
					action: function() {
					    if( modelItem instanceof SceneItem ) { // handle TestCase
					    	CloneTestCaseDialog { canvasObject: modelItem as CanvasObjectItem }
					    };
					    if( modelItem instanceof ComponentItem ) { // handle Component
					    	CloneComponentDialog { canvasObject: modelItem as CanvasObjectItem }
					    };
					}
				} 
				ActionMenuItem {
					text: ##[DELETE]"Delete"
					action: function() { DeleteModelItemDialog { modelItem: modelItem } }
				}
				SeparatorMenuItem{}
                ActionMenuItem {
                    text: "Settings"
                    action: function() { 
                    	if(this instanceof TestCaseNode){
                    		SettingsDialog{}.show(MainWindow.instance.testcaseCanvas.canvasItem);
                    	} 
                    	else{
                    		settingsAction();
                    	}
                    }
                }
			]
		}
	}
	
	public var toolbarItemsLeft:Node[] = [
		menuNode
	] on replace {
		toolbarBoxLeft.width = toolbarBoxLeft.getPrefWidth(-1)
	}
	
	protected def toolbarBoxLeft:HBox = HBox {
		layoutInfo: LayoutInfo { height: 20 }
		spacing: 5
		nodeVPos: VPos.CENTER
		vpos: VPos.CENTER
		layoutX: 15
		content: bind toolbarItemsLeft
	}
	
	public var toolbarItemsRight:Node[] = [
		Line {
			endY: 10
			stroke: bind separatorStroke
		}, GlowButton {
			padding: 3
			tooltip: "Settings"
			contentNode: FXDNode {
				url: "{__ROOT__}images/component_wrench_icon.fxz"
			}
			action: function() { settingsAction() }
		}, Line {
			endY: 10
			stroke: bind separatorStroke
		}, GlowButton {
			padding: 3
			tooltip: "Open Help page"
			contentNode: FXDNode {
				url: "{__ROOT__}images/component_help_icon.fxz"
			}
			action: function() {
				openURL( modelItem.getHelpUrl() );
			}
		}
	] on replace {
		toolbarBoxRight.width = toolbarBoxRight.getPrefWidth(-1)
	}
	
	protected def toolbarBoxRight:HBox = HBox {
		layoutInfo: LayoutInfo { height: 20 }
		spacing: 5
		nodeVPos: VPos.CENTER
		vpos: VPos.CENTER
		layoutX: bind ( width - 20 ) - toolbarBoxRight.width
		content: bind toolbarItemsRight
	}
	
	init {
		if( not FX.isInitialized( modelItem ) )
			throw new RuntimeException( "CanvasNode cannot be initialized without setting modelItem!" );
		
		addKeyHandler( KEY_PRESSED, function( e:KeyEvent ) {
			if( selected and e.code == KeyCode.VK_DELETE )
				DeleteModelItemDialog { modelItem: modelItem }
		} );
		
		modelItem.addEventListener( BaseEvent.class, this );
		label = modelItem.getLabel();
		
		layoutX = Integer.parseInt( modelItem.getAttribute( "gui.layoutX", "0" ) );
		layoutY = Integer.parseInt( modelItem.getAttribute( "gui.layoutY", "0" ) );
	}
	
	override var layoutX on replace {
		modelItem.setAttribute( "gui.layoutX", "{layoutX as Integer}" );
	}
	
	override var layoutY on replace {
		modelItem.setAttribute( "gui.layoutY", "{layoutY as Integer}" );
	}
	
	override function handleEvent( e:EventObject ) {
		if( e instanceof BaseEvent ) {
			def event = e as BaseEvent;
			if( event.getKey() == ModelItem.LABEL ) {
				runInFxThread( function():Void { label = modelItem.getLabel() } );
			} else if( event.getKey() == CanvasObjectItem.RELOADED ) {
				runInFxThread( function():Void { onReloaded() } );
			}
		}
	}
	
	override function release() {
		modelItem = null;
	}
	
	override var blocksMouse = true;
	override var onGrab = function():Void {
		toFront();
		select();
	}
	
	override var onMove = function() { canvas.refreshComponents() };
	
	protected function onReloaded():Void {};
}
