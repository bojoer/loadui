package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.TreeUtils.dummyItem;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItemBuilder;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.util.TreeUtils;
import com.eviware.loadui.ui.fx.views.assertions.LabeledTreeCell;

public class StatisticTree extends TreeView<Labeled> implements Validatable
{
	protected static final Logger log = LoggerFactory.getLogger( StatisticTree.class );

	public static BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	// Used to prevent unwanted chain reactions when forcing TreeItems to collapse. 
	public final AtomicBoolean isForceCollapsing = new AtomicBoolean( false );

	private Collection<? extends AgentItem> agents;

	public static StatisticTree forHolders( Collection<StatisticHolder> holders )
	{
		TreeItem<Labeled> holderItem = dummyItem( "ROOT" );
		return new StatisticTree( holders, holderItem );
	}

	StatisticTree( Collection<StatisticHolder> holders, TreeItem<Labeled> root )
	{
		super( root );
		setShowRoot( false );
		getStyleClass().add( "assertable-tree" );

		agents = holders.iterator().next().getCanvas().getProject().getWorkspace().getAgents();

		if( holders.size() == 1 )
			addVariablesToTree( holders.iterator().next(), root );
		else
		{
			for( StatisticHolder holder : holders )
			{
				TreeItem<Labeled> holderItem = new TreeItem<Labeled>( holder );
				holderItem.expandedProperty().addListener( new ExpandedTreeItemsLimiter( holderItem, root ) );
				addVariablesToTree( holder, holderItem );
				root.getChildren().add( holderItem );
			}
		}

		getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Labeled>>()
		{
			@Override
			public void changed( ObservableValue<? extends TreeItem<Labeled>> arg0, TreeItem<Labeled> oldValue,
					TreeItem<Labeled> newValue )
			{
				if( isForceCollapsing.get() )
					return;
				if( newValue == null || newValue.getValue() == null )
				{
					isValidProperty.set( false );
				}
				else
				{
					if( !newValue.isLeaf() )
					{
						newValue.setExpanded( true );
						getSelectionModel().clearSelection();
						isValidProperty.set( false );
					}
					else
					{
						isValidProperty.set( true );
					}
				}
			}
		} );

		setCellFactory( new Callback<TreeView<Labeled>, TreeCell<Labeled>>()
		{
			@Override
			public TreeCell<Labeled> call( TreeView<Labeled> treeView )
			{
				return new LabeledTreeCell();
			}
		} );
	}

	private void addVariablesToTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		for( String variableName : holder.getStatisticVariableNames() )
		{
			StatisticVariable variable = holder.getStatisticVariable( variableName );
			final TreeItem<Labeled> variableItem = new TreeItem<Labeled>( variable );
			variableItem.expandedProperty().addListener( new ExpandedTreeItemsLimiter( variableItem, root ) );

			for( String statisticName : holder.getStatisticVariable( variableName ).getStatisticNames() )
			{
				Statistic<Number> statistic = ( Statistic<Number> )holder.getStatisticVariable( variableName )
						.getStatistic( statisticName, StatisticVariable.MAIN_SOURCE );
				final TreeItem<Labeled> statisticItem = new TreeItem<Labeled>( statistic );
				if( !agents.isEmpty() )
				{
					statisticItem.getChildren().add( dummyItem( "Total" ) );
				}
				for( AgentItem agent : agents )
				{
					statisticItem.getChildren().add( new TreeItem<Labeled>( agent ) );
				}

				variableItem.getChildren().add( statisticItem );
			}
			root.getChildren().add( variableItem );
		}
	}

	@Override
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return isValidProperty;
	}

	@Override
	public boolean isValid()
	{
		return isValidProperty.get();
	}

	private class ExpandedTreeItemsLimiter implements ChangeListener<Boolean>
	{
		private final TreeItem<Labeled> variableItem;
		private final TreeItem<Labeled> holderItem;

		ExpandedTreeItemsLimiter( TreeItem<Labeled> variableItem, TreeItem<Labeled> parent )
		{
			this.variableItem = variableItem;
			this.holderItem = parent;
		}

		@Override
		public void changed( ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue )
		{
			if( newValue.booleanValue() )
			{
				for( TreeItem<Labeled> item : holderItem.getChildren() )
				{
					if( item != variableItem )
					{
						isForceCollapsing.set( true );
						item.setExpanded( false );
						isForceCollapsing.set( false );
					}
				}
			}
		}
	}
}
