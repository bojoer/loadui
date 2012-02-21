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
package com.eviware.loadui.fx.ui.inspector;

import com.eviware.loadui.api.ui.inspector.InspectorPanel;
import com.eviware.loadui.api.ui.inspector.Inspector;

import javafx.util.Sequences;
import javafx.fxd.FXDNode;
import javafx.scene.Cursor;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.Panel;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Interpolator;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.ext.swing.SwingComponent;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.button.GlowButton;
import com.eviware.loadui.fx.ui.dnd.Movable;
import com.eviware.loadui.fx.osgi.InspectorManager;
import com.eviware.loadui.fx.ui.node.BaseNode;

import javafx.geometry.BoundingBox;
import javafx.scene.layout.Panel;

import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.lang.RuntimeException;
import java.lang.Math;
import javax.swing.JComponent;
import java.awt.MouseInfo;

import javafx.animation.transition.TranslateTransition;
import com.sun.javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.layout.Container;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.inspector.InspectorPanelControl" );

/**
 * A Panel which sits along the bottom of the screen containing several panels, "Inspectors".
 * Only one Inspector is visible at once. Which Inspector to show is controlled using the buttons
 * which contain the names of each available Inspector. 
 * Its visibility can be toggled using the collapse/expand button, and its height can also be
 * resized by the user.
 *
 * @author dain.nilsson
 * @author henrik.olsson
 */
public class InspectorPanelControl extends InspectorPanel, CustomNode {
	
	/**
	 * The height of the top bar (the draggable bar containing the tabs).
	 */
	public def topBarHeight:Integer = 25;
	
	/**
	 * True if the panel is currently expanded, false if not.
	 */
	public-read var expanded = false;
	
	/**
	 * The currently displayed Inspector.
	 */
	public-read var activeInspector: Inspector = null on replace {
		insertInspector();
	};
	
	public function insertInspector():Void {
		if( activeInspector != null )
		{
			java.util.logging.Logger.getLogger( "com.eviware.loadui.fx.MainWindow" ).severe( "Showing inspector: {activeInspector}" );
			java.util.logging.Logger.getLogger( "com.eviware.loadui.fx.MainWindow" ).severe( "Inspector name: {activeInspector.getName()}" );
			activeInspector.onShow();
			inspectorHolder.content = Region { managed: false, width: bind inspectorHolder.width, height: bind inspectorHolder.height, style:"-fx-background-color: #6f6f6f;" };
			
			def node = getNode(activeInspector.getPanel());
			
			maxHeight = Container.getNodeMaxHeight( node );
			minHeight = Container.getNodeMinHeight( node );
			
			insert node into inspectorHolder.content;
		}
	}
	
	override var visible = false;
	
	public-init var defaultInspector: String;
	
	def inspectors: Map = new HashMap();
	
	var buttons: InspectorButton[] = [];
	var inspectorHolder:Stack;
	var buttonBox:HBox;
	var node:VBox;
	var inspectorHeight:Number = 0;

	var lastGoodHeight:Number = -1;
	function getLastGoodHeight():Number {
		if( lastGoodHeight != -1)
		{
			if( scene.height - lastGoodHeight > maxHeight )
				scene.height - maxHeight
			else
				lastGoodHeight;
		} 
		else
			scene.height - 250;
	}
	
	postinit {
		InspectorManager.registerPanel( this );
	}
	
	def doubleClickTimer = Timeline {
		keyFrames: [
			KeyFrame {
				time: 200ms
			}
		]
	};
	
	var rn:Panel;
	public-read var topBar:TopBar;
	var maxHeight:Number = 350;
	var minHeight:Number;
	
	/**
	 * {@inheritDoc}
	 */
	override function create(): Node {
		rn = Panel {
			override var height = bind scene.height on replace {
				topBar.layoutY = height - inspectorHeight - topBarHeight;
			}
			width: bind scene.width
			
			content:
				[
					inspectorHolder = Stack {
						height: bind Math.max( rn.height - (topBar.layoutY + topBar.translateY) - topBarHeight, minHeight )
						layoutY: bind topBar.layoutY + topBar.translateY + topBarHeight
						width: bind rn.width
						nodeVPos: VPos.TOP
						padding: Insets { top:10 right:0 bottom: 0 left: 0 }
						blocksMouse: true
					}
					topBar = TopBar {
						width: bind rn.width
						height: bind topBarHeight
						blocksMouse: true
					}
				]	
		}
	}

	/**
	 * {@inheritDoc}
	 */
	override function addInspector( inspector: Inspector ) {
		def name = inspector.getName();
		def previous = inspectors.put( name, inspector ) as Inspector;
		if( previous == inspector ) {
			return;
		} else if( previous != null ) {
			removeInspector( previous );
		}
		def btn = getButton( inspector );
		var i = 0;
		for( button in buttons ) {
			if( button.id.compareTo( btn.id ) > 0 )
				break;
			i++;
		}
		insert btn before buttons[i];
		
		if( inspector.getName().equals( defaultInspector ) ) {
			selectInspector( inspector );
		}
		
		visible = true;
	}

