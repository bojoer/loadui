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

import java.util.Arrays;
import java.util.Collection;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;
import com.eviware.loadui.util.StringUtils;

public class LineSegmentImpl implements LineChartView.LineSegment
{
	private final Statistic<?> statistic;
	private final DelegatingAttributeHolderSupport attributeSupport;
	private final String variableName;
	private final String statisticName;
	private final String source;

	/**
	 * Generates a String which is unique for the combination of variableName,
	 * statisticName and source.
	 * 
	 * @param variableName
	 * @param statisticName
	 * @param source
	 * @return
	 */
	static String createSegmentString( String variableName, String statisticName, String source )
	{
		return StringUtils.serialize( Arrays.asList( variableName, statisticName, source ) );
	}

	public LineSegmentImpl( Chart chart, String variableName, String statisticName, String source )
	{
		attributeSupport = new DelegatingAttributeHolderSupport( chart, variableName + "_" + statisticName + "_" + source );
		statistic = chart.getStatisticHolder().getStatisticVariable( variableName ).getStatistic( statisticName, source );
		this.variableName = variableName;
		this.statisticName = statisticName;
		this.source = source;
	}

	public String getVariableName()
	{
		return variableName;
	}

	public String getSource()
	{
		return source;
	}

	public String getStatisticName()
	{
		return statisticName;
	}

	public String getSegmentString()
	{
		return createSegmentString( variableName, statisticName, source );
	}

	@Override
	public Statistic<?> getStatistic()
	{
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