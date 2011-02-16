package com.eviware.loadui.impl.statistics;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;

public class ProjectExecutionManagerImpl implements ProjectExecutionManager
{
	private static final String CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";
	private final ExecutionManager executionManager;
	private final WorkspaceProvider workspaceProvider;
	private final HashMap<String, HashSet<Execution>> projectIdToExecutions = new HashMap<String, HashSet<Execution>>();
	private final CollectionListener collectionListener = new CollectionListener();
	private final RunningListener runningListener = new RunningListener();

	ProjectExecutionManagerImpl( final ExecutionManager executionManager, final WorkspaceProvider workspaceProvider )
	{
		this.executionManager = executionManager;
		this.workspaceProvider = workspaceProvider;

		workspaceProvider.addEventListener( BaseEvent.class, new EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event ) // should reload projectIdToExecutions!!!!
			{
				if( WorkspaceProvider.WORKSPACE_LOADED.equals( event.getKey() ) )
					workspaceProvider.getWorkspace().addEventListener( CollectionEvent.class, collectionListener );
			}
		} );

		if( workspaceProvider.isWorkspaceLoaded() )
			workspaceProvider.getWorkspace().addEventListener( CollectionEvent.class, collectionListener );
	}

	@Override
	public Set<Execution> getExecutions( ProjectItem project )
	{
		return projectIdToExecutions.containsKey( project.getId() ) ? projectIdToExecutions.get( project.getId() )
				: Collections.<Execution> emptySet();
	}

	@Override
	public Set<Execution> getExecutions( ProjectItem project, boolean includeRecent, boolean includeArchived )
	{
		HashSet<Execution> results = new HashSet<Execution>();
		for( Execution e : getExecutions( project ) )
		{
			if( ( includeRecent && !e.isArchived() ) || ( includeArchived && e.isArchived() ) )
			{
				results.add( e );
			}
		}
		return results;
	}

	@Override
	public String getProjectId( Execution execution )
	{
		return execution.getId().split( "_" )[0];
	}

	private class CollectionListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( CollectionEvent.Event.ADDED == event.getEvent() )
			{
				if( WorkspaceItem.PROJECTS.equals( event.getKey() ) )
				{
					ProjectItem addedProject = ( ProjectItem )event.getElement();
					
					// lazily get project->execution mapping from disk if needed
					if ( !projectIdToExecutions.containsKey( addedProject.getId() ) )
					{
						HashSet<Execution> executionSet = new HashSet<Execution>();
						for( String name : executionManager.getExecutionNames() )
						{
							Execution e = executionManager.getExecution( name );
							System.out.println(getProjectId( e ) + " = " + ( addedProject.getId() ));
							if( getProjectId( e ).equals( addedProject.getId() ) )
								executionSet.add( e );
						}
						projectIdToExecutions.put( addedProject.getId(), executionSet );
					}
					addedProject.addEventListener( ActionEvent.class, runningListener );
				}
			}
		}
	}

	private class RunningListener implements EventHandler<ActionEvent>
	{
		private boolean hasCurrent = false;

		@Override
		public void handleEvent( ActionEvent event )
		{
			if( event.getSource() instanceof CanvasItem )
			{
				ProjectItem runningProject = ( ( CanvasItem )event.getSource() ).getProject();
				boolean localMode = runningProject.getWorkspace().isLocalMode();
				long timestamp = System.currentTimeMillis();
				if( !hasCurrent && CanvasItem.START_ACTION.equals( event.getKey() ) )
				{
					// start new execution
					hasCurrent = true;
					String projectHash = runningProject.getId();
					String executionId = projectHash + "_" + Long.toString( timestamp );

					SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
					String label = dateFormatter.format( new Date( timestamp ) );
					Execution newExecution = executionManager.startExecution( executionId, timestamp, label );

					// add project->execution mapping to cache
					if( projectIdToExecutions.containsKey( runningProject.getId() ) )
					{
						projectIdToExecutions.get( runningProject.getId() ).add( newExecution );
					}
					else
					{
						HashSet<Execution> executionSet = new HashSet<Execution>();
						executionSet.add( newExecution );
						projectIdToExecutions.put( runningProject.getId(), executionSet );
					}

					// notify agents if in distributed mode
					if( !localMode )
						notifyAgents( CHANNEL, executionId, runningProject.getWorkspace().getAgents() );
				}
				else if( hasCurrent && CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
				{
					hasCurrent = false;
					executionManager.stopExecution();

					// remove the oldest autosaved execution if needed
					Set<Execution> executions = getExecutions( runningProject, true, false );
					while( executions.size() > runningProject.getNumberOfAutosaves()
							&& runningProject.getNumberOfAutosaves() > 0 )
					{
						Execution oldestExecution = executionManager.getCurrentExecution();
						for( Execution e : executions )
						{
							if( e.getStartTime() < oldestExecution.getStartTime() )
								oldestExecution = e;
						}
						oldestExecution.delete();
						executions.remove( oldestExecution );
					}

					if( !localMode )
					{
						String message = "stop_" + timestamp;
						notifyAgents( CHANNEL, message, runningProject.getWorkspace().getAgents() );
					}
				}
				else if( CanvasItem.STOP_ACTION.equals( event.getKey() ) )
				{
					executionManager.pauseExecution();
					if( !localMode )
					{
						String message = "pause_" + timestamp;
						notifyAgents( CHANNEL, message, runningProject.getWorkspace().getAgents() );
					}
				}
				else if( executionManager.getState() == State.PAUSED && CanvasItem.START_ACTION.equals( event.getKey() ) )
				{
					// could this be done better?
					/*
					 * if startExecution is called and execution is in PAUSED stated
					 * it will return curectExecution and change state to START
					 */
					executionManager.startExecution( null, -1 );
					if( !localMode )
					{
						String message = "unpause_" + timestamp;
						notifyAgents( CHANNEL, message, runningProject.getWorkspace().getAgents() );
					}
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
