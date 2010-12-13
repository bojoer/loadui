package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Arrays;
import java.util.Collection;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;
import com.eviware.loadui.util.StringUtils;

public class ChartLineSegment implements LineSegment
{
	private final Chart chart;
	private final DelegatingAttributeHolderSupport attributeSupport;
	private final String id;
	private final String variableName;
	private final String statisticName;
	private final String source;

	private Statistic<?> statistic;

	public ChartLineSegment( Chart chart, String variableName, String statisticName, String source )
	{
		this.chart = chart;
		this.variableName = variableName;
		this.statisticName = statisticName;
		this.source = source;
		attributeSupport = new DelegatingAttributeHolderSupport( chart, "" );
		id = StringUtils.serialize( Arrays.asList( chart.getStatisticHolder().getId(), variableName, statisticName,
				source ) );
	}

	public Chart getChart()
	{
		return chart;
	}

	public String getSource()
	{
		return source;
	}

	public String getVariableName()
	{
		return variableName;
	}

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
			statistic = chart.getStatisticHolder().getStatisticVariable( variableName )
					.getStatistic( statisticName, source );

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
}