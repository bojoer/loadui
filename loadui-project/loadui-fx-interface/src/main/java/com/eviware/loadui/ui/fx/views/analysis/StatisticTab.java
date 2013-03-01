package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import static com.google.common.base.Strings.isNullOrEmpty;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.NonSingletonFactory;
import com.eviware.loadui.ui.fx.api.analysis.ChartGroupView;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.DefaultNonSingletonFactory;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Function;

public class StatisticTab extends Tab
{
	private static final Logger log = LoggerFactory.getLogger( StatisticTab.class );
	private final StatisticPage page;
	private final ObservableValue<Execution> currentExecution;
	private final Observable poll;
	private final ObservableList<ChartGroupView> chartGroupViews;

	@FXML
	private VBox chartList;

	private final Function<ChartGroup, ChartGroupView> chartGroupToView = new Function<ChartGroup, ChartGroupView>()
	{
		@Override
		public ChartGroupView apply( ChartGroup chartGroup )
		{
			return getNonSingletonFactory().createChartGroupView( chartGroup, currentExecution, poll );
		}
	};

	private final Function<ChartGroupView, Node> chartGroupViewToNode = new Function<ChartGroupView, Node>()
	{
		@Override
		public Node apply( ChartGroupView chartGroupView )
		{
			return chartGroupView.getNode();
		}
	};

	protected NonSingletonFactory getNonSingletonFactory()
	{
		NonSingletonFactory factory = BeanInjector.getNonCachedBeanOrNull( NonSingletonFactory.class );
		if( factory != null )
			return factory;
		else
			return DefaultNonSingletonFactory.get();
	}

	public final static StatisticPage createStatisticPage( StatisticPages pages, @Nullable String label )
	{
		StatisticPage page = pages.createPage( isNullOrEmpty( label ) ? "Page " + ( pages.getChildCount() + 1 ) : label );
		return page;
	}

	public static final ChartGroup createChartGroup( StatisticPage page, String chartType, String label )
	{
		ChartGroup group = page.createChartGroup( isNullOrEmpty( chartType ) ? LineChartView.class.getName() : chartType,
				isNullOrEmpty( label ) ? "Chart " + ( page.getChildCount() + 1 ) : label );

		return group;
	}

	public StatisticTab( StatisticPage page, ObservableValue<Execution> currentExecution, Observable poll )
	{
		this.page = page;
		this.currentExecution = currentExecution;
		this.poll = poll;
		chartGroupViews = transform( fx( ofCollection( page ) ), chartGroupToView );

		FXMLUtils.load( this );
	}

	public void setNonSingletonFactory( NonSingletonFactory factory )
	{

	}

	@FXML
	private void initialize()
	{
		textProperty().bind( forLabel( page ) );

		forLabel( page ).addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String newLabel )
			{
				setId( UIUtils.toCssId( newLabel ) );
			}
		} );
		setId( UIUtils.toCssId( page.getLabel() ) );

		setOnClosed( new EventHandler<Event>()
		{
			@Override
			public void handle( Event _ )
			{
				page.delete();
			}
		} );

		getContent().addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getData() instanceof Chart.Owner )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						//TODO make content area somehow highlighted
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						Owner owner = ( Owner )event.getData();
						log.debug( "Creating new Chart Group" );
						ChartGroup group = createChartGroup( page, null, null );
						StatisticDroppedHandler.createSubChart( chartList, group, owner );
						event.consume();
					}
				}
			}
		} );

		Bindings
				.bindContent( chartList.getChildren(), ObservableLists.transform( chartGroupViews, chartGroupViewToNode ) );
	}

	@Override
	public String toString()
	{
		return page.getLabel();
	}

}
