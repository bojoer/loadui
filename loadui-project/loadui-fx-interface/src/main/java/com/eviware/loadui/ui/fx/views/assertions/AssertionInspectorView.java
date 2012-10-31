package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.assertion.RangeConstraint;
import com.google.common.base.Function;

public class AssertionInspectorView extends HBox
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionInspectorView.class );

	private final ToolBox<Node> componentToolBox;
	private final StatisticsManager statisticsManager;
	private final ObservableList<AssertionToolboxItem> toolBoxContent;
	private final ListView<AssertionItem> assertionList;
	private final ObjectProperty<ProjectItem> projectProperty = new SimpleObjectProperty<>();

	private ObservableList<AssertionItem> assertions = FXCollections.emptyObservableList();

	public AssertionInspectorView( final StatisticsManager statisticsManager )
	{
		this.statisticsManager = statisticsManager;
		componentToolBox = new ToolBox<>( "Assertables" );
		toolBoxContent = createToolBoxContent();
		Bindings.bindContent( componentToolBox.getItems(), toolBoxContent );

		assertionList = ListViewBuilder.<AssertionItem> create().style( "-fx-padding: 20;" ).build();

		projectProperty.addListener( new ChangeListener<ProjectItem>()
		{
			@Override
			public void changed( ObservableValue<? extends ProjectItem> arg0, ProjectItem oldValue, ProjectItem newValue )
			{
				if( oldValue != null )
				{
					Bindings.unbindContent( assertionList.getItems(), assertions );
					assertionList.getItems().clear();
				}
				if( newValue != null )
				{
					assertions = ObservableLists.ofCollection( projectProperty.get(), AssertionAddon.ASSERTION_ITEMS,
							AssertionItem.class, projectProperty.get().getAddon( AssertionAddon.class ).getAssertions() );

					Bindings.bindContent( assertionList.getItems(), assertions );
				}
			}
		} );

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

	private void handleDrop( DraggableEvent event )
	{
		final StatisticHolder holder = ( StatisticHolder )event.getData();
		final CreateAssertionDialog dialog = new CreateAssertionDialog( this, holder );

		dialog.setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent actionEvent )
			{
				AssertableWrapper<ListenableValue<Number>> selectedWrapper = dialog.getSelectedValue();

				Resolver<? extends ListenableValue<Number>> resolver = selectedWrapper.getResolver();

				AssertionAddon assertionAddon = holder.getCanvas().getAddon( AssertionAddon.class );
				AssertionItem.Mutable<Number> assertion = assertionAddon.createAssertion( holder, resolver );

				assertion.setConstraint( new RangeConstraint( 0, 10 ) );
				assertion.setTolerance( 1, 0 );

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

	public ObjectProperty<ProjectItem> projectProperty()
	{
		return projectProperty;
	}

	private final static Function<StatisticHolder, AssertionToolboxItem> DESCRIPTOR_TO_LABELED = new Function<StatisticHolder, AssertionToolboxItem>()
	{
		@Override
		public AssertionToolboxItem apply( StatisticHolder holder )
		{
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
}
