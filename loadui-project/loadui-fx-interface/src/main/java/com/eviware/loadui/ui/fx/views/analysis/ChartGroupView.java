package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.beans.binding.Bindings.isEmpty;
import static javafx.collections.FXCollections.observableArrayList;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
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
import com.eviware.loadui.ui.fx.api.PostActionEvent;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.InspectorHelpers;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

public class ChartGroupView extends VBox
{
	protected static final Logger log = LoggerFactory.getLogger( ChartGroupView.class );

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
	private final ProjectItem project;
	private final Observable poll;

	@FXML
	private Label chartGroupLabel;

	@FXML
	private ToggleButton componentGroupToggle;
	@FXML
	private HBox buttonBar;

	@FXML
	private VBox componentGroup;

	@FXML
	private StackPane chartView;

	private final ObservableList<LineChartViewNode> componentSubcharts;

	public ChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> executionProperty, ProjectItem project,
			Observable poll )
	{
		this.chartGroup = chartGroup;
		this.executionProperty = executionProperty;
		this.project = project;
		this.poll = poll;

		componentSubcharts = transform( fx( transform( ofCollection( chartGroup ), chartToChartView ) ),
				chartViewToChartViewHolder );

		FXMLUtils.load( this );

		log.debug( "Chart CREATED event fired." );
		log.debug( "Parent is: " + getParent() );

		final InvalidationListener fireCreatedEvent = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				fireEvent( PostActionEvent.create( PostActionEvent.WAS_CREATED, ChartGroupView.this ) );
				//TODO: remove this eventlistener since it's not used anymore.
			}
		};

		parentProperty().addListener( fireCreatedEvent );
	}

	@FXML
	private void initialize()
	{
		chartGroupToggleGroup = new ToggleGroup();
		componentGroupToggle.setToggleGroup( chartGroupToggleGroup );

		componentGroup.visibleProperty().bind( componentGroupToggle.selectedProperty() );

		bindContent( componentGroup.getChildren(), componentSubcharts );

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

	public ToggleGroup getChartGroupToggleGroup()
	{
		return chartGroupToggleGroup;
	}

	public HBox getButtonBar()
	{
		return buttonBar;
	}

	private Node createChart( String type )
	{
		if( Objects.equal( type, LineChartView.class.getName() ) )
		{
			return new LineChartViewNode( executionProperty, ( LineChartView )chartGroup.getChartView(), poll );
		}
		return LabelBuilder.create().text( "Unsupported chart type: " + type ).build();
	}

	public ChartGroup getChartGroup()
	{
		return chartGroup;
	}

	public static void applyChartViewDefaults( ChartView subChartView )
	{
		if( !"true".equals( subChartView.getAttribute( "saved", "false" ) ) )
		{
			subChartView.setAttribute( "saved", "true" );
			subChartView.setAttribute( "position", null );
			subChartView.setAttribute( "timeSpan", null );
			subChartView.setAttribute( "zoomLevel", null );
		}
	}

	private final Function<Chart, ChartView> chartToChartView = new Function<Chart, ChartView>()
	{
		@Override
		public ChartView apply( Chart chart )
		{
			ChartView subChartView = chartGroup.getChartViewForChart( chart );
			applyChartViewDefaults( subChartView );
			return subChartView;
		}
	};

	public final Function<ChartView, LineChartViewNode> chartViewToChartViewHolder = new Function<ChartView, LineChartViewNode>()
	{
		@Override
		public LineChartViewNode apply( ChartView _chartView )
		{
			return new LineChartViewNode( executionProperty, ( LineChartView )_chartView, poll );
		}
	};

	private ToggleGroup chartGroupToggleGroup;
}
