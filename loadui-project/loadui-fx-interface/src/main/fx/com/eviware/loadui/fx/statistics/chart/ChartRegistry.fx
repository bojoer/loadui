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

import com.eviware.loadui.fx.statistics.chart.line.LineChartHolder;
import com.eviware.loadui.fx.statistics.chart.line.LineChartPanels;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.*;
import com.eviware.loadui.api.statistics.model.chart.line.*;

/**
 * Creates a Node for a specific ChartView
 */
public function createChart( chartView:ChartView, holder:ChartViewHolder ):BaseChart {
	if( chartView instanceof LineChartView ) {
		LineChartHolder { chartView: chartView as LineChartView, holder: holder }
	} else {
		null
	}
}

public function getGroupPanels( chartGroup:ChartGroup ):Object[] {
	if( chartGroup.getType().equals( LineChartView.class.getName() ) or chartGroup.getType().equals( "com.eviware.loadui.api.statistics.model.chart.LineChartView" ) ) {
		LineChartPanels.getGroupPanels( chartGroup )
	} else {
		[]
	}
}

public function getChartPanels( chartView:ChartView ):Object[] {
	if( chartView instanceof LineChartView ) {
		LineChartPanels.getChartPanels( chartView as LineChartView )
	} else {
		[]
	}
}