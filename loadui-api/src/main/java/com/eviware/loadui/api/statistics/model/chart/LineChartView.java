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

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.Statistic;

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
		 * Gets the backing Statistic.
		 * 
		 * @return
		 */
		public Statistic<?> getStatistic();
	}
}