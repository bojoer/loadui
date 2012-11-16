package com.eviware.loadui.ui.fx.views.analysis;

import java.util.concurrent.Callable;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LineChartViewNode extends VBox
{
	private static final Function<DataPoint<?>, XYChart.Data<Number, Number>> DATAPOINT_TO_CHARTDATA = new Function<DataPoint<?>, XYChart.Data<Number, Number>>()
	{
		@Override
		public XYChart.Data<Number, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Number, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private final Function<LineSegment, XYChart.Series<Number, Number>> segmentToSeries = new SegmentToSeriesFunction();
	private final Function<LineSegment, Node> segmentToLegend = new SegmentToLegendFunction();

	private final ObservableValue<Execution> executionProperty;
	private final Observable poll;
	private final LineChartView chartView;

	private final LongProperty position = new SimpleLongProperty( 0 );
	private final LongProperty length = new SimpleLongProperty( 0 );
	private final LongProperty shownSpan = new SimpleLongProperty( 60000 );
	private final ObservableList<LineSegment> lineSegments;
	private final ObservableList<XYChart.Series<Number, Number>> seriesList;
	private final ObservableList<Node> legends;

	@FXML
	private VBox segments;

	@FXML
	private LineChart<Number, Number> lineChart;

	@FXML
	private NumberAxis xAxis;

	@FXML
	private ScrollBar scrollBar;

	public LineChartViewNode( final ObservableValue<Execution> executionProperty, LineChartView chartView,
			Observable poll )
	{
		this.executionProperty = executionProperty;
		this.chartView = chartView;
		this.poll = poll;

		length.bind( Bindings.createLongBinding( new Callable<Long>()
		{
			@Override
			public Long call() throws Exception
			{
				return executionProperty.getValue().getLength();
			}
		}, executionProperty, poll ) );

		lineSegments = ObservableLists.fx( ObservableLists.ofCollection( chartView, LineChartView.SEGMENTS,
				LineSegment.class, Iterables.filter( chartView.getSegments(), LineSegment.class ) ) );
		seriesList = ObservableLists.transform( lineSegments, segmentToSeries );
		legends = ObservableLists.transform( lineSegments, segmentToLegend );

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

		Bindings.bindContent( lineChart.getData(), seriesList );
		Bindings.bindContent( segments.getChildren(), legends );

		shownSpan.bind( xAxis.widthProperty().multiply( 30 ) );
	}

	private final class SegmentToSeriesFunction implements Function<LineSegment, XYChart.Series<Number, Number>>
	{
		@Override
		public XYChart.Series<Number, Number> apply( final LineSegment segment )
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getStatisticName() );

			series.setData( ObservableLists.fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
			{
				@Override
				public Iterable<XYChart.Data<Number, Number>> call() throws Exception
				{
					return Iterables.transform(
							segment.getStatistic().getPeriod( position.longValue() - 2000,
									position.longValue() + shownSpan.get() + 2000, 0, executionProperty.getValue() ),
							DATAPOINT_TO_CHARTDATA );
				}
			}, executionProperty, position, shownSpan, poll ) );

			return series;
		}
	}

	private final class SegmentToLegendFunction implements Function<LineSegment, Node>
	{
		@Override
		public Node apply( final LineSegment segment )
		{
			//TODO: Bind and possibly do some ChartNamePrettifying ;)
			Label label = new Label( segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
					+ segment.getStatisticName() );
			label.getStyleClass().add( "slim-icon" );

			return label;
		}
	}
}
