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
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Sequences;
import javafx.util.Math;

import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridRow;
import com.javafx.preview.layout.GridLayoutInfo;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.control.ColorPicker;
import com.eviware.loadui.fx.ui.form.fields.SelectField;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;

def SCALES = [ "0.000001", "0.00001", "0.0001", "0.001", "0.01", "0.1", "1", "10", "100", "1000", "10000", "100000", "1000000" ];

/**
 * Panel for changing the scale of LineSegments.
 *
 * @author dain.nilsson
 */
public class ScalePanel extends Grid {
	public-init var segments:LineSegment[];
	
	override var styleClass = "scale-panel";
	override var padding = Insets { top: 10, right: 10, bottom: 10, left: 10 };
	override var hgap = 10;
	override var vgap = 10;
	
	init {
		rows = [
			GridRow { cells: [
				Region { managed: false, width: bind width, height: bind height, styleClass: "scale-panel" },
				Label { styleClass: "header-row", text: "Statistic", layoutInfo: GridLayoutInfo { hspan: 3 } },
				Label { styleClass: "header-row", text: "Scale" },
				Label { styleClass: "header-row", text: "0", layoutInfo: LayoutInfo { width: 60 } },
				Label { styleClass: "header-row", text: "1.0", hpos: HPos.CENTER, layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
				Label { styleClass: "header-row", text: "1000000", layoutInfo: LayoutInfo { width: 60 } },
			] }, for( segment in segments ) {
				SegmentRow { segment: segment }
			}
		];
	}
}

class SegmentRow extends GridRow {
	var scale:Integer on replace {
		slider.value = scale;
		selectField.value = SCALES[6+scale];
		model.scale = scale;
	}
	
	var model:LineSegmentChartModel on replace {
		scale = model.scale;
	}
	
	public-init var segment:LineSegment on replace {
		model = LineChart.getLineSegmentChartModel( segment );
	}
	
	def slider = Slider {
		min: -6
		max: 6
		value: scale
		minorTickCount: 0
		majorTickUnit: 1
		clickToPosition: true
		layoutInfo: GridLayoutInfo { hspan: 3, hfill: true, hgrow: Priority.ALWAYS }
	}
	
	def sliderValue:Number = bind slider.value on replace {
		scale = sliderValue as Integer;
	}
	
	def selectField:SelectField = SelectField {
		options: SCALES
		value: SCALES[6+scale]
		layoutInfo: LayoutInfo { width: 90 }
		onValueChanged: function( value ):Void {
			if( value != null )
				scale = Sequences.indexOf( SCALES, value ) -6;
		}
	}
	
	init {
		cells = [
			Label {
				graphic: Path {
					elements: [
						MoveTo { x: 6, y: 0 },
						LineTo { x: 2, y: 0 },
						ArcTo { x: 0  y: 2  radiusX: 2  radiusY: 2  sweepFlag: false },
						LineTo { x: 0, y: 15 },
						ArcTo { x: 2  y: 17  radiusX: 2  radiusY: 2  sweepFlag: false },
						LineTo { x: 6, y: 17 },
						ClosePath {}
					]
					fill: model.color
					stroke: null
				}
				text: segment.getStatistic().getName()
				layoutInfo: LayoutInfo { width: 60 }
			}, Label {
				text: if( segment.getStatistic().getSource() == StatisticVariable.MAIN_SOURCE ) "Total" else segment.getStatistic().getSource()
				layoutInfo: LayoutInfo { width: 60 }
			}, Label {
				text: segment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel()
				layoutInfo: LayoutInfo { width: 60 }
			},
			selectField,
			slider
		]
	}
}