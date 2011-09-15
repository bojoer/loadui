package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Arrays;
import java.util.Collection;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;
import com.eviware.loadui.util.StringUtils;

public class ChartLineSegment implements LineSegment.Removable
{
	private final ChartLineChartView chartView;
	private final DelegatingAttributeHolderSupport attributeSupport;
	private final String id;
	private final String variableName;
	private final String statisticName;
	private final String source;

	private Statistic<?> statistic;

	public ChartLineSegment( ChartLineChartView chart, String variableName, String statisticName, String source )
	{
		this.chartView = chart;
		this.variableName = variableName;
		this.statisticName = statisticName;
		this.source = source;
		id = StringUtils.serialize( Arrays.asList( chart.getChart().getStatisticHolder().getId(), variableName,
				statisticName, source ) );
		attributeSupport = new DelegatingAttributeHolderSupport( chart, "_SEGMENT_" + id + "_" );
	}

	public Chart getChart()
	{
		return chartView.getChart();
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return chartView.getChart().getStatisticHolder();
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getVariableName()
	{
		return variableName;
	}

	@Override
	public String getStatisticName()
	{
		return statisticName;
	}

	@Override
	public String toString()
	{
		return id;
	}

	@Override
	public Statistic<?> getStatistic()
	{
		if( statistic == null )
		{
			final StatisticVariable statisticVariable = chartView.getChart().getStatisticHolder()
					.getStatisticVariable( variableName );
			if( statisticVariable != null )
			{
				statistic = statisticVariable.getStatistic( statisticName, source );
			}
		}

		return statistic;
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributeSupport.setAttribute( key, value );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributeSupport.getAttribute( key, defaultValue );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributeSupport.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributeSupport.getAttributes();
	}

	@Override
	public void remove()
	{
		chartView.removeSegment( this );
	}
}