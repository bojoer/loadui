package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Collection;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;

public class SourceLineSegment implements LineSegment
{
	private final ChartLineSegment parent;
	private final String source;
	private final DelegatingAttributeHolderSupport attributeSupport;

	private Statistic<?> statistic;

	public SourceLineSegment( ChartLineSegment parent, String source )
	{
		this.parent = parent;
		this.source = source;
		attributeSupport = new DelegatingAttributeHolderSupport( parent, source );
	}

	@Override
	public Statistic<?> getStatistic()
	{
		if( statistic == null )
			statistic = parent.getChart().getStatisticHolder().getStatisticVariable( parent.getVariableName() )
					.getStatistic( parent.getStatisticName(), source );

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
	public String toString()
	{
		return parent.toString();
	}
}