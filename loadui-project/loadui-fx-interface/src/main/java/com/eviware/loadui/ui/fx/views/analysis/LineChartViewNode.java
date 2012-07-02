package com.eviware.loadui.ui.fx.views.analysis;

import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.NumberAxisBuilder;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollBarBuilder;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LineChartViewNode extends VBox
{

	private final ObservableValue<Execution> executionProperty;
	private final LineChartView chartView;

	private final LongProperty position = new SimpleLongProperty( 0 );
	private final LongProperty length = new SimpleLongProperty( 0 );
	private final LongProperty shownSpan = new SimpleLongProperty( 60000 );

	public LineChartViewNode( final ObservableValue<Execution> executionProperty, LineChartView chartView )
	{
		this.executionProperty = executionProperty;
		this.chartView = chartView;

		//TODO: executions lengths can change if it is not yet completed, in which case this has to be updated more often.
		length.bind( Bindings.createLongBinding( new Callable<Long>()
		{
			@Override
			public Long call() throws Exception
			{
				return executionProperty.getValue().getLength();
			}
		}, executionProperty ) );

		ScrollBar scrollbar = ScrollBarBuilder.create().build();
		scrollbar.visibleAmountProperty().bind( shownSpan );
		scrollbar.blockIncrementProperty().bind( shownSpan );
		scrollbar.maxProperty().bind( Bindings.max( 0, length.subtract( shownSpan ) ) );
		position.bindBidirectional( scrollbar.valueProperty() );

		NumberAxis xAxis = NumberAxisBuilder.create().autoRanging( false ).minorTickCount( 10 ).tickUnit( 10000 ).build();
		xAxis.lowerBoundProperty().bind( scrollbar.valueProperty() );
		xAxis.upperBoundProperty().bind( scrollbar.valueProperty().add( shownSpan ) );

		LineChart<Number, Number> lineChart = new LineChart<>( xAxis, new NumberAxis() );
		lineChart.setAnimated( false );
		lineChart.setLegendSide( Side.LEFT );
		lineChart.setCreateSymbols( false );
		lineChart.titleProperty().bind( Properties.forLabel( chartView ) );

		ObservableList<LineSegment> lineSegments = ObservableLists.ofCollection( chartView, LineChartView.SEGMENTS,
				LineSegment.class, Iterables.filter( chartView.getSegments(), LineSegment.class ) );
		ObservableList<Series<Number, Number>> series = ObservableLists.transform( lineSegments, segmentToSeries );
		Bindings.bindContent( lineChart.getData(), series );

		Label label = LabelBuilder.create().build();
		label.textProperty().bind( Bindings.convert( scrollbar.valueProperty() ) );

		shownSpan.bind( xAxis.widthProperty().multiply( 30 ) );

		getChildren().addAll( lineChart, scrollbar, label );
	}

	private static final Function<DataPoint<?>, XYChart.Data<Number, Number>> DATAPOINT_TO_CHARTDATA = new Function<DataPoint<?>, XYChart.Data<Number, Number>>()
	{
		@Override
		public Data<Number, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Number, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private final Function<LineSegment, XYChart.Series<Number, Number>> segmentToSeries = new Function<LineSegment, XYChart.Series<Number, Number>>()
	{
		@Override
		public Series<Number, Number> apply( final LineSegment segment )
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getStatisticName() );

			Bindings.bindContent( series.getData(),
					ObservableLists.fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
					{
						@Override
						public Iterable<Data<Number, Number>> call() throws Exception
						{
							return Iterables.transform(
									segment.getStatistic().getPeriod( position.longValue() - 2000,
											position.longValue() + shownSpan.get() + 2000, 0, executionProperty.getValue() ),
									DATAPOINT_TO_CHARTDATA );
						}
					}, executionProperty, position, shownSpan ) );

			return series;
		}
	};
}
