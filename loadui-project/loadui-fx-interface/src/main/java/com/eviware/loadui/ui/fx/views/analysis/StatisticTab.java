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
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Function;

public class StatisticTab extends Tab
{
	private static final Logger log = LoggerFactory.getLogger( StatisticTab.class );

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

	private final Function<ChartGroup, ChartGroupView> chartGroupToView = new Function<ChartGroup, ChartGroupView>()
	{
		@Override
		public ChartGroupView apply( ChartGroup chartGroup )
		{
			return new ChartGroupView( chartGroup, currentExecution, comparedExecution, project, poll );
		}
	};

	private final StatisticPage page;
	private final ObservableValue<Execution> currentExecution;
	private final ObservableValue<Execution> comparedExecution;
	private final ProjectItem project;
	private final Observable poll;
	private final ObservableList<ChartGroupView> chartGroupViews;

	@FXML
	private VBox chartList;

	public StatisticTab( StatisticPage page, ObservableValue<Execution> currentExecution,
			ObservableValue<Execution> comparedExecution, ProjectItem project, Observable poll )
	{
		this.page = page;
		this.currentExecution = currentExecution;
		this.comparedExecution = comparedExecution;
		this.project = project;
		this.poll = poll;
		chartGroupViews = transform( fx( ofCollection( page ) ), chartGroupToView );

		FXMLUtils.load( this );
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
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						ChartGroup group = createChartGroup( page, null, null );
						ChartGroupView.createSubChart( group, ( Owner )event.getData() );
						event.consume();
					}
				}
			}
		} );

		Bindings.bindContent( chartList.getChildren(), chartGroupViews );
	}

	@Override
	public String toString()
	{
		return page.getLabel();
	}

}
