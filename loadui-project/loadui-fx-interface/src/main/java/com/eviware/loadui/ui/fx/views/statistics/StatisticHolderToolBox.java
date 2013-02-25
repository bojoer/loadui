package com.eviware.loadui.ui.fx.views.statistics;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.google.common.base.Predicates.not;
import static javafx.beans.binding.Bindings.bindContent;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.Observables;
import com.eviware.loadui.ui.fx.util.Observables.Group;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class StatisticHolderToolBox extends ToolBox<Node>
{
	private static final Predicate<ObservableList<StatisticVariable>> IS_EMPTY = new Predicate<ObservableList<StatisticVariable>>()
	{
		@Override
		public boolean apply( ObservableList<StatisticVariable> list )
		{
			return list.isEmpty();
		}
	};

	private static final Function<StatisticHolder, ObservableList<StatisticVariable>> GET_VARIABLES = new Function<StatisticHolder, ObservableList<StatisticVariable>>()
	{
		@Override
		public ObservableList<StatisticVariable> apply( StatisticHolder holder )
		{
			return ofCollection( holder, StatisticHolder.STATISTIC_VARIABLES, StatisticVariable.class,
					holder.getStatisticVariables() );
		}
	};

	private static final Function<ObservableList<StatisticVariable>, StatisticHolder> GET_HOLDER = new Function<ObservableList<StatisticVariable>, StatisticHolder>()
	{
		@Override
		public StatisticHolder apply( ObservableList<StatisticVariable> variables )
		{
			return variables.iterator().next().getStatisticHolder();
		}
	};

	private static final Function<StatisticHolder, StatisticHolderToolboxItem> HOLDER_TO_TOOLBOX_ITEM = new Function<StatisticHolder, StatisticHolderToolboxItem>()
	{
		@Override
		public StatisticHolderToolboxItem apply( StatisticHolder holder )
		{
			StatisticHolderToolboxItem view = new StatisticHolderToolboxItem( holder );

			String category = "[NO CATEGORY]";
			if( holder instanceof ComponentItem )
				category = "COMPONENTS";
			else if( holder instanceof CanvasItem )
				category = "GLOBAL";

			ToolBox.setCategory( view, StringUtils.capitalize( category ) );
			return view;
		}
	};

	private final ObservableList<StatisticHolder> statisticHolders;
	private final ObservableList<StatisticHolderToolboxItem> statisticHolderItems;
	private final Group variableGroup;

	public StatisticHolderToolBox()
	{
		StatisticsManager statisticsManager = BeanInjector.getBean( StatisticsManager.class );
		statisticHolders = ofCollection( statisticsManager, StatisticsManager.STATISTIC_HOLDERS, StatisticHolder.class,
				statisticsManager.getStatisticHolders() );

		final ObservableList<ObservableList<StatisticVariable>> statisticVariables = transform( statisticHolders,
				GET_VARIABLES );

		final ObservableList<ObservableList<StatisticVariable>> nonEmptyVariables = FXCollections.observableArrayList();
		variableGroup = Observables.group();

		bindContent( variableGroup.getObservables(), statisticVariables );

		InvalidationListener variablesChanged = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				nonEmptyVariables.setAll( Collections2.filter( statisticVariables, not( IS_EMPTY ) ) );
			}
		};
		variableGroup.addListener( variablesChanged );
		variableGroup.getObservables().addListener( variablesChanged );
		nonEmptyVariables.setAll( Collections2.filter( statisticVariables, not( IS_EMPTY ) ) );

		ObservableList<StatisticHolder> statisticHoldersWithVariables = transform( nonEmptyVariables, GET_HOLDER );
		statisticHolderItems = transform( fx( statisticHoldersWithVariables ), HOLDER_TO_TOOLBOX_ITEM );

		Bindings.bindContent( getItems(), statisticHolderItems );
	}

	public ObservableList<StatisticHolder> getStatisticHolders()
	{
		return statisticHolders;
	}

	public ObservableList<StatisticHolderToolboxItem> getStatisticHolderToolboxItems()
	{
		return statisticHolderItems;
	}
}
