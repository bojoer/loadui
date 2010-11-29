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
package com.eviware.loadui.api.statistics.model.chart;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * Provides ChartViews of a specific type to Chart objects.
 * 
 * @author dain.nilsson
 * 
 * @param <ChartViewType>
 */
public interface ChartViewAdapter<ChartViewType extends ChartView>
{
	/**
	 * Gets a ChartView for a ChartGroup.
	 * 
	 * @param chartGroup
	 * @return
	 */
	public ChartViewType getChartView( ChartGroup chartGroup );

	/**
	 * Gets a ChartView for a Chart.
	 * 
	 * @param chart
	 * @return
	 */
	public ChartViewType getChartView( Chart chart );

	/**
	 * Gets a ChartView for a specific source in a ChartGroup.
	 * 
	 * @param chartGroup
	 * @param source
	 * @return
	 */
	public ChartViewType getChartView( ChartGroup chartGroup, String source );
}
