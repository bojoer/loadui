package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fromExpression;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.collect.Iterables.transform;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.beans.binding.Bindings.createLongBinding;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.NodeBuilder;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.ui.fx.control.Dialog;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineSegmentView;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class LineChartViewNode extends VBox
{
	protected static final Logger log = LoggerFactory.getLogger( LineChartViewNode.class );

	private static final Function<DataPoint<?>, XYChart.Data<Number, Number>> DATAPOINT_TO_CHARTDATA = new Function<DataPoint<?>, XYChart.Data<Number, Number>>()
	{
		@Override
		public XYChart.Data<Number, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Number, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private static final Function<TestEvent, XYChart.Data<Number, Number>> TESTEVENT_TO_CHARTDATA = new Function<TestEvent, XYChart.Data<Number, Number>>()
	{
		@Override
		public XYChart.Data<Number, Number> apply( TestEvent event )
		{
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>( event.getTimestamp(), 10.0 );
			data.setNode( LineBuilder.create().endY( 600 ).managed( false ).build() );
			return data;
		}
	};

	private final Function<LineSegment, XYChart.Series<Number, Number>> lineSegmentToSeries = new LineSegmentToSeriesFunction();
	private final Function<TestEventSegment, Series<Number, Number>> eventSegmentToSeries = new TestEventSegmentToSeriesFunction();
	private final Function<LineSegment, LineSegmentView> segmentToView = new SegmentToViewFunction();

	private final ObservableValue<Execution> executionProperty;
	private final Observable poll;
	private final LineChartView chartView;

	private final LongProperty position = new SimpleLongProperty( 0 );
	private final LongProperty length = new SimpleLongProperty( 0 );
	private final LongProperty shownSpan = new SimpleLongProperty( 60000 );
	private final ObservableList<LineSegment> lineSegments;
	private final ObservableList<TestEventSegment> eventSegments;
	private final ObservableList<XYChart.Series<Number, Number>> lineSeriesList;
	private final ObservableList<XYChart.Series<Number, Number>> eventSeriesList;
	private final ObservableList<XYChart.Series<Number, Number>> seriesList;
	private final ObservableList<LineSegmentView> lineSegmentViews;

	@FXML
	private VBox segments;

	@FXML
	private LineChart<Number, Number> lineChart;

	@FXML
	private NumberAxis xAxis;

	@FXML
	private ScrollBar scrollBar;

	@FXML
	private HBox buttonBar;

	public LineChartViewNode( final ObservableValue<Execution> executionProperty, LineChartView chartView,
			Observable poll )
	{
		this.executionProperty = executionProperty;
		this.chartView = chartView;
		this.poll = poll;

		length.bind( createLongBinding( new Callable<Long>()
		{
			@Override
			public Long call() throws Exception
			{
				return executionProperty.getValue().getLength();
			}
		}, executionProperty, poll ) );

		lineSegments = fx( ofCollection( chartView, LineChartView.SEGMENTS, LineSegment.class,
				Iterables.filter( chartView.getSegments(), LineSegment.class ) ) );
		lineSeriesList = transform( lineSegments, lineSegmentToSeries );
		lineSegmentViews = transform( lineSegments, segmentToView );

		eventSegments = fx( ofCollection( chartView, LineChartView.SEGMENTS, TestEventSegment.class,
				Iterables.filter( chartView.getSegments(), TestEventSegment.class ) ) );
		eventSeriesList = transform( eventSegments, eventSegmentToSeries );

		seriesList = ObservableLists.concat( lineSeriesList, eventSeriesList );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		scrollBar.visibleAmountProperty().bind( shownSpan );
		scrollBar.blockIncrementProperty().bind( shownSpan );
		scrollBar.maxProperty().bind( Bindings.max( 0, length.subtract( shownSpan ) ) );
		position.bindBidirectional( scrollBar.valueProperty() );

		xAxis.lowerBoundProperty().bind( scrollBar.valueProperty() );
		xAxis.upperBoundProperty().bind( scrollBar.valueProperty().add( shownSpan ) );

		lineChart.titleProperty().bind( Properties.forLabel( chartView ) );

		bindContent( lineChart.getData(), seriesList );
		bindContent( segments.getChildren(), lineSegmentViews );

		shownSpan.bind( xAxis.widthProperty().multiply( 30 ) );
	}

	private final class LineSegmentToSeriesFunction implements Function<LineSegment, XYChart.Series<Number, Number>>
	{
		@Override
		public XYChart.Series<Number, Number> apply( final LineSegment segment )
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getStatisticName() );

			series.setData( fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
			{
				@Override
				public Iterable<XYChart.Data<Number, Number>> call() throws Exception
				{
					return transform(
							segment.getStatistic().getPeriod( position.longValue() - 2000,
									position.longValue() + shownSpan.get() + 2000, 0, executionProperty.getValue() ),
							DATAPOINT_TO_CHARTDATA );
				}
			}, observableArrayList( executionProperty, position, shownSpan, poll ) ) );

			return series;
		}
	}

	private final class TestEventSegmentToSeriesFunction implements
			Function<TestEventSegment, XYChart.Series<Number, Number>>
	{
		@Override
		public XYChart.Series<Number, Number> apply( final TestEventSegment segment )
		{
			final XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getTypeLabel() );

			series.setData( fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
			{
				@Override
				public Iterable<XYChart.Data<Number, Number>> call() throws Exception
				{
					return transform(
							segment.getTestEventsInRange( executionProperty.getValue(), position.longValue() - 2000,
									position.longValue() + shownSpan.get() + 2000, 0 ), TESTEVENT_TO_CHARTDATA );
				}
			}, observableArrayList( executionProperty, position, shownSpan, poll ) ) );

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
	}

	private final class SegmentToViewFunction implements Function<LineSegment, LineSegmentView>
	{
		@Override
		public LineSegmentView apply( final LineSegment segment )
		{
			return new LineSegmentView( segment );
		}
	}

	public void addStatistic()
	{
		final Collection<Chart> charts = chartView.getChartGroup().getChildren();

		Collection<StatisticHolder> holders = getStatisticHolders( charts );

		final AddStatisticDialog dialog = new AddStatisticDialog( this, holders );
		dialog.setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				Selection selection = dialog.getSelection();

				for( Chart chart : charts )
				{
					if( selection.holder.equals( chart.getOwner() ) )
					{
						ChartView holderChartView = chartView.getChartGroup().getChartViewForChart( chart );
						( ( ConfigurableLineChartView )holderChartView ).addSegment( selection.variable, selection.statistic,
								firstNonNull( selection.source, StatisticVariable.MAIN_SOURCE ) );
						break;
					}
				}
				dialog.close();
			}
		} );
		dialog.show();
	}

	private static Collection<StatisticHolder> getStatisticHolders( final Collection<Chart> charts )
	{
		Collection<StatisticHolder> holders = new LinkedList<>();
		for( Chart chart : charts )
			if( chart.getOwner() instanceof StatisticHolder )
				holders.add( ( StatisticHolder )chart.getOwner() );
		return holders;
	}
}
