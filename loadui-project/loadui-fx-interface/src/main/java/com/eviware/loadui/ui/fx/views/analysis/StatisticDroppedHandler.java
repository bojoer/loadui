package com.eviware.loadui.ui.fx.views.analysis;

import javafx.event.EventHandler;
import javafx.scene.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.util.BeanInjector;

public class StatisticDroppedHandler implements EventHandler<DraggableEvent>
{

	private static final Logger log = LoggerFactory.getLogger( StatisticDroppedHandler.class );
	private final Node parent;
	private final ChartGroup chartGroup;

	public StatisticDroppedHandler( Node parent, ChartGroup chartGroup )
	{
		this.parent = parent;
		this.chartGroup = chartGroup;
	}

	@Override
	public void handle( DraggableEvent event )
	{
		if( event.getData() instanceof Chart.Owner )
		{
			if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
			{
				//TODO highlight the SegmentBox
				event.accept();
				event.consume();
			}
			else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
			{
				log.debug( "Creating sub-chart" );
				createSubChart( parent, chartGroup, ( Owner )event.getData() );

				event.consume();
			}
		}
	}

	public final static void createSubChart( Node parent, ChartGroup chartGroup, Chart.Owner owner )
	{
		Chart chart = chartGroup.createChart( owner );
		if( owner instanceof StatisticHolder )
			addStatistics( parent, chartGroup, ( StatisticHolder )owner, chart );
		else if( owner instanceof AssertionItem )
			addAssertion( chartGroup, ( AssertionItem<?> )owner, chart );
	}

	private static void addAssertion( ChartGroup chartGroup, AssertionItem<?> assertionItem, Chart chart )
	{
		String typeLabel = BeanInjector.getBean( TestEventRegistry.class )
				.lookupFactory( ( ( TestEvent.Source<?> )assertionItem ).getType() ).getLabel();
		ChartView chartView = chartGroup.getChartViewForChart( chart );
		if( chartView instanceof ConfigurableLineChartView )
		{
			( ( ConfigurableLineChartView )chartView ).addSegment( typeLabel, assertionItem.getLabel() );
		}
	}

	private static void addStatistics( Node parent, ChartGroup chartGroup, StatisticHolder owner, Chart chart )
	{
		ChartView newChartView = chartGroup.getChartViewForChart( chart );
		if( newChartView instanceof ConfigurableLineChartView )
		{
			new StatisticsDialog( parent, owner, ( ConfigurableLineChartView )newChartView ).show();
		}
	}

}
