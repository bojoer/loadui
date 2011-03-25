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
package com.eviware.loadui.fx.statistics.manager;

import javafx.scene.Node;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.util.Sequences;
import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.ActivityLed;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.FxLabeled;
import com.eviware.loadui.fx.ui.resources.DialogPanel;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;

import java.awt.image.BufferedImage;
import javafx.ext.swing.SwingUtils;

def defaultImage = Image { url: "{__ROOT__}images/png/default-chart-thumbnail.png" };

public class ResultNodeBase extends BaseNode, FxLabeled {
	var iconImage = defaultImage;
	def iconListener = new IconListener();
	
	public var execution:Execution on replace oldExecution {
		if( oldExecution != null ) {
			execution.removeEventListener( BaseEvent.class, iconListener );
		}
		
		execution.addEventListener( BaseEvent.class, iconListener );
		def icon = execution.getIcon();
		iconImage = if( icon != null )
			SwingUtils.toFXImage(icon as BufferedImage)
		else
			defaultImage;
	}
	
	override var label = "Execution";
	
	public var active = false;
	
	protected var menuButton:MenuButton;
	
	override var styleClass = "result-node-base";
	
	init {
		addMouseHandler( MOUSE_PRIMARY_CLICKED, function( e:MouseEvent ):Void {
			if( e.clickCount == 2 ) {
				StatisticsWindow.execution = execution;
				AppState.byName( "STATISTICS" ).transitionTo( StatisticsWindow.STATISTICS_VIEW, AppState.ZOOM_WIPE );
			}
		} );
		
		addMouseHandler( MOUSE_CLICKED, function( e:MouseEvent ):Void {
			if( e.button == MouseButton.SECONDARY )
			{
				popup.show( this, e.screenX, e.screenY );
			}
		} );
	}
	
	protected function generateMenu():Node[] {
		MenuItem {
		text: ##[OPEN]"Open"
		action: function() {
				StatisticsWindow.execution = execution;
				AppState.byName( "STATISTICS" ).transitionTo( StatisticsWindow.STATISTICS_VIEW, AppState.ZOOM_WIPE );
			}
		}
	}
	
	def popup:PopupMenu = PopupMenu {
		items: generateMenu()
		onShowing: function():Void {
			if ( Sequences.indexOf( AppState.byScene( scene ).overlay.content, popup ) == -1 )
			{
				insert popup into AppState.byScene( scene ).overlay.content;				
			}
		},
		onHiding: function():Void {
			delete popup from AppState.byScene( scene ).overlay.content;
		}
	};
	
	override function create():Node {
		DialogPanel {
			layoutInfo: LayoutInfo { width: 155, height: 108 }
			body: VBox {
				padding: Insets { left: 8, right: 8, top: 5, bottom: 8 }
				spacing: 8
				content: [ 
					ActivityLed { active: bind active, managed: false, layoutX: 8, layoutY: 12 },
					menuButton = MenuButton {
						layoutInfo: LayoutInfo { margin: Insets { left: 8 } }
						styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
						//graphic: ActivityLed { active: bind active } //Bug in MenuButton causing incorrect margins for the "arrow" when using a graphic.
						text: bind label.toUpperCase();
						items: 
						[
							generateMenu()
						]
					},
					Stack {
						content: [
							Region {
								styleClass: "graph"
							},
							ImageView { image: bind iconImage },
							SVGPath {
								layoutInfo: LayoutInfo {
									margin: Insets { left: 5, top: 5 }
									vpos: VPos.TOP
									hpos: HPos.LEFT
								}
								fill: Color.rgb( 255,255,255,0.15 )
								content: "m 0.0,0.0 c -1.807,0.24262 -2.54911,1.55014 -3.07444,3.07443 l 0,43.52751 c 0.37619,1.83599 2.42728,3.12823 4.69256,0 12.25299,-33.96285 30.46724,-37.6855 90.61489,-46.60194 z"
							},
							popup
						]
					}
				]
			}
		}
	}
	
	override function toString():String {
		label
	}
}

class IconListener extends WeakEventHandler {
	override function handleEvent( e ) {
		def event = e as BaseEvent;
		if( Execution.ICON.equals( event.getKey() ) ) {
			runInFxThread( function():Void {
				def icon = execution.getIcon();
				iconImage = if( icon != null )
					 SwingUtils.toFXImage(icon as BufferedImage)
				else
					defaultImage;
			} );
		}
	}
}