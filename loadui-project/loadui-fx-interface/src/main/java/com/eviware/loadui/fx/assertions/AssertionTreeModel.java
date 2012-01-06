/*
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.fx.assertions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.eviware.loadui.api.charting.ChartNamePrettifier;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.fx.tree.BaseTreeNode;

public class AssertionTreeModel extends DefaultTreeModel
{
	private static final long serialVersionUID = -8572876294099664714L;

	private static final ToStringComparator nameComparator = new ToStringComparator();

	public AssertionTreeModel( StatisticHolder statisticHolder )
	{
		super( new StatisticHolderTreeNode( null, statisticHolder ) );
	}

	private static class StatisticHolderTreeNode extends BaseTreeNode
	{
		private final StatisticHolder statisticHolder;

		public StatisticHolderTreeNode( TreeNode parent, StatisticHolder statisticHolder )
		{
			super( parent );

			this.statisticHolder = statisticHolder;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( String variableName : statisticHolder.getStatisticVariableNames() )
				children.add( new StatisticVariableTreeNode( this, variableName ) );

			Collections.sort( children, nameComparator );
			return children;
		}

		@Override
		public String toString()
		{
			return statisticHolder.getLabel();
		}
	}

	private static class StatisticVariableTreeNode extends BaseTreeNode
	{
		private final String variableName;

		public StatisticVariableTreeNode( StatisticHolderTreeNode parent, String variableName )
		{
			super( parent );

			this.variableName = variableName;
		}

		@Override
		protected List<TreeNode> getChildren()
		{
			List<TreeNode> children = new ArrayList<TreeNode>();
			for( String statisticName : ( ( StatisticHolderTreeNode )getParent() ).statisticHolder.getStatisticVariable(
					variableName ).getStatisticNames() )
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
					( ( StatisticHolderTreeNode )( getParent().getParent() ) ).statisticHolder.getStatisticVariable(
							( ( StatisticVariableTreeNode )getParent() ).variableName ).getSources() );
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

	private static class SourceTreeNode extends BaseTreeNode implements AssertionTreeSelectedItemHolder
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
		public String toString()
		{
			return ChartNamePrettifier.nameForSource( sourceName );
		}

		@Override
		public boolean isSelectedByDefault()
		{
			return StatisticVariable.MAIN_SOURCE.equals( sourceName );
		}

		@Override
		public String getStatisticName()
		{
			StatisticTreeNode statisticNode = ( StatisticTreeNode )getParent();
			return statisticNode.toString();
		}

		@Override
		public String getStatisticVariableName()
		{
			StatisticTreeNode statisticNode = ( StatisticTreeNode )getParent();
			StatisticVariableTreeNode variableNode = ( StatisticVariableTreeNode )statisticNode.getParent();
			return variableNode.variableName;
		}

		@Override
		public String getSourceName()
		{
			return toString();
		}
	}

	private static class ToStringComparator implements Comparator<Object>, Serializable
	{
		private static final long serialVersionUID = 6582404199495700622L;

		@Override
		public int compare( Object o1, Object o2 )
		{
			return String.valueOf( o1 ).compareTo( String.valueOf( o2 ) );
		}
	}
}
