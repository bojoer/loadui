package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.beans.binding.Bindings.createStringBinding;

import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.util.execution.TestExecutionUtils;
import com.eviware.loadui.util.statistics.ChartUtils;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ScrollableLineChart extends HBox
{
	private ObservableValue<Execution> currentExecution;
	private ObservableValue<Execution> comparedExecution;
	private Observable poll;
	private LineChartView chartView;

	private ObservableList<Segment> segmentsList;
	private ObservableList<XYChart.Series<Number, Number>> seriesList;
	private ObservableList<SegmentView> segmentViews;

	private final LoadingCache<XYChart.Series<?, ?>, StringProperty> eventSeriesStyles = CacheBuilder.newBuilder()
			.build( new CacheLoader<XYChart.Series<?, ?>, StringProperty>()
			{
				@Override
				public StringProperty load( Series<?, ?> key ) throws Exception
				{
					return new SimpleStringProperty();
				}
			} );

	private static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroNever().appendWeeks()
			.appendSuffix( "w" ).appendSeparator( " " ).appendDays().appendSuffix( "d" ).appendSeparator( " " )
			.appendHours().appendSuffix( "h" ).appendSeparator( " " ).appendMinutes().appendSuffix( "m" ).toFormatter();

	final SimpleObjectProperty<ZoomLevel> zoomLevelProperty = new SimpleObjectProperty<ZoomLevel>(
			ScrollableLineChart.this, "zoom level", ZoomLevel.SECONDS );
	private final SimpleObjectProperty<ZoomLevel> tickZoomLevelProperty = new SimpleObjectProperty<ZoomLevel>(
			ScrollableLineChart.this, "tick zoom level", ZoomLevel.SECONDS );

	private Function<Segment, XYChart.Series<Number, Number>> segmentToSeries;
	private final Function<Segment, SegmentView> segmentToView = new SegmentToViewFunction();

	private final LongProperty position = new SimpleLongProperty( 0 );
	private final LongProperty shownSpan = new SimpleLongProperty( 60000 );
	private final LongProperty xScale = new SimpleLongProperty( 1 );

	protected static final Logger log = LoggerFactory.getLogger( ScrollableLineChart.class );

	private final MillisToTickMark millisToTickMark = new MillisToTickMark( zoomLevelProperty );

	@FXML
	private SegmentBox segmentBox;

	@FXML
	private LineChart<Number, Number> lineChart;

	@FXML
	private NumberAxis xAxis;

	@FXML
	private ChartScrollBar scrollBar;

	@FXML
	private Label ellapsedTime;

	@FXML
	private Label zoomLevel;

	public ScrollableLineChart()
	{
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		zoomLevel.textProperty().bind( createStringBinding( new Callable<String>()
		{
			@Override
			public String call() throws Exception
			{
				return zoomLevelProperty.get().getShortName();
			}
		}, zoomLevelProperty ) );

		xAxis.setTickLabelFormatter( millisToTickMark );

		scrollBar.visibleAmountProperty().bind( shownSpan );
		scrollBar.blockIncrementProperty().bind( shownSpan.divide( 2 ) );
		scrollBar.unitIncrementProperty().bind( shownSpan.divide( 40 ) );

		position.bind( scrollBar.leftSidePositionProperty() );

		positionProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				long millis = ( long )getPosition();
				Period period = new Period( millis );
				String formattedTime = timeFormatter.print( period.normalizedStandard() );
				ellapsedTime.setText( formattedTime );
			}
		} );

		maxProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( zoomLevelProperty.getValue() == ZoomLevel.ALL )
				{
					ZoomLevel tickLevel = ZoomLevel.forSpan( ( long )maxProperty().get() / 1000 );

					if( tickLevel != tickZoomLevelProperty.get() )
					{
						tickZoomLevelProperty.set( tickLevel );
						setTickMode( tickLevel );
					}
				}
			}

		} );

		getSegments().getChildren().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				int i = 0;
				for( Series<?, ?> series : seriesList )
				{
					segmentViews.get( i ).setColor( ChartUtils.lineToColor( series, seriesList ) );
					if( segmentViews.get( i ) instanceof EventSegmentView )
						eventSeriesStyles.getUnchecked( series ).set(
								"-fx-stroke: " + ChartUtils.lineToColor( series, seriesList ) + ";" );

					i++ ;
				}
			}
		} );

	}

	/**
	 * returns the tickmode ZoomLevel
	 */
	public ZoomLevel setZoomLevel( ZoomLevel zoomLevel )
	{
		ZoomLevel fromZoomLevel = zoomLevelProperty.get();
		zoomLevelProperty.set( zoomLevel );

		for( Node n : xAxis.getChildrenUnmodifiable() )
		{
			if( n instanceof Text )
			{
				Text text = ( Text )n;
				text.setText( millisToTickMark.changeZoomLevel( text.getText(), fromZoomLevel ) );
			}
		}

		if( zoomLevel.equals( ZoomLevel.ALL ) )
		{
			zoomLevel = ZoomLevel.forSpan( scrollBar.maxProperty().longValue() / 1000 );
			xScale.setValue( ( 1000.0 * zoomLevel.getInterval() ) / zoomLevel.getUnitWidth() );
			scrollBar.setDisable( true );

			xAxis.setAutoRanging( true );
			xAxis.lowerBoundProperty().unbind();
			xAxis.upperBoundProperty().unbind();
			shownSpan.bind( maxProperty() );
		}
		else
		{
			xScale.setValue( ( 1000.0 * zoomLevel.getInterval() ) / zoomLevel.getUnitWidth() );
			scrollBar.setDisable( false );

			xAxis.setAutoRanging( false );
			xAxis.lowerBoundProperty().bind( position );
			xAxis.upperBoundProperty().bind( position.add( shownSpan ).add( 2000d ) );
			shownSpan.bind( xAxis.widthProperty().multiply( xScale ) );
		}

		setTickMode( zoomLevel );

		log.debug( "ZoomLevel set to: " + zoomLevel.name() + " xScale is now: " + xScale.getValue() );

		if( scrollBar.followStateProperty().get() && TestExecutionUtils.isExecutionRunning() )
		{
			setPositionToLeftSide();
			log.debug( "Set position to: " + position.get() + " (following)" );
		}

		// recalculates the position after the span has changed
		scrollBar.setLeftSidePosition( position.getValue() );
		scrollBar.updateLeftSide();

		return zoomLevel;
	}

	public void setTickMode( ZoomLevel level )
	{
		int minorTickCount = level.getMajorTickInterval() / level.getInterval();

		// major tick interval
		xAxis.setTickUnit( ( 1000.0 * level.getInterval() * minorTickCount ) );

		xAxis.setMinorTickCount( minorTickCount == 1 ? 0 : minorTickCount );

		tickZoomLevelProperty.set( level );

		log.debug( "TickMode set to: " + level.name() );
	}

	public void setChartProperties( final ObservableValue<Execution> currentExecution,
			final ObservableValue<Execution> comparedExecution, LineChartView chartView, Observable poll )
	{
		this.currentExecution = currentExecution;
		this.comparedExecution = comparedExecution;
		this.chartView = chartView;
		this.poll = poll;

		segmentToSeries = new SegmentToSeriesFunction( currentExecution,
				javafx.collections.FXCollections.observableArrayList( currentExecution, positionProperty(), spanProperty(),
						poll, tickZoomLevelProperty, scaleUpdate() ), this, eventSeriesStyles );

		segmentsList = fx( ofCollection( chartView, LineChartView.SEGMENTS, Segment.class, chartView.getSegments() ) );
		seriesList = transform( segmentsList, segmentToSeries );
		segmentViews = transform( segmentsList, segmentToView );

		bindContent( getLineChart().getData(), seriesList );
		bindContent( getSegments().getChildren(), segmentViews );
	}

	public ReadOnlyLongProperty positionProperty()
	{
		return position;
	}

	public double getPosition()
	{
		return position.doubleValue();
	}

	public void setPosition( double position )
	{
		scrollBar.setLeftSidePosition( position );
	}

	public VBox getSegments()
	{
		return segmentBox.getSegmentsContainer();
	}

	public LineChart<Number, Number> getLineChart()
	{
		return lineChart;
	}

	public LongProperty spanProperty()
	{
		return shownSpan;
	}

	public long getSpan()
	{
		return shownSpan.get();
	}

	public javafx.beans.Observable scaleUpdate()
	{
		return segmentBox.scaleUpdate();
	}

	public DoubleProperty maxProperty()
	{
		return scrollBar.maxProperty();
	}

	public StringProperty titleProperty()
	{
		return lineChart.titleProperty();
	}

	public BooleanProperty scrollbarFollowStateProperty()
	{
		return scrollBar.followStateProperty();
	}

	public void setPositionToLeftSide()
	{
		scrollBar.setToLeftSide();
	}

	public ZoomLevel getTickZoomLevel()
	{
		return tickZoomLevelProperty.get();
	}

	private final class SegmentToViewFunction implements Function<Segment, SegmentView>
	{
		@Override
		public SegmentView apply( final Segment segment )
		{
			if( segment instanceof LineSegment )
				return new LineSegmentView( ( LineSegment )segment );
			else
				return new EventSegmentView( ( TestEventSegment )segment );
		}
	}

	private final class EventSegmentToViewFunction implements Function<TestEventSegment, EventSegmentView>
	{
		@Override
		public EventSegmentView apply( final TestEventSegment segment )
		{
			return new EventSegmentView( segment );
		}
	}

}
