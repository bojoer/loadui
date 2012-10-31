package com.eviware.loadui.ui.fx.views.assertions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItemBuilder;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateAssertionDialog extends ConfirmationDialog
{
	protected static final Logger log = LoggerFactory.getLogger( CreateAssertionDialog.class );
	private final TreeView<Labeled> tree;

	public CreateAssertionDialog( Node owner, StatisticHolder holder )
	{
		super( owner, "Create assertion", "Create" );

		final TreeItem<Labeled> holderItem = TreeItemBuilder.<Labeled> create().value( holder ).expanded( true ).build();
		for( String variableName : holder.getStatisticVariableNames() )
		{
			StatisticVariable variable = holder.getStatisticVariable( variableName );
			final TreeItem<Labeled> variableItem = new TreeItem<Labeled>( variable );
			variableItem.expandedProperty().addListener( new ExpandedTreeItemsLimiter( variableItem, holderItem ) );

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
			holderItem.getChildren().add( variableItem );
		}

		tree = new TreeView<Labeled>( holderItem );

		tree.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Labeled>>()
		{
			@Override
			public void changed( ObservableValue<? extends TreeItem<Labeled>> arg0, TreeItem<Labeled> oldValue,
					TreeItem<Labeled> newValue )
			{
				if( newValue.getValue() instanceof AssertableWrapper<?> )
				{
					setConfirmDisable( false );
				}
				else
				{
					setConfirmDisable( true );
				}
			}
		} );

		tree.setCellFactory( new Callback<TreeView<Labeled>, TreeCell<Labeled>>()
		{
			@Override
			public TreeCell<Labeled> call( TreeView<Labeled> treeView )
			{
				return new LabeledTreeCell();
			}
		} );

		setConfirmDisable( true );
		getItems().add( tree );
	}

	public AssertableWrapper<ListenableValue<Number>> getSelectedValue()
	{
		Labeled selected = tree.getSelectionModel().getSelectedItem().getValue();
		return ( AssertableWrapper<ListenableValue<Number>> )selected;
	}
}
