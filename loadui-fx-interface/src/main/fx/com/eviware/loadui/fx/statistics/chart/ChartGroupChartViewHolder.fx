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
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
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

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.util.ReleasableUtils;

/**
 * Base Chart Node, holds a ChartView.
 *
 * @author dain.nilsson
 */
public class ChartGroupChartViewHolder extends ChartViewHolder {
	override var styleClass = "chart-group-chart-view-holder";
	
	def controlButtons = new ToggleGroup();
	
	def expandCharts = ToggleButton {
		text: "Components"
		toggleGroup: controlButtons
		layoutInfo: LayoutInfo { margin: Insets { right: 50 } }
	}
	
	def expandAgents = ToggleButton {
		text: "Agents"
		toggleGroup: controlButtons
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
	}
	
	public var chartGroupHolder:ChartGroupHolder;
	
	public var chartGroup:ChartGroup on replace {
		chartView = chartGroup.getChartView();
	}
	
	init {
		insert Label { styleClass: "title", text: bind chartGroup.getTitle().toUpperCase() } after vbox.content[0];
	}
	
	override function rebuildChartButtons() {
		[
			expandCharts,
			expandAgents,
			Label { layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
			for( panelFactory in ChartRegistry.getGroupPanels( chartGroup ) ) {
				[
					ToggleButton {
						text: panelFactory.title
						value: panelFactory
						toggleGroup: panelToggleGroup
					}
					if( panelFactory.separator ) Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER } else null
				]
			}
		];
	}
}