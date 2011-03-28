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
package com.eviware.loadui.impl.statistics;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.api.reporting.SummaryExportUtils;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;

public class ProjectExecutionManagerImpl implements ProjectExecutionManager
{
	private final ExecutionManager executionManager;
	private final WorkspaceProvider workspaceProvider;
	private final ReportingManager reportingManager;
	private final HashMap<String, HashSet<Execution>> projectIdToExecutions = new HashMap<String, HashSet<Execution>>();
	private final HashMap<ProjectItem, SummaryListener> summaryListeners = new HashMap<ProjectItem, ProjectExecutionManagerImpl.SummaryListener>();
	private final CollectionListener collectionListener = new CollectionListener();
	private final RunningListener runningListener = new RunningListener();

	ProjectExecutionManagerImpl( final ExecutionManager executionManager, final WorkspaceProvider workspaceProvider,
			final ReportingManager reportingManager )
	{
		this.executionManager = executionManager;
		this.workspaceProvider = workspaceProvider;
		this.reportingManager = reportingManager;

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
					if( !projectIdToExecutions.containsKey( addedProject.getId() ) )
					{
						HashSet<Execution> executionSet = new HashSet<Execution>();
						for( Execution e : executionManager.getExecutions() )
						{
							if( getProjectId( e ).equals( addedProject.getId() ) )
								executionSet.add( e );
						}
						projectIdToExecutions.put( addedProject.getId(), executionSet );
					}
					addedProject.addEventListener( ActionEvent.class, runningListener );

					SummaryListener summaryListener = new SummaryListener( addedProject );
					summaryListeners.put( addedProject, summaryListener );
					addedProject.addEventListener( BaseEvent.class, summaryListener );
				}
				else if( ProjectItem.SCENES.equals( event.getKey() ) )
				{
					SceneItem scene = ( SceneItem )event.getElement();
					scene.addEventListener( BaseEvent.class, summaryListeners.get( event.getSource() ) );
				}
			}
			else
			{
				if( WorkspaceItem.PROJECTS.equals( event.getKey() ) )
				{
					ProjectItem removedProject = ( ProjectItem )event.getElement();

					removedProject.removeEventListener( ActionEvent.class, runningListener );
					removedProject.removeEventListener( BaseEvent.class, summaryListeners.remove( removedProject ) );
				}
				else if( ProjectItem.SCENES.equals( event.getKey() ) )
				{
					SceneItem scene = ( SceneItem )event.getElement();
					scene.removeEventListener( BaseEvent.class, summaryListeners.get( event.getSource() ) );
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
				long timestamp = System.currentTimeMillis();
				if( !hasCurrent && CanvasItem.START_ACTION.equals( event.getKey() ) )
				{
					// start new execution
					hasCurrent = true;
					String projectHash = runningProject.getId();
					String executionId = projectHash + "_" + Long.toString( timestamp );

					SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
					Date now = new Date( timestamp );
					String label = dateFormatter.format( now );
					SimpleDateFormat dateFormatter2 = new SimpleDateFormat( "yyyy-MM-dd HHmmss-SSS" );
					String fileName = runningProject.getLabel() + "_" + dateFormatter2.format( now );
					Execution newExecution = executionManager.startExecution( executionId, timestamp, label, fileName );

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
				}
				else if( hasCurrent && CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
				{
					hasCurrent = false;
					executionManager.stopExecution();

					runningProject.addEventListener( BaseEvent.class,
							new SummaryAttacher( executionManager.getCurrentExecution() ) );

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

						// also remove from projectIdToExecutions
						HashSet<Execution> recentProjectsExecutionMap = projectIdToExecutions.get( runningProject.getId() );
						recentProjectsExecutionMap.remove( oldestExecution );
						projectIdToExecutions.put( runningProject.getId(), recentProjectsExecutionMap );
					}
				}
				else if( CanvasItem.STOP_ACTION.equals( event.getKey() ) )
				{
					executionManager.pauseExecution();
				}
				else if( executionManager.getState() == State.PAUSED && CanvasItem.START_ACTION.equals( event.getKey() ) )
				{
					// could this be done better?
					/*
					 * if startExecution is called and execution is in PAUSED stated
					 * it will return curectExecution and change state to START
					 */
					executionManager.startExecution( null, -1 );
				}
			}
		}
	}

	private class SummaryAttacher implements EventHandler<BaseEvent>
	{
		private final Execution execution;

		public SummaryAttacher( Execution execution )
		{
			this.execution = execution;
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( CanvasItem.SUMMARY.equals( event.getKey() ) )
			{
				ProjectItem project = ( ProjectItem )event.getSource();
				project.removeEventListener( BaseEvent.class, this );

				long totalRequests = project.getCounter( ProjectItem.SAMPLE_COUNTER ).get();
				long totalFailures = project.getCounter( ProjectItem.FAILURE_COUNTER ).get();

				execution.setAttribute( "totalRequests", String.valueOf( totalRequests ) );
				execution.setAttribute( "totalFailures", String.valueOf( totalFailures ) );

				Summary summary = project.getSummary();
				reportingManager.createReport( summary, execution.getSummaryReport(), "JASPER_PRINT" );
				project.fireEvent( new BaseEvent( project, ProjectItem.SUMMARY_EXPORTED ) );
			}
		}
	}

	private class SummaryListener implements EventHandler<BaseEvent>
	{
		// once summary event was fired,source is added to this set to prevent
		// generating summary more than once (in case summary event was received
		// more than once from the same source e.g. when user stops the project by
		// clicking STOP button)
		private final HashSet<EventFirer> sources = new HashSet<EventFirer>();

		private final ProjectItem project;

		public SummaryListener( ProjectItem project )
		{
			this.project = project;
		}

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( CanvasItem.SUMMARY.equals( event.getKey() ) )
			{
				if( project.isSaveReport() && !sources.contains( event.getSource() ) )
				{
					if( event.getSource() instanceof ProjectItem )
					{
						SummaryExportUtils.saveSummary( ( MutableSummary )project.getSummary(), project.getReportFolder(),
								project.getReportFormat(), project.getLabel() );
					}
					else
					{
						SceneItem scene = ( SceneItem )event.getSource();
						SummaryExportUtils.saveSummary( ( MutableSummary )scene.getSummary(), project.getReportFolder(),
								project.getReportFormat(), project.getLabel() + "-" + scene.getLabel() );
					}
					project.fireEvent( new BaseEvent( project, ProjectItem.SUMMARY_EXPORTED ) );
				}
				sources.add( event.getSource() );
			}
			else if( CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) )
			{
				sources.remove( event.getSource() );
			}
		}
	}
}
