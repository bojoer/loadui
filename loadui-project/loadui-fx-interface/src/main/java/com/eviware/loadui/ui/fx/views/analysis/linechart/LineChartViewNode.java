package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static com.google.common.base.Objects.firstNonNull;
import static javafx.beans.binding.Bindings.createLongBinding;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.analysis.AddStatisticDialog;
import com.eviware.loadui.ui.fx.views.analysis.Selection;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.TestExecutionUtils;

public class LineChartViewNode extends VBox
{
	public static final String POSITION_ATTRIBUTE = "position";
	public static final String TIME_SPAN_ATTRIBUTE = "timeSpan";
	public static final String ZOOM_LEVEL_ATTRIBUTE = "zoomLevel";
	public static final String FOLLOW_ATTRIBUTE = "follow";

	protected static final Logger log = LoggerFactory.getLogger( LineChartViewNode.class );

	private static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroNever().appendWeeks()
			.appendSuffix( "w" ).appendSeparator( " " ).appendDays().appendSuffix( "d" ).appendSeparator( " " )
			.appendHours().appendSuffix( "h" ).appendSeparator( " " ).appendMinutes().appendSuffix( "m" ).toFormatter();

	private final TestExecutionTask executionTask = new TestExecutionTask()
	{

		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( phase == Phase.START )
			{
				followCheckBox.setDisable( false );
			}
			else if( phase == Phase.STOP )
			{
				followCheckBox.setDisable( true );

			}

		}
	};

	private final ObservableValue<Execution> currentExecution;
	private final Observable poll;
	private final LineChartView chartView;

	@FXML
	private ScrollableLineChart scrollableLineChart;

	@FXML
	private ZoomMenuButton zoomMenuButton;

	@FXML
	private CheckBox followCheckBox;

	public LineChartViewNode( final ObservableValue<Execution> currentExecution, LineChartView chartView, Observable poll )
	{
		log.debug( "new LineChartViewNode created! " );

		BeanInjector.getBean( TestRunner.class ).registerTask( executionTask, Phase.START, Phase.STOP );

		this.currentExecution = currentExecution;
		this.chartView = chartView;
		this.poll = poll;

		FXMLUtils.load( this );
	}

	@FXML
	public void initialize()
	{
		log.debug( "INITIALIZE LineChartViewNode STARTED" );

		loadAttributes();

		scrollableLineChart.setChartProperties( currentExecution, chartView, poll );

		scrollableLineChart.titleProperty().bind( Properties.forLabel( chartView ) );

		scrollableLineChart.maxProperty().bind( createLongBinding( new Callable<Long>()
		{
			@Override
			public Long call() throws Exception
			{
				return currentExecution.getValue().getLength();
			}
		}, currentExecution, poll ) );

		zoomMenuButton.selectedProperty().addListener( new ChangeListener<ZoomLevel>()
		{
			@Override
			public void changed( ObservableValue<? extends ZoomLevel> arg0, ZoomLevel arg1, ZoomLevel newZoomLevel )
			{
				setZoomLevel( newZoomLevel );
			}
		} );

		currentExecution.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				//sets the position to 0 when there is a new execution
				scrollableLineChart.setPosition( 0d );
			}
		} );
		followCheckBox.setDisable( !TestExecutionUtils.isExecutionRunning() );
		followCheckBox.selectedProperty().bindBidirectional( scrollableLineChart.scrollbarFollowStateProperty() );

	}

	private void loadAttributes()
	{
		ZoomLevel level;
		try
		{
			level = ZoomLevel.valueOf( chartView.getAttribute( ZOOM_LEVEL_ATTRIBUTE, "SECONDS" ) );
		}
		catch( IllegalArgumentException e )
		{
			level = ZoomLevel.SECONDS;
		}
		zoomMenuButton.setSelected( level );

		log.debug( "Zoomlevel: " + level.toString() );

		Boolean follow;
		try
		{
			follow = Boolean.parseBoolean( chartView.getAttribute( FOLLOW_ATTRIBUTE, "true" ) );
		}
		catch( IllegalArgumentException e )
		{
			follow = true;
		}
		followCheckBox.setSelected( follow );

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

	public void setZoomLevel( ZoomLevel zoomLevel )
	{
		scrollableLineChart.setZoomLevel( zoomLevel );
		chartView.setAttribute( ZOOM_LEVEL_ATTRIBUTE, zoomLevel.name() );

	}

	public LineChart<Number, Number> getLineChart()
	{
		return scrollableLineChart.getLineChart();
	}

}
