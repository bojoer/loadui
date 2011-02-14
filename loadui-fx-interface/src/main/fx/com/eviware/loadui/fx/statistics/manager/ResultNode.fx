/*
*ResultNode.fx
*
*Created on feb 10, 2011, 16:08:20 em
*/

package com.eviware.loadui.fx.statistics.manager;

import javafx.scene.Node;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;
import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.DialogPanel;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.api.statistics.store.Execution;

public class ResultNode extends BaseNode, Draggable, Deletable {
	public-init var execution:Execution on replace {
		label = execution.getLabel();
	}
	
	var label:String = "Execution";
	
	init {
		addMouseHandler( MOUSE_PRIMARY_CLICKED, function( e:MouseEvent ):Void {
			if( e.clickCount == 2 ) {
				StatisticsWindow.execution = execution;
				AppState.byName( "STATISTICS" ).transitionTo( StatisticsWindow.STATISTICS_VIEW, AppState.ZOOM_WIPE );
			}
		} );
	}
	
	override function create():Node {
		var menuButton:MenuButton;
		
		DialogPanel {
			layoutInfo: LayoutInfo { width: 155, height: 108 }
			body: VBox {
				padding: Insets { left: 8, right: 8, top: 5 }
				spacing: 10
				content: [
					menuButton = MenuButton {
						styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
						text: bind label.toUpperCase();
						items: MenuItem {
							text: ##[OPEN]"Open"
							action: function() {
								StatisticsWindow.execution = execution;
								AppState.byName( "STATISTICS" ).transitionTo( StatisticsWindow.STATISTICS_VIEW, AppState.ZOOM_WIPE );
							}
						}
					}
				]
			}
			opacity: bind if( dragging ) 0.8 else 1
		}
	}
	
	override function doDelete():Void {
		execution.delete();
	}
	
	override function toString():String {
		//execution.toString();
		"An Execution"
	}
}