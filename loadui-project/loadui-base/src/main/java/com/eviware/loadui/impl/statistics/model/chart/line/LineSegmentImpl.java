package com.eviware.loadui.impl.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;

public class LineSegmentImpl implements LineChartView.LineSegment
{
	private final Statistic<?> statistic;
	private boolean enabled = true;
	private String color = "";

	public LineSegmentImpl( Statistic<?> statistic )
	{
		this.statistic = statistic;
	}

	@Override
	public Statistic<?> getStatistic()
	{
		return statistic;
	}

	@Override
	public String getColor()
	{
		return color;
	}

	@Override
	public void setColor( String color )
	{
		this.color = color;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}
}
