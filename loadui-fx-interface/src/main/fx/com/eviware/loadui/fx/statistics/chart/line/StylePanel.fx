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
import javafx.scene.layout.LayoutInfo;

import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridLayoutInfo;

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
	
	init {
		rows = [
			row([
				Label { text: "Color" },
				Label { text: "Statistic", layoutInfo: GridLayoutInfo { hspan: 3 } },
				Label { text: "Width" },
				Label { text: "Stroke" }
			]), for( segment in (chartGroup.getChartView() as LineChartView).getSegments() ) row([
				Label { text: "Color" },
				Label {
					text: segment.getStatistic().getName()
					layoutInfo: LayoutInfo { width: 60 }
				}, Label {
					text: if( segment.getStatistic().getSource() == StatisticVariable.MAIN_SOURCE ) "All" else segment.getStatistic().getSource()
					layoutInfo: LayoutInfo { width: 60 }
				}, Label {
					text: segment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel()
					layoutInfo: LayoutInfo { width: 60 }
				}
				Label { text: "Width" },
				Label { text: "Stroke" }
			])
		];
	}
}