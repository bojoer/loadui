package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Objects;

public class ChartGroupView extends VBox
{
	public static final Chart createSubChart( ChartGroup chartGroup, Chart.Owner owner )
	{
		Chart chart = chartGroup.createChart( owner );
		if( owner instanceof StatisticHolder )
		{
			ChartView chartView = chartGroup.getChartViewForChart( chart );
			if( chartView instanceof ConfigurableLineChartView )
			{
				for( Statistic.Descriptor statistic : ( ( StatisticHolder )owner ).getDefaultStatistics() )
				{
					( ( ConfigurableLineChartView )chartView ).addSegment( statistic.getStatisticVariableLabel(),
							statistic.getStatisticLabel(), statistic.getSource() );
				}
			}
		}
		else if( owner instanceof AssertionItem )
		{
			AssertionItem<?> assertionItem = ( AssertionItem<?> )owner;
			String typeLabel = BeanInjector.getBean( TestEventRegistry.class )
					.lookupFactory( ( ( TestEvent.Source<?> )assertionItem ).getType() ).getLabel();
			ChartView chartView = chartGroup.getChartViewForChart( chart );
			if( chartView instanceof ConfigurableLineChartView )
			{
				( ( ConfigurableLineChartView )chartView ).addSegment( typeLabel, assertionItem.getLabel() );
			}
		}

		return chart;
	}

	private final ChartGroup chartGroup;
	private final ObservableValue<Execution> executionProperty;
	private final Observable poll;

	@FXML
	private Label chartGroupLabel;

	@FXML
	private StackPane chartView;

	public ChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> executionProperty, Observable poll )
	{
		this.chartGroup = chartGroup;
		this.executionProperty = executionProperty;
		this.poll = poll;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		chartGroupLabel.textProperty().bind( forLabel( chartGroup ) );
		chartView.getChildren().setAll( createChart( chartGroup.getType() ) );
		//TODO: Bind SegmentViews.

		addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getData() instanceof Chart.Owner )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						createSubChart( chartGroup, ( Owner )event.getData() );
						event.consume();
					}
				}
			}
		} );
	}

	private Node createChart( String type )
	{
		if( Objects.equal( type, LineChartView.class.getName() ) )
		{
			return new LineChartViewNode( executionProperty, ( LineChartView )chartGroup.getChartView(), poll );
		}
		else
		{
			return LabelBuilder.create().text( "Unsupported chart type: " + type ).build();
		}
	}
}
