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

package com.eviware.loadui.impl.charting.line;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.LineChart;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.charting.LineChartUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.DefaultNumericTickCalculator;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.chart.model.ChartModel;

public class LineChartImpl extends Chart implements LineChart, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( LineChartImpl.class );

	private static final int PADDING = 10000;

	private final LineChartView chartView;

	private final EventSupport eventSupport = new EventSupport();
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

		follow = Boolean.parseBoolean( chartView.getAttribute( FOLLOW_ATTRIBUTE, "true" ) );

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
		long position = getPosition();

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

		for( Segment segment : chartView.getSegments() )
			addedSegment( segment );
	}

	@Override
	public void refresh( boolean shouldPoll )
	{
		if( zoomLevel == ZoomLevel.ALL )
		{
			long maxTime = getMaxTime();
			long position = getPosition();
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
			long pos = mainExecution == null ? 0 : mainExecution.getLength();
			if( comparedExecution != null && comparedExecution.getLength() > pos )
				pos = Math.min( pos + timeSpan / 3, comparedExecution.getLength() );
			setPosition( pos - timeSpan );
		}

		if( shouldPoll )
		{
			for( LineSegmentChartModel lineModel : lines.values() )
				lineModel.poll();
		}
	}

	@Override
	public void update()
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for( ChartModel model : getModels() )
		{
			min = Math.min( min, ( ( AbstractLineSegmentModel )model ).getMinY() );
			max = Math.max( max, ( ( AbstractLineSegmentModel )model ).getMaxY() );
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
		super.update();
	}

	@Override
	public void setMainExecution( Execution execution )
	{
		if( mainExecution != execution )
		{
			mainExecution = execution;
			for( LineSegmentChartModel lineModel : lines.values() )
				lineModel.setExecution( execution );
			updateXRange();
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
			final Execution cExecution = execution;

			try
			{
				LineChartUtils.invokeInSwingAndWait( new Runnable()
				{
					@Override
					public void run()
					{
						if( comparedExecution == null )
						{
							for( LineSegmentChartModel lineModel : lines.values() )
							{
								ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
								comparedModel.clearPoints();
								comparedModel.setExecution( cExecution );
								comparedLines.put( lineModel, comparedModel );
								addModel( comparedModel, comparedModel.getChartStyle() );
							}
						}
						else if( cExecution == null )
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
							{
								comparedModel.clearPoints();
								comparedModel.setExecution( cExecution );
							}
						}
						comparedExecution = cExecution;
					}
				} );
			}
			catch( InterruptedException e )
			{
				log.error( "Error setting compared Execution", e );
			}
			catch( InvocationTargetException e )
			{
				log.error( "Error setting compared Execution", e );
			}
		}
	}

	private void addedSegment( final Segment segment )
	{
		if( !lines.containsKey( segment ) )
		{
			try
			{
				LineChartUtils.invokeInSwingAndWait( new Runnable()
				{
					@Override
					public void run()
					{
						if( segment instanceof LineSegment )
						{
							LineSegment lineSegment = ( LineSegment )segment;
							LineSegmentChartModel lineModel = new LineSegmentChartModel( chartView, lineSegment );
							lineModel.setLevel( zoomLevel == ZoomLevel.ALL ? ZoomLevel.forSpan( getMaxTime() / 1000 )
									.getLevel() : zoomLevel.getLevel() );
							lines.put( lineSegment, lineModel );
							if( mainExecution != null )
								lineModel.setExecution( mainExecution );
							long position = getPosition();
							lineModel.setXRange( position - PADDING, position + timeSpan + PADDING );
							addModel( lineModel, lineModel.getChartStyle() );
							if( comparedExecution != null )
							{
								ComparedLineSegmentChartModel comparedModel = new ComparedLineSegmentChartModel( lineModel );
								comparedModel.setExecution( comparedExecution );
								comparedLines.put( lineModel, comparedModel );
								addModel( comparedModel, comparedModel.getChartStyle() );
							}
							fireEvent( new CollectionEvent( LineChartImpl.this, LINE_SEGMENT_MODELS,
									CollectionEvent.Event.ADDED, lineModel ) );
						}
					}
				} );
			}
			catch( InterruptedException e )
			{
				log.error( "Error adding LineSegment", e );
			}
			catch( InvocationTargetException e )
			{
				log.error( "Error adding LineSegment", e );
			}
		}
	}

	private void removedSegment( Segment segment )
	{
		final LineSegmentChartModel model = lines.remove( segment );
		if( model != null )
		{
			try
			{
				LineChartUtils.invokeInSwingAndWait( new Runnable()
				{
					@Override
					public void run()
					{
						removeModel( model );
						ComparedLineSegmentChartModel comparedModel = comparedLines.remove( model );
						if( comparedModel != null )
							removeModel( comparedModel );
						ReleasableUtils.releaseAll( model, comparedModel );
						fireEvent( new CollectionEvent( LineChartImpl.this, LINE_SEGMENT_MODELS,
								CollectionEvent.Event.REMOVED, model ) );
					}
				} );
			}
			catch( InterruptedException e )
			{
				log.error( "Error removing LineSegment", e );
			}
			catch( InvocationTargetException e )
			{
				log.error( "Error removing LineSegment", e );
			}
		}
	}

	private void updateXRange()
	{
		long position = getPosition();
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
	public final long getPosition()
	{
		return Math.max( 0, Math.min( getMaxTime() - timeSpan, position ) );
	}

	@Override
	public void setPosition( long position )
	{
		position = Math.max( 0, position );
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
	public final void setZoomLevel( ZoomLevel zoomLevel )
	{
		if( this.zoomLevel != zoomLevel )
		{
			this.zoomLevel = zoomLevel;
			chartView.setAttribute( ZOOM_LEVEL_ATTRIBUTE, zoomLevel.name() );
			timeCalculator.setLevel( zoomLevel );
			int level = zoomLevel == ZoomLevel.ALL ? ZoomLevel.forSpan( getMaxTime() / 1000 ).getLevel() : zoomLevel
					.getLevel();

			for( LineSegmentChartModel lineModel : lines.values() )
			{
				lineModel.clearPoints();
				lineModel.setLevel( level );
			}

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
		ReleasableUtils.releaseAll( lines.values(), eventSupport );
		lines.clear();
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void awaitDraw()
	{
		AbstractLineSegmentModel.awaitQueuedReads();
		refresh( false );
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
					LineChartUtils.invokeInSwingLater( new Runnable()
					{
						@Override
						public void run()
						{
							if( CollectionEvent.Event.ADDED == event.getEvent() )
								addedSegment( ( Segment )event.getElement() );
							else
								removedSegment( ( Segment )event.getElement() );
						}
					} );
				}
			}
			else if( e instanceof PropertyChangeEvent )
			{
				final PropertyChangeEvent event = ( PropertyChangeEvent )e;
				if( ZOOM_LEVEL.equals( event.getPropertyName() ) )
				{
					LineChartUtils.invokeInSwingLater( new Runnable()
					{
						@Override
						public void run()
						{
							setZoomLevel( ZoomLevel.valueOf( ( String )event.getNewValue() ) );
						}
					} );
				}
				else if( FOLLOW.equals( event.getPropertyName() ) )
				{
					LineChartUtils.invokeInSwingLater( new Runnable()
					{
						@Override
						public void run()
						{
							setFollow( ( Boolean )event.getNewValue() );
						}
					} );
				}
				else if( POSITION.equals( event.getPropertyName() ) )
				{
					LineChartUtils.invokeInSwingLater( new Runnable()
					{
						@Override
						public void run()
						{
							setPosition( ( ( Number )event.getNewValue() ).longValue() );
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