package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fromExpression;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.google.common.base.Objects.firstNonNull;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.beans.binding.Bindings.createLongBinding;
import static javafx.collections.FXCollections.observableArrayList;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.util.Callback;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.analysis.linechart.EventSegmentView;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineSegmentView;
import com.eviware.loadui.ui.fx.views.analysis.linechart.SegmentView;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

public class LineChartViewNode extends VBox
{
	public static final String POSITION_ATTRIBUTE = "position";
	public static final String TIME_SPAN_ATTRIBUTE = "timeSpan";
	public static final String ZOOM_LEVEL_ATTRIBUTE = "zoomLevel";
	public static final String FOLLOW_ATTRIBUTE = "follow";

	public static final String ZOOM_LEVEL = "zoomLevel";
	public static final String FOLLOW = "follow";
	public static final String POSITION = "position";

	protected static final Logger log = LoggerFactory.getLogger( LineChartViewNode.class );

	private static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroNever().appendWeeks()
			.appendSuffix( "w" ).appendSeparator( " " ).appendDays().appendSuffix( "d" ).appendSeparator( " " )
			.appendHours().appendSuffix( "h" ).appendSeparator( " " ).appendMinutes().appendSuffix( "m" ).toFormatter();

	private static final Function<DataPoint<?>, XYChart.Data<Number, Number>> DATAPOINT_TO_CHARTDATA = new Function<DataPoint<?>, XYChart.Data<Number, Number>>()
	{
		@Override
		public XYChart.Data<Number, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Number, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private final LoadingCache<XYChart.Series<?, ?>, StringProperty> eventSeriesStyles = CacheBuilder.newBuilder()
			.build( new CacheLoader<XYChart.Series<?, ?>, StringProperty>()
			{
				@Override
				public StringProperty load( Series<?, ?> key ) throws Exception
				{
					return new SimpleStringProperty();
				}
			} );

	private final Function<Segment, XYChart.Series<Number, Number>> segmentToSeries = new SegmentToSeriesFunction();
	private final Function<Segment, SegmentView> segmentToView = new SegmentToViewFunction();

	private final ObservableValue<Execution> executionProperty;
	private final Observable poll;
	private final LineChartView chartView;

	private final LongProperty length = new SimpleLongProperty( 0 );

	private ObservableList<Segment> segmentsList;
	private ObservableList<XYChart.Series<Number, Number>> seriesList;
	private ObservableList<SegmentView> segmentViews;

	private final SimpleObjectProperty<ZoomLevel> tickZoomLevelProperty = new SimpleObjectProperty<ZoomLevel>(
			LineChartViewNode.this, "tick zoom level", ZoomLevel.SECONDS );

	@FXML
	private ScrollableLineChart scrollableLineChart;

	@FXML
	private Label timer;

	@FXML
	private ZoomMenuButton zoomMenuButton;

	public LineChartViewNode( final ObservableValue<Execution> executionProperty, LineChartView chartView,
			Observable poll )
	{
		log.debug( "new LineChartViewNode created! " );

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

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		segmentsList = fx( ofCollection( chartView, LineChartView.SEGMENTS, Segment.class, chartView.getSegments() ) );
		seriesList = transform( segmentsList, segmentToSeries );
		segmentViews = transform( segmentsList, segmentToView );

		scrollableLineChart.maxProperty().bind( length );
		scrollableLineChart.titleProperty().bind( Properties.forLabel( chartView ) );

		scrollableLineChart.positionProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				long millis = ( long )scrollableLineChart.getPosition();
				Period period = new Period( millis );
				String formattedTime = timeFormatter.print( period.normalizedStandard() );
				timer.setText( formattedTime );
			}
		} );

		tickZoomLevelProperty.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				scrollableLineChart.setTickMode( tickZoomLevelProperty.get() );

			}
		} );

		scrollableLineChart.getSegments().getChildren().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				int i = 0;
				for( Series<?, ?> series : seriesList )
				{
					segmentViews.get( i ).setColor( seriesToColor( series ) );
					if( segmentViews.get( i ) instanceof EventSegmentView )
						eventSeriesStyles.getUnchecked( series ).set( "-fx-stroke: " + seriesToColor( series ) + ";" );

					i++ ;
				}
			}
		} );

		bindContent( scrollableLineChart.getLineChart().getData(), seriesList );
		bindContent( scrollableLineChart.getSegments().getChildren(), segmentViews );

		length.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( zoomMenuButton.selectedProperty().getValue() == ZoomLevel.ALL )
				{
					ZoomLevel tickLevel = ZoomLevel.forSpan( length.get() / 1000 );

					if( tickLevel != tickZoomLevelProperty.get() )
					{
						tickZoomLevelProperty.set( tickLevel );
					}
				}

			}

		} );

		ZoomLevel level;
		try
		{
			level = ZoomLevel.valueOf( chartView.getAttribute( ZOOM_LEVEL_ATTRIBUTE, "SECONDS" ) );
			log.debug( " ZoomLevel already set to:" + level.toString() );
		}
		catch( IllegalArgumentException e )
		{
			level = ZoomLevel.SECONDS;
			log.debug( " New chart - default ZoomLevel:" + level.toString() );
		}

		zoomMenuButton.selectedProperty().addListener( new ChangeListener<ZoomLevel>()
		{
			@Override
			public void changed( ObservableValue<? extends ZoomLevel> arg0, ZoomLevel arg1, ZoomLevel newZoomLevel )
			{
				setZoomLevel( newZoomLevel );
			}
		} );

		zoomMenuButton.setSelected( level );

		executionProperty.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				//sets the position to 0 when there is a new excecution
				scrollableLineChart.setPosition( 0d );
			}
		} );

	}

	private String seriesToColor( Series<?, ?> series )
	{
		int seriesOrder = seriesList.indexOf( series );

		switch( seriesOrder % 8 )
		{
		case 0 :
			return "#f9d900";
		case 1 :
			return "#a9e200";
		case 2 :
			return "#22bad9";
		case 3 :
			return "#0181e2";
		case 4 :
			return "#2f357f";
		case 5 :
			return "#860061";
		case 6 :
			return "#c62b00";
		case 7 :
			return "#ff5700";
		}

		throw new RuntimeException( "This is mathematically impossible!" );
	}

	private final class SegmentToSeriesFunction implements Function<Segment, XYChart.Series<Number, Number>>
	{
		@Override
		public XYChart.Series<Number, Number> apply( final Segment segment )
		{
			if( segment instanceof LineSegment )
				return lineSegmentToSeries( ( LineSegment )segment );
			else
				return eventSegmentToSeries( ( TestEventSegment )segment );
		}

		private Series<Number, Number> lineSegmentToSeries( final LineSegment segment )
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getStatisticName() );

			series.setData( fromExpression(
					new Callable<Iterable<XYChart.Data<Number, Number>>>()
					{
						@Override
						public Iterable<XYChart.Data<Number, Number>> call() throws Exception
						{
							return Iterables.transform(
									segment.getStatistic().getPeriod(
											scrollableLineChart.positionProperty().longValue() - 2000,
											scrollableLineChart.positionProperty().longValue()
													+ scrollableLineChart.getSpan().longValue() + 2000,
											tickZoomLevelProperty.getValue().getLevel(), executionProperty.getValue() ),
									DATAPOINT_TO_CHARTDATA );
						}
					},
					observableArrayList( executionProperty, scrollableLineChart.positionProperty(),
							scrollableLineChart.getSpan(), poll, tickZoomLevelProperty ) ) );

			return series;
		}

		public XYChart.Series<Number, Number> eventSegmentToSeries( final TestEventSegment segment )
		{
			final XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName( segment.getTypeLabel() );

			series.setData( fromExpression(
					new Callable<Iterable<XYChart.Data<Number, Number>>>()
					{
						@Override
						public Iterable<XYChart.Data<Number, Number>> call() throws Exception
						{
							return Iterables.transform( segment.getTestEventsInRange( executionProperty.getValue(),
									scrollableLineChart.positionProperty().longValue() - 2000, scrollableLineChart
											.positionProperty().longValue() + scrollableLineChart.getSpan().longValue() + 2000,
									tickZoomLevelProperty.getValue().getLevel() ),
									new Function<TestEvent, XYChart.Data<Number, Number>>()
									{
										@Override
										public XYChart.Data<Number, Number> apply( TestEvent event )
										{
											XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>( event
													.getTimestamp(), 10.0 );
											Line eventLine = LineBuilder.create().endY( 600 ).managed( false ).build();
											eventLine.styleProperty().bind( eventSeriesStyles.getUnchecked( series ) );
											data.setNode( eventLine );
											return data;
										}
									} );
						}
					},
					observableArrayList( executionProperty, scrollableLineChart.positionProperty(),
							scrollableLineChart.getSpan(), poll ) ) );

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

	@FXML
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

	private void setZoomLevel( ZoomLevel zoomLevel )
	{
		ZoomLevel tickMode = scrollableLineChart.setZoomLevel( zoomLevel );
		tickZoomLevelProperty.set( tickMode );
		chartView.setAttribute( ZOOM_LEVEL_ATTRIBUTE, zoomLevel.name() );
	}

	public Node getLineChart()
	{
		return scrollableLineChart.getLineChart();
	}
}
