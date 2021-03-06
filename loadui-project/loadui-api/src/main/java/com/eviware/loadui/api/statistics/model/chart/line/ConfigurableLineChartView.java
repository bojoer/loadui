/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.statistics.model.chart.line;

import java.util.Set;

/**
 * Configurable version of LineChartView, which allows adding and removing
 * LineSegments.
 * 
 * @author dain.nilsson
 */
public interface ConfigurableLineChartView extends LineChartView
{
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
	public LineSegment.Removable addSegment( String variableName, String statisticName, String source );

	/**
	 * Adds a TestEventSegment for the given TestEventSourceDescriptor.
	 * 
	 * @param typeLabel
	 * @param sourceLabel
	 *           TODO
	 * @return
	 */
	public TestEventSegment.Removable addSegment( String typeLabel, String sourceLabel );
}
