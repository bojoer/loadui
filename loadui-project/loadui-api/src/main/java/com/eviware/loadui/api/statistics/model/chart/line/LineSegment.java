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
package com.eviware.loadui.api.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;

/**
 * Represents a line in a LineChart. It can be enabled or disabled, has a color,
 * and is backed by a Statistic.
 * 
 * @author dain.nilsson
 */
public interface LineSegment extends Segment
{
	/**
	 * Returns the StatisticHolder of the LineSegment.
	 * 
	 * @return
	 */
	public StatisticHolder getStatisticHolder();

	/**
	 * Gets the Source.
	 * 
	 * @return
	 */
	public String getSource();

	/**
	 * Gets the name of the StatisticVariable.
	 * 
	 * @return
	 */
	public String getVariableName();

	/**
	 * Gets the name of the Statistic.
	 * 
	 * @return
	 */
	public String getStatisticName();

	/**
	 * Gets the backing Statistic.
	 * 
	 * @return
	 */
	public Statistic<?> getStatistic();

	public interface Removable extends LineSegment, Segment.Removable
	{
		// Marker interface.
	}
}