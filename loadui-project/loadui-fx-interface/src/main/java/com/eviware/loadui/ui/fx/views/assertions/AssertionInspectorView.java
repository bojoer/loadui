package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.util.StringUtils;
import com.google.common.base.Function;

public class AssertionInspectorView extends HBox
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionInspectorView.class );

	private final ToolBox<Node> componentToolBox;
	private final StatisticsManager statisticsManager;
	private final static Function<StatisticHolder, Labeled> DESCRIPTOR_TO_LABELED = new Function<StatisticHolder, Labeled>()
	{
		@Override
		public Labeled apply( StatisticHolder holder )
		{
			log.debug( "APPLYING DESCRIPTOR_TO_LABELED" );

			AssertionToolboxItem view = new AssertionToolboxItem( holder );

			String category = "[NO CATEGORY]";
			if( holder instanceof ComponentItem )
				category = "COMPONENTS";
			else if( holder instanceof ComponentItem )
				category = "GLOBAL";

			ToolBox.setCategory( view, StringUtils.capitalize( category ) );
			return view;
		}
	};

	private final ObservableList<? extends Labeled> toolBoxContent;

	private final ListView<String> assertionList;

	public AssertionInspectorView( final StatisticsManager statisticsManager )
	{
		this.statisticsManager = statisticsManager;
		componentToolBox = new ToolBox<>( "Assertables" );
		toolBoxContent = createToolBoxContent();
		Bindings.bindContent( componentToolBox.getItems(), toolBoxContent );

		assertionList = ListViewBuilder.<String> create().build();
		HBox.setHgrow( assertionList, Priority.ALWAYS );
		assertionList.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( final DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED
						&& event.getSource() instanceof AssertionToolboxItem )
				{
					event.accept();
					event.consume();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					handleDrop( event );
					event.consume();
				}
			}
		} );

		getChildren().setAll( componentToolBox, assertionList );
	}

	private void handleDrop( DraggableEvent event )
	{
		final StatisticHolder holder = ( StatisticHolder )event.getData();
		log.debug( "ADDING HOLDER TO LIST: " + holder.getLabel() );

		new CreateAssertionDialog( this ).show();

		holder.getCanvas().getAddon( AssertionAddon.class ).createAssertion( holder.getCanvas(),
		/*
		 * //TODO: Here goes Statistic
		 */
		null );

		assertionList.getItems().add( holder.getLabel() );
	}

	private ObservableList<? extends Labeled> createToolBoxContent()
	{
		ObservableList<StatisticHolder> statisticHolders = ofCollection( statisticsManager,
				StatisticsManager.STATISTIC_HOLDERS, StatisticHolder.class, statisticsManager.getStatisticHolders() );

		return transform( fx( statisticHolders ), DESCRIPTOR_TO_LABELED );
	}
}
