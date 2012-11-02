package com.eviware.loadui.ui.fx.views.assertions;

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

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.fields.Validatable;

public class AssertableTree extends TreeView<Labeled> implements Validatable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertableTree.class );

	public static BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	public static AssertableTree forHolder( StatisticHolder holder )
	{
		TreeItem<Labeled> holderItem = TreeItemBuilder.<Labeled> create().value( holder ).expanded( true ).build();
		return new AssertableTree( holder, holderItem );
	}

	AssertableTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		super( root );

		getStyleClass().add( "assertable-tree" );

		for( String variableName : holder.getStatisticVariableNames() )
		{
			StatisticVariable variable = holder.getStatisticVariable( variableName );
			final TreeItem<Labeled> variableItem = new TreeItem<Labeled>( variable );
			variableItem.expandedProperty().addListener( new ExpandedTreeItemsLimiter( variableItem, root ) );

			for( String statisticName : holder.getStatisticVariable( variableName ).getStatisticNames() )
			{
				Statistic<Number> statistic = ( Statistic<Number> )holder.getStatisticVariable( variableName )
						.getStatistic( statisticName, StatisticVariable.MAIN_SOURCE );
				variableItem.getChildren().add( new TreeItem<Labeled>( new StatisticWrapper<Number>( statistic ) ) );
			}

			if( variable instanceof ListenableValue<?> )
			{
				variableItem.getChildren().add( new TreeItem<Labeled>( new RealtimeValueWrapper( variable ) ) );
			}
			root.getChildren().add( variableItem );
		}

		getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Labeled>>()
		{
			@Override
			public void changed( ObservableValue<? extends TreeItem<Labeled>> arg0, TreeItem<Labeled> oldValue,
					TreeItem<Labeled> newValue )
			{
				if( newValue == null || newValue.getValue() == null )
				{
					log.debug( "selectedItemProperty changed to: null" );
					isValidProperty.set( false );
				}
				else
				{
					log.debug( "selectedItemProperty changed to: " + newValue.getValue().getLabel() );
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

		ExpandedTreeItemsLimiter( TreeItem<Labeled> variableItem, TreeItem<Labeled> holderItem )
		{
			this.variableItem = variableItem;
			this.holderItem = holderItem;
		}

		@Override
		public void changed( ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue )
		{
			log.debug( "ExpandedTreeItemsLimiter for " + variableItem.getValue().getLabel() + " "
					+ ( newValue.booleanValue() ? "expanded" : "not expanded" ) );
			if( newValue.booleanValue() )
			{
				for( TreeItem<Labeled> item : holderItem.getChildren() )
				{
					log.debug( "is " + item.getValue().getLabel() + " == " + variableItem.getValue().getLabel() + "? "
							+ Boolean.toString( item == variableItem ) );
					if( item != variableItem )
					{
						log.debug( "setting " + item.getValue().getLabel() + " to NOT expanded" );
						item.setExpanded( false );
						log.debug( "" + item.getValue().getLabel() + " is expanded? " + item.isExpanded() );
					}
				}
			}
		}
	}
}
