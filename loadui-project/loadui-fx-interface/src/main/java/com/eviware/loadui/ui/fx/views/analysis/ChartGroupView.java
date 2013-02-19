package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import static javafx.beans.binding.Bindings.bindContent;
import static javafx.beans.binding.Bindings.lessThan;
import static javafx.beans.binding.Bindings.size;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.PostActionEvent;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineChartViewNode;
import com.google.common.base.Function;
import com.google.common.base.Objects;

public class ChartGroupView extends VBox
{
	protected static final Logger log = LoggerFactory.getLogger( ChartGroupView.class );

	private final ChartGroup chartGroup;

	private final ObservableValue<Execution> currentExecution;
	private final Observable poll;

	@FXML
	private MenuButton chartMenuButton;

	@FXML
	private MenuItem renameChartViewItem;

	@FXML
	private MenuItem deleteChartViewItem;

	@FXML
	private ToggleButton componentGroupToggle;

	@FXML
	private AnchorPane componentGroupAnchor;

	@FXML
	private HBox buttonBar;

	final private VBox componentGroup = VBoxBuilder.create().styleClass( "sub-chart-group" ).build();

	@FXML
	private VBox mainChartGroup;

	@FXML
	private StackPane chartView;

	private final ObservableList<LineChartViewNode> componentSubcharts;

	public ChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> currentExecution, Observable poll )
	{
		this.chartGroup = chartGroup;
		this.currentExecution = currentExecution;
		this.poll = poll;

		componentSubcharts = transform( fx( transform( ofCollection( chartGroup ), chartToChartView ) ),
				chartViewToLineChartViewNode );

		FXMLUtils.load( this );

		final InvalidationListener fireCreatedEvent = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				fireEvent( PostActionEvent.create( PostActionEvent.WAS_CREATED, ChartGroupView.this ) );
			}
		};

		parentProperty().addListener( fireCreatedEvent );
	}

	@FXML
	private void initialize()
	{

		chartGroupToggleGroup = new ToggleGroup();
		componentGroupToggle.setToggleGroup( chartGroupToggleGroup );
		componentGroupToggle.disableProperty().bind( lessThan( 2, size( componentSubcharts ) ) );

		componentGroupToggle.selectedProperty().addListener( new ChangeListener<Boolean>()
		{

			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean newValue )
			{
				if( newValue )
				{
					ChartGroupView.this.getChildren().add( componentGroup );
				}
				else
				{
					ChartGroupView.this.getChildren().remove( componentGroup );
				}
			}
		} );

		bindContent( componentGroup.getChildren(), componentSubcharts );
		chartMenuButton.textProperty().bind( forLabel( chartGroup ) );
		chartView.getChildren().setAll( createChart( chartGroup.getType() ) );
		addEventHandler( DraggableEvent.ANY, new StatisticDroppedHandler( this, chartGroup ) );
	}

	@FXML
	protected void renameChart( ActionEvent evt )
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, chartGroup ) );
	}

	@FXML
	protected void deleteChart( ActionEvent evt )
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_DELETE, chartGroup ) );
	}

	public ToggleGroup getChartGroupToggleGroup()
	{
		return chartGroupToggleGroup;
	}

	public HBox getButtonBar()
	{
		return buttonBar;
	}

	public AnchorPane getComponentGroupAnchor()
	{
		return componentGroupAnchor;
	}

	public VBox getMainChartGroup()
	{
		return mainChartGroup;
	}

	private Node createChart( String type )
	{
		if( Objects.equal( type, LineChartView.class.getName() ) )
		{
			return new LineChartViewNode( currentExecution, ( LineChartView )chartGroup.getChartView(), poll );
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

	public final Function<ChartView, LineChartViewNode> chartViewToLineChartViewNode = new Function<ChartView, LineChartViewNode>()
	{
		@Override
		public LineChartViewNode apply( ChartView _chartView )
		{
			return new LineChartViewNode( currentExecution, ( LineChartView )_chartView, poll );
		}
	};

	private ToggleGroup chartGroupToggleGroup;
}
