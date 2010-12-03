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

import java.util.Collection;
import java.util.Set;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.Chart;

/**
 * ChartView which describes a Line chart.
 * 
 * @author dain.nilsson
 */
public interface LineChartView extends ChartView, EventFirer
{
	/**
	 * CollectionEvent fired when a segment is added or removed.
	 */
	public final static String SEGMENTS = LineChartView.class.getName() + "@segments";

	/**
	 * Gets the contained LineSegments.
	 * 
	 * @return
	 */
	public Collection<LineSegment> getSegments();

	/**
	 * Gets the names of the available StatisticVariables;
	 * 
	 * @return
	 */
	public Set<String> getVariableNames();

	/**
	 * Gets the names of the available Statistics, for a given StatisticVariable.
	 * 
	 * @param variableName
	 * @return
	 */
	public Set<String> getStatisticNames( String variableName );

	/**
	 * Gets the names of the available sources, for a given StatisticVariable.
	 * 
	 * @param variableName
	 * @param statisticName
	 * @return
	 */
	public Set<String> getSources( String variableName );

	/**
	 * Adds a LineSegment for the given Statistic under the given
	 * StatisticVariable, from the given source.
	 * 
	 * @param variableName
	 * @param statisticName
	 * @param source
	 * @return
	 */
	public LineSegment addSegment( String variableName, String statisticName, String source );

	/**
	 * Removes a LineSegment which has previously been added.
	 * 
	 * @param segment
	 */
	public void removeSegment( LineSegment segment );

	/**
	 * Represents a line in a LineChart. It can be enabled or disabled, has a
	 * color, and is backed by a Statistic.
	 * 
	 * @author dain.nilsson
	 */
	public interface LineSegment extends AttributeHolder
	{
		/**
		 * Gets the backing Statistic.
		 * 
		 * @return
		 */
		public Statistic<?> getStatistic();
	}
}
