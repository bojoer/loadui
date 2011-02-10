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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Draggable;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

public class DeletableSegmentButton extends SegmentButton, Draggable, Deletable {
	override var revert = false;
	override var confirmDelete = false;
	
	def contextMenu:PopupMenu = PopupMenu {
		items: [
			MenuItem {
				text: "Remove",
				action: function():Void {
					deleteObject();
				}
			}
		]
		onShowing: function():Void {
			insert contextMenu into AppState.byScene( scene ).overlay.content;
		}
		onHiding: function():Void {
			delete contextMenu from AppState.byScene( scene ).overlay.content;
		}
	}
	
	init {
		addMouseHandler( MOUSE_CLICKED, function( e:MouseEvent ):Void {
			if( e.button == MouseButton.SECONDARY )
				contextMenu.show( this, e.screenX, e.screenY );
		} );
	}
	
	override function doDelete():Void {
		(chartView as ConfigurableLineChartView).removeSegment( model.segment );
	}
}