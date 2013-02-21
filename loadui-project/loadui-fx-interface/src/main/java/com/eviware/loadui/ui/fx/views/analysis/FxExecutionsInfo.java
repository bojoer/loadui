package com.eviware.loadui.ui.fx.views.analysis;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.google.common.base.Preconditions;

public class FxExecutionsInfo implements ExecutionsInfo
{

	private ObservableList<Execution> recentExecutions;
	private ObservableList<Execution> archivedExecutions;
	private Property<Execution> currentExecution;
	private HBox menuParent;
	private Collection<Node> nodesToaddToMenu = new LinkedHashSet<>( 2 );
	private Deque<Callback<Data, Void>> callbacks = new ArrayDeque<>( 2 );

	private boolean isReady()
	{
		return recentExecutions != null && archivedExecutions != null && currentExecution != null && menuParent != null;
	}

	@Override
	public void addToMenu( Node node )
	{
		if( menuParent == null )
			nodesToaddToMenu.add( node );
		else
			menuParent.getChildren().add( node );
	}

	@Override
	public void runWhenReady( Callback<Data, Void> callback )
	{
		callbacks.add( callback );
		checkIfReady();
	}

	public void setRecentExecutions( ObservableList<Execution> recentExecutions )
	{
		this.recentExecutions = recentExecutions;
		checkIfReady();
	}

	public void setArchivedExecutions( ObservableList<Execution> archivedExecutions )
	{
		this.archivedExecutions = archivedExecutions;
		checkIfReady();
	}

	public void setCurrentExecution( Property<Execution> currentExecution )
	{
		this.currentExecution = currentExecution;
		checkIfReady();
	}

	public void setMenuParent( HBox menuParent )
	{
		Preconditions.checkNotNull( menuParent );
		this.menuParent = menuParent;

		if( nodesToaddToMenu != null && !nodesToaddToMenu.isEmpty() )
			menuParent.getChildren().addAll( nodesToaddToMenu );

		nodesToaddToMenu = null;
		checkIfReady();
	}

	private void checkIfReady()
	{
		if( isReady() )
		{
			while( !callbacks.isEmpty() )
			{
				callbacks.pop().call( new Data()
				{

					@Override
					public ObservableList<Execution> getRecentExecutions()
					{
						return recentExecutions;
					}

					@Override
					public ObservableList<Execution> getArchivedExecutions()
					{
						return archivedExecutions;
					}

					@Override
					public Property<Execution> getCurrentExecution()
					{
						return currentExecution;
					}

				} );
			}
		}
	}

}
