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
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Separator;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.util.Sequences;

import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;
import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteDeletablesDialog;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.traits.Releasable;;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;

import java.util.EventObject;
import java.util.ArrayList;

/**
 * Base Chart Node, holds a ChartView.
 *
 * @author dain.nilsson
 */
public class ChartGroupChartViewHolder extends ChartViewHolder, WeakEventHandler {
	
	def workspaceListener = WorkspaceListener{}
	
	def workspace: WorkspaceItem = bind MainWindow.instance.workspace on replace oldVal {
		oldVal.removeEventListener( BaseEvent.class, workspaceListener );
		workspace.addEventListener( BaseEvent.class, workspaceListener );
	}
	
	override var styleClass = "chart-group-chart-view-holder";
	
	def controlButtons = new ToggleGroup();

	var hasCharts: Boolean on replace {
		if( not hasCharts ) {
			expandCharts.selected = false;
			if(selectedToggle == expandCharts ) {
				controlButtons.selectedToggle = null;
			}
		}
	}

	def expandCharts = ToggleButton {
		text: "Components"
		toggleGroup: controlButtons
		layoutInfo: LayoutInfo { margin: Insets { right: 50 } }
		disable: bind not hasCharts
	}
	
	var hasAgents: Boolean on replace {
		if( not hasAgents ){
			expandAgents.selected = false;
			if(selectedToggle == expandAgents ) {
				controlButtons.selectedToggle = null;
			}
		}
	}
	
	def agentsExpanded: Boolean = bind chartGroupHolder.expandAgents on replace {
		if( agentsExpanded and not hasAgents ){
			chartGroupHolder.toggleAgentExpand();
		}
	}
	
	def expandAgents = ToggleButton {
		text: "Agents"
		toggleGroup: controlButtons
		disable: bind not hasAgents
	}
	
	def selectedToggle = bind controlButtons.selectedToggle on replace oldToggle {
		if( selectedToggle != null ) {
			if( selectedToggle == expandCharts and not chartGroupHolder.expandGroups ) {
				chartGroupHolder.toggleGroupExpand();
			} else if( selectedToggle == expandAgents and not chartGroupHolder.expandAgents ) {
				chartGroupHolder.toggleAgentExpand();
			}
		} else {
			if( oldToggle == expandCharts ) {
				chartGroupHolder.toggleGroupExpand();
			} else if( oldToggle == expandAgents ) {
				chartGroupHolder.toggleAgentExpand();
			}
		}	
	}
	
	def expandState = bind if( chartGroupHolder.expandGroups ) 0 else if( chartGroupHolder.expandAgents ) 1 else 2 on replace {
		controlButtons.selectedToggle = if( expandState == 0 ) expandCharts else if( expandState == 1 ) expandAgents else null;
		hasCharts = chartGroup.getChildren().size() > 0;
		hasAgents = chartGroup.getSources().size() > 0;
	}
	
	public var chartGroupHolder:ChartGroupHolder;
	
	public var chartGroup:ChartGroup on replace {
		chartView = chartGroup.getChartView();
		updateSubLabel();
		chartGroup.addEventListener(
			CollectionEvent.class,
			this
		);
	}
	
	function updateSubLabel():Void
	{
		def count = chartGroup.getChildCount();
		if( count > 1 )
		{
			subLabel.text = "Group ({Integer.toString( count )})";
		}
		else if( count == 1 )
		{
			subLabel.text = ModelUtils.getLabelHolder( chartGroup.getChildren().iterator().next().getOwner() ).label;
		}
		else
		{
			subLabel.text = "";
		}
	}
	
	def popup:PopupMenu = PopupMenu {
		items: contextMenuItems()
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
	
	init {
		var menuButton:MenuButton;
		insert menuButton = MenuButton {
			styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
			style: "-fx-text-fill: #bababa;"
			text: bind label.toUpperCase()
			items: contextMenuItems();
		} after vbox.content[0];
		
		addMouseHandler( MOUSE_PRESSED, function( e:MouseEvent ):Void {
			if( e.secondaryButtonDown ) popup.show( this, e.screenX, e.screenY );
		} );
	}
	
	function contextMenuItems():Node[] {
		[
			MenuItem {
				text: "Rename"
				action: function():Void {
					RenameModelItemDialog { scene: scene, labeled: chartGroup }
				}
			}, MenuItem {
				text: "Delete"
				action: function():Void {
					DeleteDeletablesDialog { hostScene: scene, deletables: chartGroupHolder }
				}
			}
		]
	}
	
	override function rebuildChartButtons() {
		[
			expandCharts,
			expandAgents,
			super.rebuildChartButtons()
		];
	}
	
	override function getPanels() {
		ChartRegistry.getGroupPanels( chartGroup )
	}
	
	override function handleEvent( e: EventObject ) {
		FxUtils.runInFxThread( function():Void {
			updateSubLabel();
		});
		def event = e as BaseEvent;
		if( ChartGroup.CHILDREN == event.getKey() ) {
			runInFxThread( function():Void {
				hasCharts = chartGroup.getChildren().size() > 0;
				hasAgents = chartGroup.getSources().size() > 0;
			} );
		}
	}
	
	postinit{
		hasCharts = chartGroup.getChildren().size() > 0;
		hasAgents = chartGroup.getSources().size() > 0;
	}
}

class WorkspaceListener extends EventHandler {
	override function handleEvent( e:EventObject ) {
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( WorkspaceItem.AGENTS == event.getKey() ) {
				runInFxThread( function():Void {
					hasAgents = chartGroup.getSources().size() > 0;
				});
			}
		}
	}
}
