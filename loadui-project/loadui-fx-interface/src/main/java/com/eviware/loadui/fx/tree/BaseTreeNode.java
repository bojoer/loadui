package com.eviware.loadui.fx.tree;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

public abstract class BaseTreeNode implements TreeNode
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

	public boolean isSelectedByDefault()
	{
		return false;
	}
}
