package com.eviware.loadui.impl.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.util.StringUtils;

public class ChartTestEventSegment extends AbstractChartSegment implements TestEventSegment.Removable
{
	public ChartTestEventSegment( ChartLineChartView chart, String typeLabel, String sourceLabel )
	{
		super( chart, StringUtils.serialize( "TEST_EVENT", typeLabel, sourceLabel ) );
	}
}
