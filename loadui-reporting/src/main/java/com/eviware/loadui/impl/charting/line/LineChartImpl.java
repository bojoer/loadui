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

package com.eviware.loadui.impl.charting.line;

import java.util.ArrayList;
import java.util.HashMap;

import com.eviware.loadui.api.charting.line.LineChart;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.range.Range;

public class LineChartImpl extends Chart implements LineChart
{
	private final LineChartView chartView;

	private final HashMap<LineSegment, LineSegmentChartModel> lines = new HashMap<LineChartView.LineSegment, LineSegmentChartModel>();
	private final HashMap<LineSegmentChartModel, ComparedLineSegmentChartModel> comparedLines = new HashMap<LineSegmentChartModel, ComparedLineSegmentChartModel>();
	private final TotalTimeTickCalculator timeCalculator = new TotalTimeTickCalculator();
	private final ChartViewListener chartViewListener = new ChartViewListener();
	private final LongRange xRange;

	private Execution mainExecution;
	private Execution comparedExecution;
	private long position = 0;
	private long timeSpan = 10000;
	private ZoomLevel zoomLevel = ZoomLevel.SECONDS;

	public LineChartImpl( LineChartView chartView )
	{
		this.chartView = chartView;

		try
		{
			position = Long.parseLong( chartView.getAttribute( POSITION_ATTRIBUTE, "0" ) );
		}
		catch( NumberFormatException e )
		{
			position = 0;
		}

		try
		{
			timeSpan = Long.parseLong( chartView.getAttribute( TIME_SPAN_ATTRIBUTE, "10000" ) );
		}
		catch( NumberFormatException e )
		{
			timeSpan = 10000;
		}

		xRange = new LongRange( 0, timeSpan );
		NumericAxis xAxis = new NumericAxis( xRange );
		xAxis.setTickCalculator( timeCalculator );
		setXAxis( xAxis );

		chartView.addEventListener( CollectionEvent.class, chartViewListener );

		for( LineSegment segment : chartView.getSegments() )
			addedSegment( segment );
	}

	@Override
	public void refresh( boolean shouldPoll )
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for( LineSegmentChartModel lineModel : lines.values() )
		{
			if( shouldPoll )
				lineModel.poll();

			Range<Double> yRange = lineModel.getYRange( 0.05, 0.05 );
			if( yRange.minimum() != Double.NEGATIVE_INFINITY && yRange.maximum() != Double.POSITIVE_INFINITY )
			{
				min = Math.min( min, yRange.minimum() );
				max = Math.max( max, yRange.maximum() );
			}
		}

		if( comparedExecution != null )
		{
			for( ComparedLineSegmentChartModel lineModel : comparedLines.values() )
			{
				Range<Double> yRange = lineModel.getYRange( 0.05, 0.05 );
				if( yRange.minimum() != Double.NEGATIVE_INFINITY && yRange.maximum() != Double.POSITIVE_INFINITY )
				{
					min = Math.min( min, yRange.minimum() );
					max = Math.max( max, yRange.maximum() );
				}
			}
		}

		if( min > max )
		{
			min = 0;
			max = 100;
		}
		else if( min == max )
		{
			min -= 2.5;
			max += 2.5;
		}
		getYAxis().setRange( min, max );
	}

	@Override
	public void setMainExecution( Execution execution )
	{
		if( execution == null )
			throw new NullPointerException( "Main Execution cannot be null!" );

		if( mainExecution != execution )
		{
			mainExecution = execution;
			for( LineSegmentChartModel lineModel : lines.values() )
				lineModel.setXRange( 0, 0 );
			refresh( false );
		}
	}

	@Override
	public void setComparedExecution( Execution execution )
	{
		if( comparedExecution != execution )
		{
			if( comparedExecution == null )
			{
				for( LineSegmentChartModel lineModel : lines.values() )
				{
					ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
					comparedLines.put( lineModel, comparedModel );
					addModel( comparedModel, comparedModel.getChartStyle() );
				}
			}
			else if( execution == null )
			{
				for( LineSegmentModel lineModel : new ArrayList<LineSegmentChartModel>( comparedLines.keySet() ) )
					removeModel( comparedLines.remove( lineModel ) );
			}
			else
			{
				for( ComparedLineSegmentChartModel comparedModel : comparedLines.values() )
					comparedModel.setExecution( comparedExecution );
			}
			comparedExecution = execution;
		}
	}

	private void addedSegment( LineSegment segment )
	{
		if( !lines.containsKey( segment ) )
		{
			LineSegmentChartModel lineModel = new LineSegmentChartModel( chartView, segment );
			lines.put( segment, lineModel );
			addModel( lineModel, lineModel.getChartStyle() );
			if( comparedExecution != null )
			{
				ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
				comparedLines.put( lineModel, comparedModel );
				addModel( comparedModel, comparedModel.getChartStyle() );
			}
		}
	}

	private void removedSegment( LineSegment segment )
	{
		LineSegmentChartModel model = lines.remove( segment );
		if( model != null )
		{
			removeModel( model );
			ComparedLineSegmentChartModel comparedModel = comparedLines.remove( model );
			if( comparedModel != null )
			{
				removeModel( comparedModel );
			}
		}
	}

	@Override
	public long getMaxTime()
	{
		return comparedExecution == null ? mainExecution.getLength() : Math.max( mainExecution.getLength(),
				comparedExecution.getLength() );
	}

	@Override
	public long getTimeSpan()
	{
		return timeSpan;
	}

	@Override
	public void setTimeSpan( long timeSpan )
	{
		if( this.timeSpan != timeSpan )
		{
			chartView.setAttribute( TIME_SPAN_ATTRIBUTE, String.valueOf( timeSpan ) );
			refresh( false );
		}
	}

	@Override
	public long getPosition()
	{
		return position;
	}

	@Override
	public void setPosition( long position )
	{
		if( this.position != position )
		{
			this.position = position;
			chartView.setAttribute( POSITION_ATTRIBUTE, String.valueOf( position ) );
			xRange.setMin( position );
			xRange.setMax( position + timeSpan );

			int padding = 10000;
			long xMin = position - padding;
			long xMax = position + timeSpan + padding;
			for( LineSegmentChartModel lineModel : lines.values() )
				lineModel.setXRange( xMin, xMax );

			refresh( false );
		}
	}

	@Override
	public ZoomLevel getZoomLevel()
	{
		return zoomLevel;
	}

	@Override
	public void setZoomLevel( ZoomLevel zoomLevel )
	{
		if( this.zoomLevel != zoomLevel )
		{
			this.zoomLevel = zoomLevel;
			timeCalculator.setLevel( zoomLevel );

			refresh( false );
		}
	}

	private class ChartViewListener implements WeakEventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
				addedSegment( ( LineSegment )event.getElement() );
			else
				removedSegment( ( LineSegment )event.getElement() );
		}
	}
}