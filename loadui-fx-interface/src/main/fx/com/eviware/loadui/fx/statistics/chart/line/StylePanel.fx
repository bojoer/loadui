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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.HPos;

import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridRow;
import com.javafx.preview.layout.GridLayoutInfo;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.control.ColorPicker;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;

/**
 * Panel for changing the style of LineSegments.
 *
 * @author dain.nilsson
 */
public class StylePanel extends Grid {
	public-init var chartGroup:ChartGroup;
	
	override var styleClass = "style-panel";
	override var padding = Insets { top: 10, right: 10, bottom: 10, left: 10 };
	override var hgap = 10;
	override var vgap = 10;
	
	init {
		rows = [
			GridRow { cells: [
				Region { managed: false, width: bind width, height: bind height, styleClass: "style-panel" },
				Label { styleClass: "header-row", text: "Color" },
				Label { styleClass: "header-row", text: "Statistic", layoutInfo: GridLayoutInfo { hspan: 3 } },
				Label { styleClass: "header-row", text: "Width", layoutInfo: GridLayoutInfo { hspan: 2 } },
				Label { styleClass: "header-row", text: "Stroke" }
			] }, for( segment in (chartGroup.getChartView() as LineChartView).getSegments() ) {
				def model = LineChart.getLineSegmentModel( segment );
				var slider:Slider;
				var width:Number = bind slider.value on replace {
					model.setLineWidth( width );
					slider.value = width as Integer;
				}
				var lineColor:Color = model.getLineColor();
				GridRow { cells: [
					ColorPicker {
						color: model.getLineColor();
						onReplace: function( color ):Void {
							model.setLineColor( color );
							lineColor = color;
						}
					}, Label {
						text: segment.getStatistic().getName()
						layoutInfo: LayoutInfo { width: 60 }
					}, Label {
						text: if( segment.getStatistic().getSource() == StatisticVariable.MAIN_SOURCE ) "All" else segment.getStatistic().getSource()
						layoutInfo: LayoutInfo { width: 60 }
					}, Label {
						text: segment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel()
						layoutInfo: LayoutInfo { width: 60 }
					},
					Label {
						text: bind "{width as Integer}px"
						graphic: Rectangle { height: bind width as Integer, width: 15, fill: bind lineColor }
						graphicHPos: HPos.RIGHT
					}, slider = Slider {
						min: 1
						max: 9
						value: model.getLineWidth()
						minorTickCount: 0
						majorTickUnit: 1
						clickToPosition: true
					},
					Label { text: "Stroke" }
				] }
			}
		];
	}
}