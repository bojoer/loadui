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
package com.eviware.loadui.api.statistics.model.chart;

import java.util.Collection;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;

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
	 * Represents a line in a LineChart. It can be enabled or disabled, has a
	 * color, and is backed by a Statistic.
	 * 
	 * @author dain.nilsson
	 */
	public interface LineSegment extends AttributeHolder
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

		public interface Removable extends LineSegment
		{
			public void remove();
		}
	}
}