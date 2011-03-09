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

import javafx.scene.Node;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.util.ModelUtils;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;

public class SegmentButton extends BaseNode, Resizable {
	override var styleClass = "segment-button";
	override var width on replace { button.width = width }
	override var height on replace { button.height = height }
	public var compactSegments = true;
	public-init var chartView:LineChartView;
	
	def button = Button {
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };
		blocksMouse: false
		action: function():Void {
			//TODO: Implement segment panel.
			println("TODO");
		}
	}
	
	var lineColor:Color = bind model.color on replace {
		button.style = "-fx-inner-border: {FxUtils.colorToWebString(lineColor)};";
	}
	
	public-init var model:LineSegmentChartModel on replace {
		def statistic = model.segment.getStatistic();
		button.graphic = HBox {
			content: [
				Label {
					text: statistic.getName()
					layoutInfo: LayoutInfo { width: 60 }
				}, Label {
					text: if( statistic.getSource() == StatisticVariable.MAIN_SOURCE ) "All" else statistic.getSource()
					layoutInfo: LayoutInfo { width: 40 }
					visible: bind not compactSegments
					managed: bind not compactSegments
				}, Label {
					text: bind ModelUtils.getLabelHolder( statistic.getStatisticVariable().getStatisticHolder() ).label
					layoutInfo: LayoutInfo { width: 60 }
					visible: bind not compactSegments
					managed: bind not compactSegments
				}
			]
		}
	}
	
	override function create():Node {
		button;
	}
	
	override function getPrefHeight( width:Number ):Number {
		button.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		button.getPrefWidth( height )
	}
	
	override function doLayout():Void {
	}
}