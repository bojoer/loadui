/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
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
	private ReadOnlyProperty<Execution> currentExecution;
	private HBox menuParent;
	private final Deque<Callback<Data, Void>> volatileCallbacks = new ArrayDeque<>( 4 );
	private final Collection<PermanentCallback> permanentCallbacks = new ArrayList<>( 1 );

	private boolean isReady()
	{
		return recentExecutions != null && archivedExecutions != null && currentExecution != null && menuParent != null;
	}

	@Override
	public void runWhenReady( Callback<Data, Void> callback )
	{
		volatileCallbacks.push( callback );
		checkIfReady();
	}

	@Override
	public void alwaysRunWhenReady( Callback<Data, Void> callback )
	{
		permanentCallbacks.add( new PermanentCallback( callback ) );
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
		checkIfReady();
	}

	private void checkIfReady()
	{
		if( isReady() )
		{
			Data data = new DataImpl();
			while( !volatileCallbacks.isEmpty() )
			{
				volatileCallbacks.pop().call( data );
			}
			for( PermanentCallback perm : permanentCallbacks )
			{
				if( !perm.alreadyRun )
					perm.callback.call( data );
				perm.alreadyRun = true;
			}
		}
	}

	@Override
	public void reset()
	{
		recentExecutions = null;
		archivedExecutions = null;
		currentExecution = null;
		menuParent = null;
		volatileCallbacks.clear();

		for( PermanentCallback perm : permanentCallbacks )
		{
			perm.alreadyRun = false;
		}

	}

	private class DataImpl implements Data
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
		public ReadOnlyProperty<Execution> getCurrentExecution()
		{
			return currentExecution;
		}

		@Override
		public void addToMenu( Node node )
		{
			menuParent.getChildren().add( node );
		}
	}

	private class PermanentCallback
	{
		Callback<Data, Void> callback;
		boolean alreadyRun = false;

		public PermanentCallback( Callback<Data, Void> callback )
		{
			this.callback = callback;
		}
	}

}
