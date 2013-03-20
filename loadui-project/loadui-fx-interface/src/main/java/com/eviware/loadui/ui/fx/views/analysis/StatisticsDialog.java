package com.eviware.loadui.ui.fx.views.analysis;

import static com.google.common.base.Objects.firstNonNull;
import static javafx.beans.binding.Bindings.not;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.Statistic.Descriptor;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.ConfigurableLineChartView;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class StatisticsDialog extends ConfirmationDialog
{
	private static final Logger log = LoggerFactory.getLogger( StatisticsDialog.class );

	private final StatisticTree tree;

	public StatisticsDialog( Node parent, StatisticHolder holder, final ConfigurableLineChartView chartView )
	{
		super( parent, "Add Statistic", "Add" );
		tree = StatisticTree.forHolder( holder );

		final Collection<Chart> charts = chartView.getChartGroup().getChildren();

		if( chartView.getSegments().isEmpty() )
		{
			autoSelectDefaultStatisticsFor( holder );
		}

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				for( Selection selection : tree.getSelections() )
				{
					for( Chart chart : charts )
					{
						if( addSegment( chartView, chart, selection ) )
							break;
					}
				}
				close();
			}
		} );

		setOnCancel( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				if( !thereAreSegmentsIn( chartView.getChartGroup() ) )
					chartView.getChartGroup().delete();
				close();
			}

		} );

		confirmDisableProperty().bind( not( tree.isValidProperty() ) );

		HBox hBox = HBoxBuilder.create().children( tree ).build();
		getItems().add( hBox );
	}

	private boolean addSegment( final ConfigurableLineChartView chartView, Chart chart, Selection selection )
	{
		if( selection.holder.equals( chart.getOwner() ) )
		{
			ChartView holderChartView = chartView.getChartGroup().getChartViewForChart( chart );

			( ( ConfigurableLineChartView )holderChartView ).addSegment( selection.variable, selection.statistic,
					firstNonNull( selection.source, StatisticVariable.MAIN_SOURCE ) );
			return true;
		}
		return false;
	}

	private void autoSelectDefaultStatisticsFor( StatisticHolder holder )
	{
		for( Descriptor statistic : holder.getDefaultStatistics() )
		{
			String variableName = statistic.getStatisticVariableLabel();
			String statisticName = statistic.getStatisticLabel();
			select( variableName, statisticName );
		}
	}

	private void select( String variableName, String statisticName )
	{
		log.debug( "Trying to select variable " + variableName + ", stat name " + statisticName );
		for( TreeItem<Labeled> item : tree.getRoot().getChildren() )
		{
			if( item.getValue().getLabel().equals( variableName ) )
			{
				for( TreeItem<Labeled> child : item.getChildren() )
				{
					if( child.getValue().getLabel().equals( statisticName ) )
					{
						if( !child.getChildren().isEmpty() )
							selectDefaultChild( child.getChildren() );
						else
							selectChild( child );
						return;
					}
				}

			}
		}
	}

	private void selectChild( TreeItem<Labeled> child )
	{
		tree.getSelectionModel().select( child );
	}

	private void selectDefaultChild( ObservableList<TreeItem<Labeled>> children )
	{
		for( TreeItem<Labeled> child : children )
		{
			if( child.getValue().getLabel().equals( StatisticTree.AGENT_TOTAL ) )
			{
				selectChild( child );
				return;
			}
		}
	}

	public static boolean thereAreSegmentsIn( ChartGroup group )
	{
		for( ChartView view : group.getChartViewsForCharts() )
			if( view instanceof ConfigurableLineChartView )
				if( !( ( ConfigurableLineChartView )view ).getSegments().isEmpty() )
					return true;
		return false;
	}

}