	/**
	 * {@inheritDoc}
	 */
	override function removeInspector( inspector: Inspector ) {
		if( inspectors.remove( inspector.getName() ) == inspector ) {
			delete getButton( inspector ) from buttons;
			if( activeInspector == inspector and sizeof buttons > 0 ) {
				buttons[0].action();
			}
			if( inspectors.isEmpty() ) {
				visible = false;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	override function getInspectors() {
		Collections.unmodifiableCollection( inspectors.values() );
	}
	
	/**
	 * {@inheritDoc}
	 */
	override function getInspector( name: String ) {
		inspectors.get( name ) as Inspector;
	}

	/**
	 * {@inheritDoc}
	 */
	override function selectInspector( inspector: Inspector ) {
		if( getInspector( inspector.getName() ) == inspector ) {

			if( expanded and activeInspector != null ) {
				activeInspector.onHide();
			}
			getButton( activeInspector ).pushed = false;
			inspector.onShow();
			
			activeInspector = inspector;
			getButton( activeInspector ).pushed = true;
			
			if( scene.height - topBar.layoutY - topBarHeight > maxHeight )
				topBar.layoutY = scene.height - maxHeight - topBarHeight;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	override function getId() {
		id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	override function isExpanded() { expanded }
	
	var collapseAnim:TranslateTransition;

	/**
	 * {@inheritDoc}
	 */
	override function collapse() {
		if( not expanded ) return;
		
		def goalHeight = scene.height - topBar.layoutY - topBarHeight;
		
		collapseAnim = TranslateTransition {
			node: topBar
			toY: goalHeight;
			action: function() {
				topBar.layoutY += topBar.translateY;
				topBar.translateY = 0;
				inspectorHeight = scene.height - topBar.layoutY - topBarHeight;
				expanded = false;
			}
		}
		collapseAnim.playFromStart();
	}
	
	var expandAnim:TranslateTransition;
	
	/**
	 * {@inheritDoc}
	 */
	override function expand() {
		if( expanded ) return;
		
		def goalHeight = getLastGoodHeight() - topBar.layoutY;
		
		expandAnim = TranslateTransition {
			node: topBar
			toY: goalHeight;
			action: function() {
				insertInspector();
				topBar.layoutY += topBar.translateY;
				topBar.translateY = 0;
				inspectorHeight = scene.height - topBar.layoutY - topBarHeight;
				expanded = true;
			}
		}
		
		expandAnim.playFromStart();
		
	}
	
	function toggle() {
		if( expanded ) {
			collapse();
		} else {
			expand();
		}
	}
	
	function getButton( inspector: Inspector ):InspectorButton {
		def btn_id = "inspector_button_{inspector.getName()}";
		var btn = buttonBox.lookup( btn_id ) as InspectorButton;
		if( btn == null )
			btn = InspectorButton { id: btn_id, text: inspector.getName(), blocksMouse: true, action: function() {
					if ( inspector == activeInspector )
					{
						toggle();
					}
					else
					{	
						selectInspector( inspector );		
						if( not expanded )
						{
							toggle();
						}
					}
				}
			};
		
		btn;
	}
	
	function getNode( object: Object ):Node {
		if( object instanceof Node )
			object as Node
		else if( object instanceof JComponent )
			SwingComponent.wrap( object as JComponent )
		else throw new RuntimeException("Unsupported panel type: {object.getClass()}");
	}
}


public class TopBar extends BaseNode, Movable, Resizable {
	
	def container:HBox = HBox {
		height: bind height
		width: bind width
		nodeHPos: HPos.LEFT
		
		content:
		[
			Region {
				managed: false
				width: bind container.width
				height: bind container.height
				style: "-fx-background-insets: 0, 0 0 1 0, 1 0 1 0; -fx-background-color: #333333, #9c9c9c, #555555;"
			}
			Stack {
				nodeHPos: HPos.CENTER
				cursor: Cursor.HAND
				width: 35
				layoutInfo: LayoutInfo { hpos: HPos.LEFT, hgrow: Priority.NEVER }
				content: [
					Rectangle {
						width: 35
						height: 20
						fill: Color.TRANSPARENT
					}, FXDNode {
						url: "{__ROOT__}images/double_arrows.fxz"
						scaleY: bind if( expanded ) -1 else 1
						layoutInfo: LayoutInfo { hpos: HPos.CENTER }
					}
				]
				blocksMouse: true
				onMouseClicked: function( e:MouseEvent ) {
					if( e.button == MouseButton.PRIMARY )
						toggle();
				}
			},
			
			buttonBox = HBox {
				layoutInfo: LayoutInfo {
					hfill: false
					vfill: false
			      hgrow: Priority.NEVER
			      vgrow: Priority.NEVER
			      hpos: HPos.LEFT
			      vpos: VPos.CENTER 
			    }
				spacing: -1
				nodeVPos: VPos.CENTER
				content: bind buttons
			}
		]
	}
	
	override function getPrefHeight( width ):Number {
		container.getPrefHeight( width )
	}
	
	override function getPrefWidth( height ):Number {
		container.getPrefWidth( height )
	}
	
	override function create(): Node {
		container
	}
	
	override var hoverCursor = Cursor.V_RESIZE;
	
	override var onMove =  function() {
		if( topBar.layoutY == scene.height - topBarHeight )
			expanded = false
		else
		{
			lastGoodHeight = topBar.layoutY + topBar.translateY;
			
			if( not expanded )
			{
				expanded = true;
			}
		}
		inspectorHeight = scene.height - (topBar.layoutY + topBar.translateY) - topBarHeight;
	}
	
	override var onGrab = function() {
	  def minY = Math.max( 0, rn.height - maxHeight - topBarHeight );
	  containment = BoundingBox { width: width, minY: minY, height: rn.height - minY };
	 }
	
	postinit {
		addMouseHandler( MOUSE_CLICKED, function( e:MouseEvent ) {
				if( e.clickCount == 2 ) {
					toggle();
				}
			}
		)
	}

}

