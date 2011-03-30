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
package com.eviware.loadui.api.charting.line;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;

/**
 * A LineChart displaying data from an underlying LineChartView. A LineChart
 * should also be an instance of JComponent.
 * 
 * @author dain.nilsson
 */
public interface LineChart
{
	public static final String POSITION_ATTRIBUTE = "position";
	public static final String TIME_SPAN_ATTRIBUTE = "timeSpan";
	public static final String ZOOM_LEVEL_ATTRIBUTE = "zoomLevel";

	public static final String ZOOM_LEVEL = "zoomLevel";

	/**
	 * Refreshes the LineChart. If shouldPoll is set to true, then the LineChart
	 * will poll for new points in the current range (used when viewing an
	 * Execution in progress).
	 * 
	 * @param shouldPoll
	 */
	public void refresh( boolean shouldPoll );

	/**
	 * Sets the main Execution to view.
	 * 
	 * @param execution
	 */
	public void setMainExecution( Execution execution );

	/**
	 * Sets an Execution to compare to. Call this with null to unset the compared
	 * Execution.
	 * 
	 * @param execution
	 */
	public void setComparedExecution( Execution execution );

	/**
	 * Gets the greatest length of the viewed Executions (main and compared),
	 * measured in milliseconds.
	 * 
	 * @return
	 */
	public long getMaxTime();

	/**
	 * Gets the show time span, in milliseconds.
	 * 
	 * @return
	 */
	public long getTimeSpan();

	/**
	 * Sets the shown time span, in milliseconds.
	 * 
	 * @param timeSpan
	 */
	public void setTimeSpan( long timeSpan );

	/**
	 * Gets the leftmost shown position of the chart.
	 * 
	 * @return
	 */
	public long getPosition();

	/**
	 * Sets the leftmost shown position of the chart.
	 * 
	 * @param position
	 */
	public void setPosition( long position );

	/**
	 * Gets the ZoomLevel of the chart.
	 * 
	 * @return
	 */
	public ZoomLevel getZoomLevel();

	/**
	 * Sets the ZoomLevel of the chart.
	 * 
	 * @param zoomLevel
	 */
	public void setZoomLevel( ZoomLevel zoomLevel );

	/**
	 * Gets the LineSegmentModel for a LineSegment.
	 * 
	 * @param segment
	 * @return
	 */
	public LineSegmentModel getLineSegmentModel( LineSegment segment );

	/**
	 * Factory for creating LineCharts.
	 * 
	 * @author dain.nilsson
	 */
	public interface Factory
	{
		public LineChart createLineChart( LineChartView lineChartView );
	}
}