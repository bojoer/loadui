/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Implementation for the StatisticsManager. Also used by the
 * StatisticsHolderSupport to access the available StatisticsWriterFactories,
 * which are imported using OSGi.
 * 
 * @author dain.nilsson
 */
public class StatisticsManagerImpl implements StatisticsManager
{
	public static final Logger log = LoggerFactory.getLogger( StatisticsManagerImpl.class );

	public static final String CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";

	private static StatisticsManagerImpl instance;

	private final ExecutionManager executionManager;
	private final EventSupport eventSupport = new EventSupport();
	private Set<StatisticHolder> holders = new HashSet<StatisticHolder>();
	private Map<String, StatisticsWriterFactory> factories = new HashMap<String, StatisticsWriterFactory>();

	private final CollectionListener collectionListener = new CollectionListener();
	private final RunningListener runningListener = new RunningListener();
	private final StatisticHolderListener statisticHolderListener = new StatisticHolderListener();

	static StatisticsManagerImpl getInstance()
	{
		return instance;
	}

	public StatisticsManagerImpl( ExecutionManager executionManager, final WorkspaceProvider workspaceProvider )
	{
		instance = this;
		this.executionManager = executionManager;

		workspaceProvider.addEventListener( BaseEvent.class, new EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				if( WorkspaceProvider.WORKSPACE_LOADED.equals( event.getKey() ) )
					workspaceProvider.getWorkspace().addEventListener( CollectionEvent.class, collectionListener );
			}
		} );

		if( workspaceProvider.isWorkspaceLoaded() )
			workspaceProvider.getWorkspace().addEventListener( CollectionEvent.class, collectionListener );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void registerStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.add( statisticHolder ) )
		{
			statisticHolder.addEventListener( BaseEvent.class, statisticHolderListener );
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.ADDED, statisticHolder ) );
		}
	}

	@Override
	public void deregisterStatisticHolder( StatisticHolder statisticHolder )
	{
		if( holders.remove( statisticHolder ) )
		{
			statisticHolder.removeEventListener( BaseEvent.class, statisticHolderListener );
			fireEvent( new CollectionEvent( this, STATISTIC_HOLDERS, CollectionEvent.Event.REMOVED, statisticHolder ) );
		}
	}

	@Override
	public Collection<StatisticHolder> getStatisticHolders()
	{
		return Collections.unmodifiableSet( holders );
	}

	@Override
	public long getMinimumWriteDelay()
	{
		return 1000;
	}

	@Override
	public ExecutionManager getExecutionManager()
	{
		return executionManager;
	}

	public void registerStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.put( factory.getType(), factory );
	}

	public void unregisterStatisticsWriterFactory( StatisticsWriterFactory factory, Map<String, String> properties )
	{
		factories.remove( factory.getType() );
	}

	StatisticsWriter createStatisticsWriter( String type, StatisticVariable variable )
	{
		StatisticsWriterFactory factory = factories.get( type );
		if( factory != null )
			return factory.createStatisticsWriter( this, variable );

		return null;
	}

	private class StatisticHolderListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( StatisticHolder.STATISTICS.equals( event.getKey() ) )
			{
				fireEvent( new BaseEvent( event.getSource(), STATISTIC_HOLDER_UPDATED ) );
			}
		}
	}

	private class CollectionListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
			{
				if( WorkspaceItem.PROJECTS.equals( event.getKey() ) )
					( ( ProjectItem )event.getElement() ).addEventListener( ActionEvent.class, runningListener );
			}
		}
	}

	private class RunningListener implements EventHandler<ActionEvent>
	{
		private boolean hasCurrent = false;

		@Override
		public void handleEvent( ActionEvent event )
		{
			if( !hasCurrent && CanvasItem.START_ACTION.equals( event.getKey() ) )
			{
				hasCurrent = true;
				long timestamp = System.currentTimeMillis();
				String executionId = "execution_" + timestamp;
				executionManager.startExecution( executionId, timestamp );
				if( !( ( ProjectItem )event.getSource() ).getWorkspace().isLocalMode() )
					notifyAgents( CHANNEL, executionId, ( ( ProjectItem )event.getSource() ).getWorkspace().getAgents() );
			}
			else if( hasCurrent && CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
			{
				hasCurrent = false;
				executionManager.stopExecution();
				if( !( ( ProjectItem )event.getSource() ).getWorkspace().isLocalMode() )
				{
					String message = "stop_" + System.currentTimeMillis();
					notifyAgents( CHANNEL, message, ( ( ProjectItem )event.getSource() ).getWorkspace().getAgents() );
				}
			}
			else if( CanvasItem.STOP_ACTION.equals( event.getKey() ) )
			{
				executionManager.pauseExecution();
				if( !( ( ProjectItem )event.getSource() ).getWorkspace().isLocalMode() )
				{
					String message = "pause_" + System.currentTimeMillis();
					notifyAgents( CHANNEL, message, ( ( ProjectItem )event.getSource() ).getWorkspace().getAgents() );
				}
			}
			else if( executionManager.getState() == State.PAUSED && CanvasItem.START_ACTION.equals( event.getKey() ) )
			{
				// could this be done better?
				/*
				 * if startExecution is called and execution is in PAUSED stated it
				 * will return curectExecution and change state to START
				 */
				executionManager.startExecution( null, -1 );
				if( !( ( ProjectItem )event.getSource() ).getWorkspace().isLocalMode() )
				{
					String message = "unpause_" + System.currentTimeMillis();
					notifyAgents( CHANNEL, message, ( ( ProjectItem )event.getSource() ).getWorkspace().getAgents() );
				}
			}
		}

		private void notifyAgents( String channel, String message, Collection<AgentItem> collection )
		{
			for( AgentItem agent : collection )
				agent.sendMessage( CHANNEL, message );
		}
	}
}