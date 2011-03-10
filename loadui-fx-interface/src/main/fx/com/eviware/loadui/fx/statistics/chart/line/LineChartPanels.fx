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

import com.eviware.loadui.fx.statistics.chart.PanelFactory;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

public class LineChartPanels {
}

public function getGroupPanels( chartGroup:ChartGroup ):PanelFactory[] {
	[
		PanelFactory {
			title: "Add statistic"
			build: function() { AddSegmentPanel { chartViews: for( chartView in chartGroup.getChartViewsForCharts() ) chartView as ConfigurableLineChartView } }
		}, PanelFactory {
			title: "Zoom",
			build: function() { ZoomPanel { chartView: chartGroup.getChartView() as LineChartView } }
		}, PanelFactory {
			title: "Scale"
			build: function() { ScalePanel { segments: (chartGroup.getChartView() as LineChartView).getSegments()[s|true] } }
		}, PanelFactory {
			title: "Style"
			separator: true
			build: function() { StylePanel { segments: (chartGroup.getChartView() as LineChartView).getSegments()[s|true] } }
		}, PanelFactory {
			title: "Row Data"
			build: function() { RowDataPanel { segments: (chartGroup.getChartView() as LineChartView).getSegments()[s|true] } }
		}
	]
}

public function getChartPanels( chartView:LineChartView ):PanelFactory[] {
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
			build: function() { ScalePanel { segments: chartView.getSegments()[s|true] } }
		}, PanelFactory {
			title: "Style"
			separator: true
			build: function() { StylePanel { segments: chartView.getSegments()[s|true] } }
		}, PanelFactory {
			title: "Row Data"
			build: function() { RowDataPanel { segments: chartView.getSegments()[s|true] } }
		}
	]
}