package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import static com.google.common.base.Strings.isNullOrEmpty;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Function;

public class StatisticTab extends Tab
{
	private static final Logger log = LoggerFactory.getLogger( StatisticTab.class );
	private final StatisticPage page;
	private final Observable poll;
	private ObservableList<ChartGroupView> chartGroupViews;
	private StringProperty tabTitle;

	@FXML
	private VBox chartList;

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

	public StatisticTab( StatisticPage page, Observable poll )
	{
		this.page = page;
		this.poll = poll;
		FXMLUtils.load( this );
	}

	public void setCurrentExecution( final ObservableValue<Execution> currentExecution )
	{
		chartGroupViews = transform( fx( ofCollection( page ) ), new Function<ChartGroup, ChartGroupView>()
		{
			@Override
			public ChartGroupView apply( ChartGroup chartGroup )
			{
				return new ChartGroupView( chartGroup, currentExecution, poll );
			}
		} );
		Bindings.bindContent( chartList.getChildren(), chartGroupViews );
	}

	@FXML
	private void initialize()
	{
		tabTitle = forLabel( page );
		textProperty().bindBidirectional( tabTitle );
		setId( UIUtils.toCssId( page.getLabel() ) );

		MenuItem renameItem = new MenuItem( "Rename" );
		renameItem.setId( "tab-rename" );
		renameItem.setOnAction( new EventHandler<ActionEvent>()
		{
			public void handle( ActionEvent _ )
			{
				chartList.fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, page ) );
			}
		} );
		MenuItem deleteItem = new MenuItem( "Delete" );
		deleteItem.setId( "tab-delete" );
		deleteItem.setOnAction( new EventHandler<ActionEvent>()
		{
			public void handle( ActionEvent _ )
			{
				getOnClosed().handle( _ );
			}
		} );

		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll( renameItem, deleteItem );
		setContextMenu( menu );

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

	}

	@Override
	public String toString()
	{
		return page.getLabel();
	}

}
