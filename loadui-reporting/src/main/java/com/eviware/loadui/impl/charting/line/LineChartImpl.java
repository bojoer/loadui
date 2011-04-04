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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.LineChart;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.ReleasableUtils;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.DefaultNumericTickCalculator;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.range.Range;

public class LineChartImpl extends Chart implements LineChart, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( LineChartImpl.class );

	private static final int PADDING = 10000;

	private final LineChartView chartView;

	private final HashMap<LineSegment, LineSegmentChartModel> lines = new HashMap<LineSegment, LineSegmentChartModel>();
	private final HashMap<LineSegmentChartModel, ComparedLineSegmentChartModel> comparedLines = new HashMap<LineSegmentChartModel, ComparedLineSegmentChartModel>();
	private final TotalTimeTickCalculator timeCalculator = new TotalTimeTickCalculator();
	private final ChartViewListener chartViewListener = new ChartViewListener();
	private final LongRange xRange;

	private Execution mainExecution;
	private Execution comparedExecution;
	private long position = 0;
	private long timeSpan = 10000;
	private ZoomLevel zoomLevel = null;
	private boolean follow = true;

	public LineChartImpl( LineChartView chartView )
	{
		this.chartView = chartView;

		follow = Boolean.valueOf( chartView.getAttribute( FOLLOW_ATTRIBUTE, "true" ) );

		try
		{
			timeSpan = Long.parseLong( chartView.getAttribute( TIME_SPAN_ATTRIBUTE, "10000" ) );
		}
		catch( NumberFormatException e )
		{
			timeSpan = 10000;
		}

		if( !follow )
		{
			try
			{
				position = Long.parseLong( chartView.getAttribute( POSITION_ATTRIBUTE, "0" ) );
			}
			catch( NumberFormatException e )
			{
				position = 0;
			}
		}

		xRange = new LongRange( position, position + timeSpan );
		NumericAxis xAxis = new NumericAxis( xRange );
		xAxis.setTickCalculator( timeCalculator );
		setXAxis( xAxis );

		Axis yAxis = getYAxis();
		DefaultNumericTickCalculator yTickCalculator = new DefaultNumericTickCalculator();
		yTickCalculator.setMinorTickIntervalBetweenMajors( 0 );
		yAxis.setTickCalculator( yTickCalculator );
		yAxis.setRange( 0, 10 );
		yAxis.setLabelVisible( false );

		LineChartStyles.styleChart( this );

		ZoomLevel level;
		try
		{
			level = ZoomLevel.valueOf( chartView.getAttribute( ZOOM_LEVEL_ATTRIBUTE, "SECONDS" ) );
		}
		catch( IllegalArgumentException e )
		{
			level = ZoomLevel.SECONDS;
		}
		setZoomLevel( level );

		chartView.addEventListener( EventObject.class, chartViewListener );

		for( LineSegment segment : chartView.getSegments() )
			addedSegment( segment );
	}

	@Override
	public void refresh( boolean shouldPoll )
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		if( zoomLevel == ZoomLevel.ALL )
		{
			long maxTime = getMaxTime();
			if( timeSpan != maxTime || position != 0 )
			{
				position = 0;
				timeSpan = getMaxTime();
				xRange.setMin( 0 );
				xRange.setMax( timeSpan );
				int level = ZoomLevel.forSpan( timeSpan / 1000 ).getLevel();

				for( LineSegmentChartModel lineModel : lines.values() )
				{
					lineModel.setLevel( level );
					lineModel.setXRange( -PADDING, timeSpan + PADDING );
				}
			}
		}
		else if( follow )
		{
			setPosition( getMaxTime() - timeSpan );
		}

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
				lineModel.setExecution( execution );
			refresh( false );
		}
	}

	@Override
	public void setComparedExecution( Execution execution )
	{
		if( mainExecution == execution )
			execution = null;

		if( comparedExecution != execution )
		{
			if( comparedExecution == null )
			{
				for( LineSegmentChartModel lineModel : lines.values() )
				{
					ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
					comparedModel.setExecution( execution );
					comparedLines.put( lineModel, comparedModel );
					addModel( comparedModel, comparedModel.getChartStyle() );
				}
			}
			else if( execution == null )
			{
				for( LineSegmentModel lineModel : new ArrayList<LineSegmentChartModel>( comparedLines.keySet() ) )
				{
					ComparedLineSegmentChartModel comparedModel = comparedLines.remove( lineModel );
					removeModel( comparedModel );
					ReleasableUtils.release( comparedModel );
				}
			}
			else
			{
				for( ComparedLineSegmentChartModel comparedModel : comparedLines.values() )
					comparedModel.setExecution( execution );
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
			if( mainExecution != null )
				lineModel.setExecution( mainExecution );
			lineModel.setXRange( position - PADDING, position + timeSpan + PADDING );
			addModel( lineModel, lineModel.getChartStyle() );
			if( comparedExecution != null )
			{
				ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
				comparedModel.setExecution( comparedExecution );
				comparedLines.put( lineModel, comparedModel );
				addModel( comparedModel, comparedModel.getChartStyle() );
			}
			chartView.fireEvent( new CollectionEvent( chartView, LINE_SEGMENT_MODELS, CollectionEvent.Event.ADDED,
					lineModel ) );
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
				removeModel( comparedModel );
			ReleasableUtils.releaseAll( model, comparedModel );
			chartView
					.fireEvent( new CollectionEvent( chartView, LINE_SEGMENT_MODELS, CollectionEvent.Event.REMOVED, model ) );
		}
	}

	private void updateXRange()
	{
		xRange.setMin( position );
		xRange.setMax( position + timeSpan );
		long xMin = position - PADDING;
		long xMax = position + timeSpan + PADDING;
		for( LineSegmentChartModel lineModel : lines.values() )
			lineModel.setXRange( xMin, xMax );
	}

	@Override
	public long getMaxTime()
	{
		long mainLength = mainExecution == null ? 0 : mainExecution.getLength();
		long comparedLength = comparedExecution == null ? 0 : comparedExecution.getLength();
		return Math.max( mainLength, comparedLength );
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
			setTimeSpanNoSave( timeSpan );
		}
	}

	public void setTimeSpanNoSave( long timeSpan )
	{
		this.timeSpan = timeSpan;
		updateXRange();

		refresh( false );
	}

	@Override
	public long getPosition()
	{
		return position;
	}

	@Override
	public void setPosition( long position )
	{
		position = Math.max( 0, Math.min( getMaxTime() - timeSpan, position ) );
		if( this.position != position )
		{
			this.position = position;
			chartView.setAttribute( POSITION_ATTRIBUTE, String.valueOf( position ) );

			updateXRange();
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
			chartView.setAttribute( ZOOM_LEVEL_ATTRIBUTE, zoomLevel.name() );
			timeCalculator.setLevel( zoomLevel );
			int level = zoomLevel == ZoomLevel.ALL ? ZoomLevel.forSpan( getMaxTime() / 1000 ).getLevel() : zoomLevel
					.getLevel();

			for( LineSegmentChartModel lineModel : lines.values() )
				lineModel.setLevel( level );

			refresh( false );
		}
	}

	@Override
	public boolean isFollow()
	{
		return follow;
	}

	@Override
	public void setFollow( boolean follow )
	{
		if( this.follow != follow )
		{
			this.follow = follow;
			chartView.setAttribute( FOLLOW_ATTRIBUTE, Boolean.toString( follow ) );
			if( follow )
				refresh( false );
		}
	}

	@Override
	public LineSegmentModel getLineSegmentModel( LineSegment segment )
	{
		return lines.get( segment );
	}

	@Override
	public void release()
	{
		chartView.removeEventListener( EventObject.class, chartViewListener );
		ReleasableUtils.releaseAll( lines.values() );
		lines.clear();
	}

	private class ChartViewListener implements WeakEventHandler<EventObject>
	{
		@Override
		public void handleEvent( EventObject e )
		{
			if( e instanceof CollectionEvent )
			{
				final CollectionEvent event = ( CollectionEvent )e;
				if( LineChartView.SEGMENTS.equals( event.getKey() ) )
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
								addedSegment( ( LineSegment )event.getElement() );
							else
								removedSegment( ( LineSegment )event.getElement() );
						}
					} );
				}
			}
			else if( e instanceof PropertyChangeEvent )
			{
				final PropertyChangeEvent event = ( PropertyChangeEvent )e;
				if( ZOOM_LEVEL.equals( event.getPropertyName() ) )
				{
					SwingUtilities.invokeLater( new Runnable()
					{
						@Override
						public void run()
						{
							setZoomLevel( ZoomLevel.valueOf( ( String )event.getNewValue() ) );
						}
					} );
				}
			}
		}
	}

	public static class Factory implements LineChart.Factory
	{
		@Override
		public LineChart createLineChart( LineChartView lineChartView )
		{
			return new LineChartImpl( lineChartView );
		}
	}
}