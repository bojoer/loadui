package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.api.statistics.StatisticHolder.STATISTIC_VARIABLES;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.google.common.base.Predicates.not;
import static javafx.beans.binding.Bindings.bindContent;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.ScrollableList;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Observables;
import com.eviware.loadui.ui.fx.util.Observables.Group;
import com.eviware.loadui.util.StringUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@SuppressWarnings( "rawtypes" )
public class AssertionInspectorView extends HBox
{
	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( AssertionInspectorView.class );

	private static final Function<AssertionItem, AssertionView> ASSERTION_TO_VIEW = new Function<AssertionItem, AssertionView>()
	{
		@Override
		public AssertionView apply( AssertionItem assertion )
		{
			return new AssertionView( assertion );
		}
	};

	private final StatisticsManager statisticsManager;
	private final ObservableList<AssertionToolboxItem> toolBoxContent;
	private final ObjectProperty<ProjectItem> projectProperty = new SimpleObjectProperty<>();

	@FXML
	private ToolBox<Node> componentToolBox;

	@FXML
	private ScrollableList<AssertionView> assertionList;

	private ObservableList<AssertionView> assertions = FXCollections.emptyObservableList();

	public AssertionInspectorView( final StatisticsManager statisticsManager )
	{
		this.statisticsManager = statisticsManager;
		toolBoxContent = createToolBoxContent();

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
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
					assertions = ObservableLists.transform( ObservableLists.fx( ObservableLists.ofCollection(
							projectProperty.get(), AssertionAddon.ASSERTION_ITEMS, AssertionItem.class, projectProperty.get()
									.getAddon( AssertionAddon.class ).getAssertions() ) ), ASSERTION_TO_VIEW );

					Bindings.bindContent( assertionList.getItems(), assertions );
				}
			}
		} );

		Bindings.bindContent( componentToolBox.getItems(), toolBoxContent );

		assertionList.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( final DraggableEvent event )
			{
				if( event.getData() instanceof StatisticHolder )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
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
			}
		} );
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
				AssertableWrapper<ListenableValue<Number>> selectedWrapper = dialog.getSelectedAssertable();

				Resolver<? extends ListenableValue<Number>> resolver = selectedWrapper.getResolver();

				AssertionAddon assertionAddon = holder.getCanvas().getAddon( AssertionAddon.class );
				AssertionItem.Mutable<Number> assertion = assertionAddon.createAssertion( holder, resolver );

				assertion.setConstraint( dialog.getConstraint() );
				Pair<Integer, Integer> tolerance = dialog.getTolerance();
				assertion.setTolerance( tolerance.getValue(), tolerance.getKey() );

				dialog.close();
			}
		} );
		dialog.show();
	}

	private ObservableList<AssertionToolboxItem> createToolBoxContent()
	{
		ObservableList<StatisticHolder> statisticHolders = ofCollection( statisticsManager,
				StatisticsManager.STATISTIC_HOLDERS, StatisticHolder.class, statisticsManager.getStatisticHolders() );

		statisticVariables = transform( statisticHolders, GET_VARIABLES );

		variableGroup = Observables.group();
		bindContent( variableGroup.getObservables(), statisticVariables );
		variableGroup.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				nonEmptyVariables.setAll( Collections2.filter( statisticVariables, not( IS_EMPTY ) ) );
			}
		} );

		ObservableList<StatisticHolder> statisticHoldersWithVariables = transform( nonEmptyVariables, GET_HOLDER );

		return transform( fx( statisticHoldersWithVariables ), HOLDER_TO_TOOLBOX_ITEM );
	}

	private static Predicate<ObservableList<StatisticVariable>> IS_EMPTY = new Predicate<ObservableList<StatisticVariable>>()
	{
		@Override
		public boolean apply( @Nullable ObservableList<StatisticVariable> c )
		{
			return c.isEmpty();
		}
	};

	public ObjectProperty<ProjectItem> projectProperty()
	{
		return projectProperty;
	}

	private static Function<ObservableList<StatisticVariable>, StatisticHolder> GET_HOLDER = new Function<ObservableList<StatisticVariable>, StatisticHolder>()
	{
		@Override
		@Nullable
		public StatisticHolder apply( @Nullable ObservableList<StatisticVariable> variables )
		{
			return variables.iterator().next().getStatisticHolder();
		}
	};

	private static Function<StatisticHolder, ObservableList<StatisticVariable>> GET_VARIABLES = new Function<StatisticHolder, ObservableList<StatisticVariable>>()
	{
		@Override
		@Nullable
		public ObservableList<StatisticVariable> apply( @Nullable StatisticHolder holder )
		{
			return ofCollection( holder, STATISTIC_VARIABLES, StatisticVariable.class, holder.getStatisticVariables() );
		}
	};

	private final static Function<StatisticHolder, AssertionToolboxItem> HOLDER_TO_TOOLBOX_ITEM = new Function<StatisticHolder, AssertionToolboxItem>()
	{
		@Override
		public AssertionToolboxItem apply( StatisticHolder holder )
		{
			AssertionToolboxItem view = new AssertionToolboxItem( holder );

			String category = "[NO CATEGORY]";
			if( holder instanceof ComponentItem )
				category = "COMPONENTS";
			else if( holder instanceof CanvasItem )
				category = "GLOBAL";

			ToolBox.setCategory( view, StringUtils.capitalize( category ) );
			return view;
		}
	};

	private ObservableList<ObservableList<StatisticVariable>> statisticVariables;

	private final ObservableList<ObservableList<StatisticVariable>> nonEmptyVariables = FXCollections
			.observableArrayList();

	private Group variableGroup;
}
