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
package com.eviware.loadui.impl.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.impl.statistics.model.chart.AbstractChartViewProvider;

/**
 * ChartViewProvider for LineChartViews.
 * 
 * @author dain.nilsson
 */
public class LineChartViewProvider extends AbstractChartViewProvider<LineChartView>
{
	public LineChartViewProvider( ChartGroup chartGroup )
	{
		super( chartGroup );
	}

	@Override
	protected LineChartView buildChartViewForGroup( ChartGroup chartGroup )
	{
		return new ChartGroupLineChartView( chartGroup );
	}

	@Override
	protected LineChartView buildChartViewForChart( Chart chart )
	{
		return new ChartLineChartView( chart );
	}

	@Override
	protected LineChartView buildChartViewForSource( String source )
	{
		return new SourceLineChartView( chartGroup, source );
	}
}