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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.control.Separator;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import com.eviware.loadui.fx.statistics.chart.PanelFactory;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

public class LineChartPanels {
}

public function getGroupPanels( chartGroup:ChartGroup ):Object[] {
	[
		PanelFactory {
			title: "Add statistic"
			build: function() { AddSegmentPanel { chartViews: for( chartView in chartGroup.getChartViewsForCharts() ) chartView as ConfigurableLineChartView } }
		}, PanelFactory {
			title: "Zoom",
			build: function() { ZoomPanel { chartView: chartGroup.getChartView() as LineChartView } }
		}, PanelFactory {
			title: "Scale"
			build: function() {
				def chartView = chartGroup.getChartView() as LineChartView;
				def lineChart = LineChartHolder.getLineChart( chartView );
				def lineModels = for( segment in chartView.getSegments() ) lineChart.getLineSegmentModel( segment );
				ScalePanel { lineSegmentModels: lineModels }
			}
		}, PanelFactory {
			title: "Style"
			build: function() {
				def chartView = chartGroup.getChartView() as LineChartView;
				def lineChart = LineChartHolder.getLineChart( chartView );
				def lineModels = for( segment in chartView.getSegments() ) lineChart.getLineSegmentModel( segment );
				StylePanel { lineSegmentModels: lineModels }
			}
		}, Separator {
			vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER
		}, PanelFactory {
			title: "Raw data"
			build: function() { RawDataPanel { segments: (chartGroup.getChartView() as LineChartView).getSegments()[s|true] } }
		}, Separator {
			vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER
		}, FollowCheckBox {
			chartView: chartGroup.getChartView() as LineChartView, layoutInfo: LayoutInfo { margin: Insets { top: 3 } }, vpos: VPos.CENTER
		}, SyncButton {
			chartView: chartGroup.getChartView() as LineChartView, layoutInfo: LayoutInfo { margin: Insets { top: 2 } }, vpos: VPos.CENTER
		}
	]
}

public function getChartPanels( chartView:LineChartView ):Object[] {
	[
		if( chartView instanceof ConfigurableLineChartView )
			PanelFactory {
				title: "Add statistic"
				build: function() { AddSegmentPanel { chartViews: chartView as ConfigurableLineChartView } }
			}
		else [],
		PanelFactory {
			title: "Zoom",
			build: function() { ZoomPanel { chartView: chartView } }
		}, PanelFactory {
			title: "Scale"
			build: function() {
				def lineChart = LineChartHolder.getLineChart( chartView );
				def lineModels = for( segment in chartView.getSegments() ) lineChart.getLineSegmentModel( segment );
				ScalePanel { lineSegmentModels: lineModels }
			}
		}, PanelFactory {
			title: "Style"
			build: function() {
				def lineChart = LineChartHolder.getLineChart( chartView );
				def lineModels = for( segment in chartView.getSegments() ) lineChart.getLineSegmentModel( segment );
				StylePanel { lineSegmentModels: lineModels }
			}
		}, Separator {
			vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos: HPos.CENTER
		}, PanelFactory {
			title: "Raw data"
			build: function() { RawDataPanel { segments: chartView.getSegments()[s|true] } }
		}, Separator {
			vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos: HPos.CENTER
		}, FollowCheckBox {
			chartView: chartView, layoutInfo: LayoutInfo { margin: Insets { top: 3 } }, vpos: VPos.CENTER
		}, SyncButton {
			chartView: chartView, layoutInfo: LayoutInfo { margin: Insets { top: 2 } }, vpos: VPos.CENTER
		}
	]
}