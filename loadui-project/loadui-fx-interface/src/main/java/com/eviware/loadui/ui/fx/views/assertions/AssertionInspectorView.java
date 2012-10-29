package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.serialization.StatisticResolver;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class AssertionInspectorView extends HBox
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionInspectorView.class );

	private final ToolBox<Node> componentToolBox;
	private final StatisticsManager statisticsManager;
	private final WorkspaceProvider workspaceProvider;
	private Statistic<Number> selected;

	private final ObservableList<AssertionToolboxItem> toolBoxContent;

	private final ListView<AssertionItem> assertionList;

	public AssertionInspectorView( final StatisticsManager statisticsManager, final WorkspaceProvider workspaceProvider )
	{
		this.statisticsManager = statisticsManager;
		this.workspaceProvider = workspaceProvider;
		componentToolBox = new ToolBox<>( "Assertables" );
		toolBoxContent = createToolBoxContent();
		Bindings.bindContent( componentToolBox.getItems(), toolBoxContent );

		ProjectItem project = Iterables.getFirst( workspaceProvider.getWorkspace().getProjects(), null );

		log.debug( "project.getAddon( AssertionAddon.class ): " + project.getAddon( AssertionAddon.class ) );
		assertions = ObservableLists.ofCollection( project, AssertionAddon.ASSERTION_ITEMS,
				AssertionItem.class, project.getAddon( AssertionAddon.class ).getAssertions() );

		assertionList = ListViewBuilder.<AssertionItem> create().build();
		Bindings.bindContent( assertionList.getItems(), assertions );

		HBox.setHgrow( assertionList, Priority.ALWAYS );

		assertionList.setCellFactory( new Callback<ListView<AssertionItem>, ListCell<AssertionItem>>()
		{
			@Override
			public ListCell<AssertionItem> call( ListView<AssertionItem> arg0 )
			{
				return new AssertionItemCell();
			}
		} );

		assertionList.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( final DraggableEvent event )
			{
				log.debug( "event.getData() " + event.getData() );
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof StatisticHolder )
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

	@SuppressWarnings( "unchecked" )
	private void handleDrop( DraggableEvent event )
	{
		final StatisticHolder holder = ( StatisticHolder )event.getData();
		final CreateAssertionDialog dialog = new CreateAssertionDialog( this, holder );

		dialog.setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				selected = dialog.getSelectedValue();

				Resolver resolver = new StatisticResolver( selected );
				holder.getCanvas().getAddon( AssertionAddon.class )
						.createAssertion( holder, ( Resolver<? extends ListenableValue<Number>> )resolver );

				dialog.close();
			}
		} );
		dialog.show();
	}

	private ObservableList<AssertionToolboxItem> createToolBoxContent()
	{
		ObservableList<StatisticHolder> statisticHolders = ofCollection( statisticsManager,
				StatisticsManager.STATISTIC_HOLDERS, StatisticHolder.class, statisticsManager.getStatisticHolders() );

		return transform( fx( statisticHolders ), DESCRIPTOR_TO_LABELED );
	}

	private final static Function<StatisticHolder, AssertionToolboxItem> DESCRIPTOR_TO_LABELED = new Function<StatisticHolder, AssertionToolboxItem>()
	{
		@Override
		public AssertionToolboxItem apply( StatisticHolder holder )
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

	private ObservableList<AssertionItem> assertions;
}
