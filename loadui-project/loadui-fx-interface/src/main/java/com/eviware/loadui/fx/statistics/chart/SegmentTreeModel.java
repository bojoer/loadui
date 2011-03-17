/*
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx.statistics.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

public class SegmentTreeModel extends DefaultTreeModel
{
	private static final long serialVersionUID = -8572876294099664714L;

	private static final ToStringComparator nameComparator = new ToStringComparator();

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

			Collections.sort( children, nameComparator );
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

			Collections.sort( children, nameComparator );
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

			Collections.sort( children, nameComparator );
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
			ArrayList<TreeNode> children = new ArrayList<TreeNode>();
			ArrayList<String> sources = new ArrayList<String>(
					( ( ChartViewTreeNode )( getParent().getParent() ) ).lineChartView
							.getSources( ( ( StatisticVariableTreeNode )getParent() ).variableName ) );
			Collections.sort( sources, nameComparator );
			if( sources.contains( StatisticVariable.MAIN_SOURCE ) )
			{
				sources.remove( StatisticVariable.MAIN_SOURCE );
				sources.add( 0, StatisticVariable.MAIN_SOURCE );
			}

			for( String sourceName : sources )
				children.add( new SourceTreeNode( this, sourceName ) );
			return children;
		}

		@Override
		public String toString()
		{
			return ChartNamePrettifier.nameForStatistic( statisticName );
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
			return ChartNamePrettifier.nameForSource( sourceName );
		}
	}

	private static class ToStringComparator implements Comparator<Object>
	{
		@Override
		public int compare( Object o1, Object o2 )
		{
			return String.valueOf( o1 ).compareTo( String.valueOf( o2 ) );
		}
	}
}
