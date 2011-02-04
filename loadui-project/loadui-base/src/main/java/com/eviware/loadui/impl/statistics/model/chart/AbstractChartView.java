/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.statistics.model.chart;

import java.util.Collection;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProvider;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;

/**
 * Abstract base class for ChartViews.
 * 
 * @author dain.nilsson
 */
public abstract class AbstractChartView implements ChartView
{
	public final static String CHART_GROUP_PREFIX = "_CHARTGROUP_";
	public final static String CHART_PREFIX = "_CHART_";
	public final static String SOURCE_PREFIX = "_SOURCE_";

	private final DelegatingAttributeHolderSupport attributeSupport;
	private final ChartGroup chartGroup;

	public AbstractChartView( ChartViewProvider<?> provider, AttributeHolder attributeDelegate, String prefix )
	{
		attributeSupport = new DelegatingAttributeHolderSupport( attributeDelegate, prefix );
		chartGroup = provider.getChartGroup();
	}

	@Override
	public ChartGroup getChartGroup()
	{
		return chartGroup;
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
