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
package com.eviware.loadui.api.statistics.model;

import java.util.Collection;
import java.util.Set;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.model.OrderedCollection;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Releasable;

/**
 * Holds a number of Charts. Allows creation and reordering of the contained
 * Charts.
 * 
 * @author dain.nilsson
 */
public interface ChartGroup extends AttributeHolder, OrderedCollection<Chart>, Releasable, Labeled.Mutable
{
	// BaseEvent key fired when the type of the ChartGroup changes.
	public static final String TYPE = ChartGroup.class.getName() + "@type";

	// BaseEvent key fired when the template script of the ChartGroup changes.
	public static final String TEMPLATE_SCRIPT = ChartGroup.class.getName() + "@templateScript";

	/**
	 * Gets the type of the ChartGroup.
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Sets the type of the ChartGroup.
	 * 
	 * @param type
	 */
	public void setType( String type );

	/**
	 * Gets the ChartView for the ChartGroup. This will change any time setType
	 * is called with a new type.
	 * 
	 * @return
	 */
	public ChartView getChartView();

	/**
	 * Gets the ChartView for a Chart.
	 * 
	 * @param chart
	 * @return
	 */
	public ChartView getChartViewForChart( Chart chart );

	/**
	 * Gets the ChartView for a source.
	 * 
	 * @param source
	 * @return
	 */
	public ChartView getChartViewForSource( String source );

	/**
	 * Gets the ChartViews for the contained Charts.
	 * 
	 * @return
	 */
	public Collection<ChartView> getChartViewsForCharts();

	/**
	 * Gets the ChartViews for the available sources.
	 * 
	 * @return
	 */
	public Collection<ChartView> getChartViewsForSources();

	/**
	 * Gets the template script of the ChartGroup.
	 * 
	 * @return
	 */
	public String getTemplateScript();

	/**
	 * Sets the template script of the ChartGroup.
	 */
	public void setTemplateScript( String templateScript );

	/**
	 * Creates and returns a new Chart for the given StatisticHolder, placing it
	 * at the end of the existing Charts.
	 * 
	 * @param statisticHolder
	 * @return
	 */
	public Chart createChart( StatisticHolder statisticHolder );

	/**
	 * Moved a contained Chart to the given index.
	 * 
	 * @param chart
	 * @param index
	 */
	public void moveChart( Chart chart, int index );

	/**
	 * Deletes the ChartGroup.
	 */
	public void delete();

	/**
	 * Gets a Set of all available sources for the contained Charts.
	 * 
	 * @return
	 */
	public Set<String> getSources();
}
