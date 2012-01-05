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

import javafx.scene.control.Button;

import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.charting.line.LineChart;

import java.util.ArrayList;
import java.beans.PropertyChangeEvent;

public class SyncButton extends Button {
	public-init var chartView:LineChartView;
	
	override var text = "Sync";
	
	override var action = function():Void {
		def chartViews = new ArrayList();
		chartViews.add( chartView.getChartGroup().getChartView() );
		chartViews.addAll( chartView.getChartGroup().getChartViewsForCharts() );
		chartViews.addAll( chartView.getChartGroup().getChartViewsForSources() );
		def zoomLevel = chartView.getAttribute( LineChart.ZOOM_LEVEL_ATTRIBUTE, "" );
		def follow = Boolean.parseBoolean( chartView.getAttribute( LineChart.FOLLOW_ATTRIBUTE, "false" ) );
		def position = Long.parseLong( chartView.getAttribute( LineChart.POSITION_ATTRIBUTE, "0" ) );
		
		for( cv in chartViews[x|x != chartView] ) {
			def chartView2 = cv as LineChartView;
			chartView2.fireEvent( new PropertyChangeEvent( chartView2, LineChart.ZOOM_LEVEL, null, zoomLevel ) );
			chartView2.fireEvent( new PropertyChangeEvent( chartView2, LineChart.FOLLOW, not follow, follow ) );
			chartView2.fireEvent( new PropertyChangeEvent( chartView2, LineChart.POSITION, null, position ) );
			/*if( not follow ) {
				FX.deferAction( function() {
					chartView2.fireEvent( new PropertyChangeEvent( chartView2, LineChart.POSITION, null, position ) );
				} );
			}*/
		}
	}
}