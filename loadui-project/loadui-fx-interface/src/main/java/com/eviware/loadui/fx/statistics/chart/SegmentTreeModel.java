package com.eviware.loadui.fx.statistics.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

public class SegmentTreeModel extends DefaultTreeModel
{
	public SegmentTreeModel( Collection<ConfigurableLineChartView> lineChartViews )
	{
		super( new ChartGroupTreeNode( null, lineChartViews ) );
	}

	public SegmentTreeModel( ConfigurableLineChartView lineChartView )
	{
		super( new ChartViewTreeNode( null, lineChartView ) );
	}

	private static abstract class BaseTreeNode implements TreeNode
	{
		private final TreeNode parent;

		public BaseTreeNode( TreeNode parent )
		{
			this.parent = parent;
		}

		protected abstract List<TreeNode> getChildren();

		@Override
		public TreeNode getChildAt( int childIndex )
		{
			return getChildren().get( childIndex );
		}

		@Override
		public int getChildCount()
		{
			return getChildren().size();
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex( TreeNode node )
		{
			return getChildren().indexOf( node );
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		public Enumeration<?> children()
		{
			return Collections.enumeration( getChildren() );
		}
	}

	private static class ChartGroupTreeNode extends BaseTreeNode
	{
		private final Collection<ConfigurableLineChartView> chartViews;

		public ChartGroupTreeNode( TreeNode parent, Collection<ConfigurableLineChartView> chartViews )
		{
			super( parent );

			this.chartViews = chartViews;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( ConfigurableLineChartView chartView : chartViews )
				children.add( new ChartViewTreeNode( this, chartView ) );

			return children;
		}

		@Override
		public String toString()
		{
			return "StatisticHolders";
		}
	}

	private static class ChartViewTreeNode extends BaseTreeNode
	{
		private final ConfigurableLineChartView lineChartView;

		public ChartViewTreeNode( TreeNode parent, ConfigurableLineChartView lineChartView )
		{
			super( parent );

			this.lineChartView = lineChartView;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( String variableName : lineChartView.getVariableNames() )
				children.add( new StatisticVariableTreeNode( this, variableName ) );

			return children;
		}

		@Override
		public String toString()
		{
			return lineChartView.toString();
		}
	}

	private static class StatisticVariableTreeNode extends BaseTreeNode
	{
		private final String variableName;

		public StatisticVariableTreeNode( ChartViewTreeNode parent, String variableName )
		{
			super( parent );

			this.variableName = variableName;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( String statisticName : ( ( ChartViewTreeNode )getParent() ).lineChartView
					.getStatisticNames( variableName ) )
				children.add( new StatisticTreeNode( this, statisticName ) );

			return children;
		}

		@Override
		public String toString()
		{
			return variableName;
		}
	}

	private static class StatisticTreeNode extends BaseTreeNode
	{
		private final String statisticName;

		public StatisticTreeNode( StatisticVariableTreeNode parent, String statisticName )
		{
			super( parent );

			this.statisticName = statisticName;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( String sourceName : ( ( ChartViewTreeNode )( getParent().getParent() ) ).lineChartView
					.getSources( ( ( StatisticVariableTreeNode )getParent() ).variableName ) )
				children.add( new SourceTreeNode( this, sourceName ) );

			return children;
		}

		@Override
		public String toString()
		{
			return statisticName;
		}
	}

	private static class SourceTreeNode extends BaseTreeNode implements Runnable
	{
		private final String sourceName;

		public SourceTreeNode( StatisticTreeNode parent, String sourceName )
		{
			super( parent );

			this.sourceName = sourceName;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean isLeaf()
		{
			return true;
		}

		@Override
		public void run()
		{
			StatisticTreeNode statisticNode = ( StatisticTreeNode )getParent();
			StatisticVariableTreeNode variableNode = ( StatisticVariableTreeNode )statisticNode.getParent();
			ChartViewTreeNode chartNode = ( ChartViewTreeNode )variableNode.getParent();

			chartNode.lineChartView.addSegment( variableNode.variableName, statisticNode.statisticName, sourceName );
		}

		@Override
		public String toString()
		{
			return sourceName;
		}
	}
}
