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
package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fromExpression;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.ui.fx.util.Observables.Group;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class SegmentViewToSeriesFunction implements Function<SegmentView<?>, XYChart.Series<Long, Number>>
{
	private final ObservableValue<Execution> execution;
	//private final ObservableList<Observable> observables;

	private final Group<Observable> observablesUpdatedByUser;

	private final Observable position;
	private final Observable timePulse;
	private final ExecutionChart chart;

	protected static final Logger log = LoggerFactory.getLogger( SegmentViewToSeriesFunction.class );

	public SegmentViewToSeriesFunction( ObservableValue<Execution> execution,
			Group<Observable> observablesUpdatedByUser, Observable timePulse, Observable position, ExecutionChart chart )
	{
		this.execution = execution;
		this.observablesUpdatedByUser = observablesUpdatedByUser;
		this.timePulse = timePulse;
		this.position = position;
		this.chart = chart;
	}

	@Override
	public XYChart.Series<Long, Number> apply( final SegmentView<?> segment )
	{
		if( segment instanceof LineSegmentView )
			return lineSegmentToSeries( ( LineSegmentView )segment );
		if( segment instanceof EventSegmentView )
			return eventSegmentToSeries( ( EventSegmentView )segment );
		throw new RuntimeException( "Unsupported Segment type" );
	}

	private static final Function<DataPoint<?>, XYChart.Data<Long, Number>> datapointToChartdata = new Function<DataPoint<?>, XYChart.Data<Long, Number>>()
	{
		@Override
		public XYChart.Data<Long, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Long, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private Series<Long, Number> lineSegmentToSeries( final LineSegmentView segmentView )
	{
		final LineSegment segment = segmentView.getSegment();
		final XYChart.Series<Long, Number> series = new XYChart.Series<>();
		series.setName( segment.getStatisticName() );

		// applies the scale to each point
		final Function<XYChart.Data<Long, Number>, XYChart.Data<Long, Number>> chartdataToScaledChartdata = new Function<XYChart.Data<Long, Number>, XYChart.Data<Long, Number>>()
		{
			@Override
			public XYChart.Data<Long, Number> apply( XYChart.Data<Long, Number> point )
			{
				double scaleValue = Math.pow( 10,
						Integer.parseInt( segment.getAttribute( LineSegmentView.SCALE_ATTRIBUTE, "0" ) ) );

				XYChart.Data<Long, Number> dataPoint = new XYChart.Data<Long, Number>( point.getXValue(), point.getYValue()
						.doubleValue() * scaleValue );

				dataPoint.setNode( CircleBuilder.create().fill( chart.getColor( segment, execution.getValue() ) )
						.radius( 3 ).build() );

				return dataPoint;
			}
		};

		InvalidationListener seriesUpdater = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable o )
			{
				if( segment.isRemoved() || execution.getValue() == null )
				{
					series.getData().clear();
				}
				else if( chart.scrollbarFollowStateProperty().get() && execution.getValue() == chart.getCurrentExecution()
						&& observablesUpdatedByUser != o )
				{
					DataPoint<Number> latestDataPoint = segment.getStatistic().getLatestPoint(
							chart.getTickZoomLevel().getLevel() );
					if( latestDataPoint != null )
					{
						series.getData().add(
								chartdataToScaledChartdata.apply( datapointToChartdata.apply( latestDataPoint ) ) );
						if( series.getData().get( 0 ).getXValue() < chart.getPosition() )
							series.getData().remove( 0 );
					}
				}
				else
				{
					if( o == timePulse )
					{
						DataPoint<Number> latestDataPoint = segment.getStatistic().getLatestPoint(
								chart.getTickZoomLevel().getLevel() );
						if( !series.getData().isEmpty() && latestDataPoint != null )
						{
							if( series.getData().get( series.getData().size() - 1 ).getXValue() + chart.getSpan() < latestDataPoint
									.getTimestamp() )
							{
								return; // No need to update chart.
							}
						}
					}
					updateLineFromDB( segment, series, chartdataToScaledChartdata );
				}
			}
		};

		final InvalidationListener colorUpdater = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				if( series.getNode() instanceof Path )
				{
					( ( Path )series.getNode() ).setStroke( chart.getColor( segment, execution.getValue() ) );
				}
			}
		};

		segmentView.getProperties().put( "Listeners", ImmutableList.of( colorUpdater, seriesUpdater ) );
		segmentView.getProperties().put( "ListenerTargets",
				ImmutableList.of( observablesUpdatedByUser, timePulse, position ) );

		// TODO: Make path color just update when new segment is added
		observablesUpdatedByUser.addListener( colorUpdater );

		observablesUpdatedByUser.addListener( seriesUpdater );
		position.addListener( seriesUpdater );
		timePulse.addListener( seriesUpdater );
		chart.scrollbarFollowStateProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				updateLineFromDB( segment, series, chartdataToScaledChartdata );
			}
		} );

		return series;
	}

	private XYChart.Series<Long, Number> eventSegmentToSeries( final EventSegmentView segmentView )
	{
		final TestEventSegment segment = segmentView.getSegment();

		final XYChart.Series<Long, Number> series = new XYChart.Series<>();
		series.setName( segment.getTypeLabel() );

		series.setData( fromExpression( new Callable<Iterable<XYChart.Data<Long, Number>>>()
		{
			@Override
			public Iterable<XYChart.Data<Long, Number>> call() throws Exception
			{
				if( segment.isRemoved() || execution.getValue() == null )
				{
					return new LinkedList<>();
				}

				return Iterables.transform(
						segment.getTestEventsInRange( execution.getValue(), ( long )chart.getPosition() - 2000,
								( long )chart.getPosition() + chart.getSpan() + 2000, chart.getTickZoomLevel().getLevel() ),
						new Function<TestEvent, XYChart.Data<Long, Number>>()
						{
							@Override
							public XYChart.Data<Long, Number> apply( TestEvent event )
							{
								XYChart.Data<Long, Number> data = new XYChart.Data<Long, Number>( event.getTimestamp(), 10.0 );

								Line eventLine = LineBuilder.create().endY( 600 ).managed( false )
										.strokeType( StrokeType.OUTSIDE ).build();

								eventLine.setStroke( chart.getColor( segment, execution.getValue() ) );

								data.setNode( eventLine );

								return data;
							}
						} );
			}
		}, observableArrayList( observablesUpdatedByUser, position, timePulse ) ) );

		series.nodeProperty().addListener( new ChangeListener<Node>()
		{
			@Override
			public void changed( ObservableValue<? extends Node> arg0, Node arg1, Node newNode )
			{
				newNode.setVisible( false );
			}
		} );
		return series;
	}

	private void updateLineFromDB( final LineSegment segment, final XYChart.Series<Long, Number> series,
			final Function<XYChart.Data<Long, Number>, XYChart.Data<Long, Number>> chartdataToScaledChartdata )
	{
		log.debug( "Reading from database" );
		Iterable<XYChart.Data<Long, Number>> chartdata = Iterables.transform(
				segment.getStatistic().getPeriod( ( long )chart.getPosition() - 2000,
						( long )chart.getPosition() + chart.getSpan() + 2000, chart.getTickZoomLevel().getLevel(),
						execution.getValue() ), datapointToChartdata );

		series.getData().setAll( Lists.newArrayList( Iterables.transform( chartdata, chartdataToScaledChartdata ) ) );
	}
}
