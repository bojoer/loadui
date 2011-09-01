/* 
 * Copyright 2011 eviware software ab
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
import com.eviware.loadui.fx.osgi.InspectorManager;

import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.lang.RuntimeException;
import java.lang.Math;
import javax.swing.JComponent;
import java.awt.MouseInfo;

import com.sun.javafx.scene.layout.Region;

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
public class InspectorPanelControl extends InspectorPanel, CustomNode, Resizable {
	
	/**
	 * The minimum allowed height to allow the user to resize the panel to.
	 */
	public var minHeight:Integer = 20;
	
	/**
	 * The maximum allowed height to allow the user to resize the panel to.
	 */
	public var maxHeight:Integer = 500;
	
	/**
	 * True if the panel is currently expanded, false if not.
	 */
	public-read var expanded = true;
	
	/**
	 * The preferred height of the panel using the current width.
	 */
	public-read var prefHeight:Integer;
	
	/**
	 * The currently displayed Inspector.
	 */
	public-read var activeInspector: Inspector = null on replace {
		if( activeInspector != null )
			inspectorHolder.content = getNode(activeInspector.getPanel());
	};
	
	public-init var defaultInspector: String;
	
	/**
	 * The height of the panel.
	 */
	public var inspectorHeight:Integer = 200 on replace {
		prefHeight = getPrefHeight( width ) as Integer;
	};
	var lastGoodHeight:Integer = 0;
	
	def inspectors: Map = new HashMap();
	var shown = 1.0;
	override var translateY = bind ( 1-shown ) * ( height - (resizeBar.boundsInLocal.height-4) );
	
	var buttons: InspectorButton[] = [];
	var inspectorHolder:Stack;
	var buttonBox:HBox;
	var node:VBox;
	var resizeBar:Node;
	var contentPane:Stack;
	
	var resizeY:Number;
	var resizeStart:Number;
	
	postinit {
		InspectorManager.registerPanel( this );
		FX.deferAction( function():Void {
			lastGoodHeight = inspectorHeight;
			prefHeight = getPrefHeight( width ) as Integer;
		} );
	}
	
	def doubleClickTimer = Timeline {
		keyFrames: [
			KeyFrame {
				time: 200ms
			}
		]
	};
	
	
	/**
	 * {@inheritDoc}
	 */
	override function create(): Node {
		node = VBox {
			height: bind inspectorHeight
			width: bind width
			blocksMouse: true
			content: [
				Region{ managed: false, style: "-fx-background-color: #6f6f6f;", width: bind width, height: bind height }
				resizeBar = Stack {
					width: bind width
					content: [
						FXDNode {
							url: "{__ROOT__}images/drag_stripe_1px.fxz"
							scaleX: bind width
							cursor: Cursor.V_RESIZE
							onMousePressed: function( e:MouseEvent ) {
								if( not e.primaryButtonDown )
									return;
								
								if( inspectorHeight >= minHeight )
									lastGoodHeight = inspectorHeight;
								if( not expanded ) {
									inspectorHeight = 0;
									expand();
								}
								resizeStart = inspectorHeight;
								resizeY = MouseInfo.getPointerInfo().getLocation().y;
							}
							onMouseDragged: function( e:MouseEvent ) {
								if( not e.primaryButtonDown )
									return;
								def h = resizeStart + ( resizeY - MouseInfo.getPointerInfo().getLocation().y ) as Integer;
								inspectorHeight = if( h > maxHeight ) maxHeight else h;
							}
							onMouseReleased: function( e:MouseEvent ) {
								if( e.button != MouseButton.PRIMARY )
									return;
								if( inspectorHeight < minHeight ) {
									collapse();
								} else {
									lastGoodHeight = inspectorHeight;
								}
							}
							onMouseClicked: function( e:MouseEvent ) {
								if( e.button != MouseButton.PRIMARY )
									return;
								if( doubleClickTimer.running ) {
									doubleClickTimer.stop();
									toggle();
								} else {
									doubleClickTimer.playFromStart();
								}
							}
						}, 
						
						
						HBox {
							content:
							[
								Stack {
									cursor: Cursor.HAND
									width: bind 30
									layoutInfo: LayoutInfo { hpos: HPos.LEFT }
									content: [
										Rectangle {
											width: 30
											height: 20
											fill: Color.TRANSPARENT
										}, FXDNode {
											url: "{__ROOT__}images/double_arrows.fxz"
											scaleY: bind if( expanded ) -1 else 1
										}
									]
									blocksMouse: true
										onMouseClicked: function( e:MouseEvent ) {
											if( e.button == MouseButton.PRIMARY )
												toggle();
										}
								},
								
								buttonBox = HBox {
									blocksMouse: true
									layoutInfo: LayoutInfo {
										hfill: false
										vfill: false
								      hgrow: Priority.NEVER
								      vgrow: Priority.NEVER
								      width: bind width
								      hpos: HPos.LEFT
								      vpos: VPos.CENTER 
								    }
									spacing: -1
									nodeVPos: VPos.CENTER
									content: bind buttons
								},
							]
						}

						
//						 Text {
//							textOrigin: TextOrigin.TOP
//							translateX: 30
//							content: "System"
//							fill: Color.web("#303030")
//							layoutInfo: LayoutInfo { hpos: HPos.LEFT, vpos: VPos.CENTER }
//						},
						FXDNode {
							url: "{__ROOT__}images/drag_handle.fxz"
						}
					]
				}, 
				
				contentPane = Stack {
					width: bind width
					height: bind inspectorHeight
					content: [
//						Region{ managed: false, style: "-fx-background-color: #00ff00;", width: bind width, height: bind inspectorHeight }
//						Rectangle {
//							fill: Color.rgb( 0x70, 0x70, 0x70 )
//							width: bind contentPane.width
//							height: bind contentPane.height
//						}, GlowButton {
//							layoutX: bind width - 30
//							layoutY: bind 15
//							visible: bind activeInspector.getHelpUrl() != null
//							managed: false
//							width: 20
//							height: 15
//							tooltip: "Open Help page"
//							contentNode: FXDNode {
//								url: "{__ROOT__}images/inspector_help_icon.fxz"
//							}
//							action: function() {
//								openURL( activeInspector.getHelpUrl() );
//							}
//						},
						 VBox {
							spacing: 0
							content: [
//								Region{ managed: false, style: "-fx-background-color: #00ffff;", width: bind width, height: bind height }
//								Region{ managed: true, style: "-fx-background-color: #ff0000;", width: bind width, height: 10 }
//								buttonBox = HBox {
//									layoutInfo: LayoutInfo {
//										hfill: false vfill: false
//								        hgrow: Priority.NEVER vgrow: Priority.NEVER
//								        width: bind width
//								    }
//									spacing: 2
//									nodeVPos: VPos.CENTER
//									content: bind [ Rectangle { fill: Color.TRANSPARENT, width: 10 height: 40 }, buttons ]
//								},
								 inspectorHolder = Stack {
								 	layoutInfo: LayoutInfo { margin: Insets{top:14} }
//								 	content: [Region{ managed: false, style: "-fx-background-color: #ffff00;", width: bind width, height: bind inspectorHeight - 30 }]
//									onLayout: function():Void {
//										for( node in Panel.getManaged( inspectorHolder.content ) )
//											Panel.resizeNode( node, width, inspectorHeight );
//									}
									width: bind width
								   height: bind height
//								   nodeVPos: VPos.TOP
//								       layoutInfo: LayoutInfo {
//									        hfill: true
//									        hgrow: Priority.SOMETIMES
//									    }
								}
//								 inspectorHolder = Panel {
//									onLayout: function():Void {
//										for( node in Panel.getManaged( inspectorHolder.content ) )
//											Panel.resizeNode( node, width, inspectorHeight - buttonBox.height );
//									}
//									width: bind width
//								   height: bind inspectorHeight - buttonBox.height
//								}
							]
						}
					]
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
			maxHeight = inspector.getMaxHeight();
			if( expanded ) {
				if( activeInspector != null ) {
					activeInspector.onHide();
					getButton( activeInspector ).pushed = false;
				}
				inspectorHeight = Math.min( inspectorHeight, maxHeight );
				inspector.onShow();
			}
			activeInspector = inspector;
			getButton( activeInspector ).pushed = true;
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
	
	/**
	 * {@inheritDoc}
	 */
	override function getPrefHeight( width: Float ) {
		resizeBar.boundsInLocal.height + inspectorHeight;
	}
	
	/**
	 * {@inheritDoc}
	 */
	override function getPrefWidth( height: Float ) {
		buttonBox.getPrefWidth( buttonBox.height );
	}
	
	def collapseAnim = Timeline {
		keyFrames: [
			KeyFrame {
				time: 0s
				values: [ shown => 1.0 ]
			}, KeyFrame {
				time: 100ms
				values: [ shown => 0.0 tween Interpolator.EASEIN ]
				action: function() {
					inspectorHeight = 0;
					if( activeInspector != null ) {
						activeInspector.onHide();
						inspectorHolder.content = null;
					}
					if( Sequences.indexByIdentity( node.content, contentPane ) != -1 )
						delete contentPane from node.content;
				}
			}
		]
	};
	
	/**
	 * {@inheritDoc}
	 */
	override function collapse() {
		if( not expanded ) return;
		
		expandAnim.stop();
		collapseAnim.playFromStart();
		expanded = false;
	}
	
	def expandAnim = Timeline {
		keyFrames: [
			KeyFrame {
				time: 0s
				values: [ shown => 0.0 ]
				action: function() {
					if( activeInspector != null ) {
						activeInspector.onShow();
						inspectorHolder.content = getNode(activeInspector.getPanel());
					}
					
					if( Sequences.indexByIdentity( node.content, contentPane ) == -1 )
						insert contentPane into node.content;
				}
			}, KeyFrame {
				time: 100ms
				values: [ shown => 1.0 tween Interpolator.EASEIN ]
			}
		]
	};
	
	/**
	 * {@inheritDoc}
	 */
	override function expand() {
		if( expanded ) return;
		
		collapseAnim.stop();
		expandAnim.playFromStart();
		expanded = true;
	}
	
	function toggle() {
		if( expanded ) {
			collapse();
		} else {
			inspectorHeight = lastGoodHeight;
			expand();
		}
	}
	
	function getButton( inspector: Inspector ):InspectorButton {
		def btn_id = "inspector_button_{inspector.getName()}";
		var btn = buttonBox.lookup( btn_id ) as InspectorButton;
		if( btn == null )
			btn = InspectorButton { id: btn_id, text: inspector.getName(), action: function() {
					if ( inspector == activeInspector )
					{
						toggle();
					}
					else
					{
						if( not expanded )
						{
							toggle();
						}
						selectInspector( inspector );
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
