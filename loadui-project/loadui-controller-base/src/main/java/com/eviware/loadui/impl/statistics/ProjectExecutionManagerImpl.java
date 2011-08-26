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
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.Addon.Context;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.api.reporting.SummaryExportUtils;
import com.eviware.loadui.api.statistics.ExecutionAddon;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.events.EventFuture;
import com.google.common.collect.ImmutableSet;

public class ProjectExecutionManagerImpl implements ProjectExecutionManager
{
	private static Logger log = LoggerFactory.getLogger( ProjectExecutionManagerImpl.class );

	private enum State
	{
		STOPPED, PROJECT_STARTED, TESTCASE_STARTED
	}

	private final ExecutionManager executionManager;
	private final WorkspaceProvider workspaceProvider;
	private final ReportingManager reportingManager;
	private final HashMap<String, HashSet<Execution>> projectIdToExecutions = new HashMap<String, HashSet<Execution>>();
	private final HashMap<ProjectItem, SummaryListener> summaryListeners = new HashMap<ProjectItem, SummaryListener>();
	private final HashSet<SummaryAttacher> summaryAttachers = new HashSet<SummaryAttacher>();
	private final CollectionListener collectionListener = new CollectionListener();
	private final RunningListener runningListener = new RunningListener();

	private State state = State.STOPPED;

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

		BeanInjector.getBean( AddonRegistry.class ).registerFactory( ExecutionAddon.class,
				new Addon.Factory<ExecutionAddon>()
				{
					@Override
					public Class<ExecutionAddon> getType()
					{
						return ExecutionAddon.class;
					}

					@Override
					public ExecutionAddon create( Context context )
					{
						return new ExecutionAddonImpl( context );
					}

					@Override
					public Set<Class<?>> getEagerTypes()
					{
						return ImmutableSet.of();
					}
				} );
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

	private class ExecutionAddonImpl implements ExecutionAddon
	{
		private final Addon.Context context;

		public ExecutionAddonImpl( Addon.Context context )
		{
			this.context = context;
		}

		@Override
		public Set<Execution> getExecutions()
		{
			return ProjectExecutionManagerImpl.this.getExecutions( ( ProjectItem )context.getOwner() );
		}

		@Override
		public Set<Execution> getExecutions( boolean includeRecent, boolean includeArchived )
		{
			return ProjectExecutionManagerImpl.this.getExecutions( ( ProjectItem )context.getOwner(), includeRecent,
					includeArchived );
		}
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

					addedProject.addEventListener( BaseEvent.class, runningListener );
					addedProject.addEventListener( CollectionEvent.class, collectionListener );
					for( SceneItem scene : addedProject.getScenes() )
						scene.addEventListener( BaseEvent.class, runningListener );

					SummaryListener summaryListener = new SummaryListener( addedProject );
					summaryListeners.put( addedProject, summaryListener );
					addedProject.addEventListener( BaseEvent.class, summaryListener );

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
				}
				else if( ProjectItem.SCENES.equals( event.getKey() ) )
				{
					SceneItem scene = ( SceneItem )event.getElement();
					scene.addEventListener( BaseEvent.class, runningListener );
					scene.addEventListener( BaseEvent.class, summaryListeners.get( event.getSource() ) );
				}
			}
			else
			{
				if( WorkspaceItem.PROJECTS.equals( event.getKey() ) )
				{
					ProjectItem removedProject = ( ProjectItem )event.getElement();

					removedProject.removeEventListener( BaseEvent.class, runningListener );
					removedProject.removeEventListener( CollectionEvent.class, collectionListener );
					removedProject.removeEventListener( BaseEvent.class, summaryListeners.remove( removedProject ) );
				}
				else if( ProjectItem.SCENES.equals( event.getKey() ) )
				{
					SceneItem scene = ( SceneItem )event.getElement();
					scene.removeEventListener( BaseEvent.class, summaryListeners.get( event.getSource() ) );
					scene.removeEventListener( BaseEvent.class, runningListener );
				}
			}
		}
	}

	private class RunningListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getSource() instanceof CanvasItem && !Boolean.getBoolean( LoadUI.DISABLE_STATISTICS ) )
			{
				// log.debug( "GOT EVENT: "+ event.getKey() + " from "
				// +event.getSource().getClass().getName() );

				CanvasItem canvas = ( CanvasItem )event.getSource();
				ProjectItem runningProject = canvas.getProject();
				if( state == State.STOPPED )
				{
					if( CanvasItem.START_ACTION.equals( event.getKey() ) )
						startExecution( canvas, runningProject );
				}
				else if( state == State.TESTCASE_STARTED )
				{
					if( CanvasItem.START_ACTION.equals( event.getKey() ) && canvas == runningProject )
					{
						summaryAttachers.add( new SummaryAttacher( runningProject, executionManager.getCurrentExecution() ) );
						state = State.PROJECT_STARTED;
					}
					else if( CanvasItem.ON_COMPLETE_DONE.equals( event.getKey() ) )
					{
						if( canvas == runningProject )
						{
							stopExecution( canvas, runningProject );
						}
						else
						{
							if( !runningProject.isRunning() )
							{
								for( SceneItem scene : runningProject.getScenes() )
									if( scene.isRunning() )
										return;

								stopExecution( canvas, runningProject );
							}
						}
					}
				}
				else if( state == State.PROJECT_STARTED )
				{
					if( CanvasItem.ON_COMPLETE_DONE.equals( event.getKey() ) && canvas == runningProject )
					{
						stopExecution( canvas, runningProject );
					}
				}
			}
		}

		private void startExecution( CanvasItem canvas, ProjectItem runningProject )
		{
			// start new execution
			long timestamp = System.currentTimeMillis();
			String projectHash = runningProject.getId();
			String executionId = projectHash + "_" + Long.toString( timestamp );

			SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
			Date now = new Date( timestamp );
			String label = dateFormatter.format( now );
			SimpleDateFormat dateFormatter2 = new SimpleDateFormat( "yyyy-MM-dd HHmmss-SSS" );
			String fileName = runningProject.getLabel() + "_" + dateFormatter2.format( now );
			Execution newExecution = executionManager.startExecution( executionId, timestamp, label, fileName );

			if( newExecution == null )
			{
				log.warn( "Could not create a new execution." );
				return;
			}

			if( canvas == runningProject )
			{
				summaryAttachers.add( new SummaryAttacher( runningProject, executionManager.getCurrentExecution() ) );

				state = State.PROJECT_STARTED;
			}
			else
			{
				state = State.TESTCASE_STARTED;
			}

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

		private void stopExecution( CanvasItem canvas, ProjectItem runningProject )
		{
			executionManager.stopExecution();

			// remove the oldest autosaved execution if needed
			Set<Execution> executions = getExecutions( runningProject, true, false );
			while( executions.size() > runningProject.getNumberOfAutosaves() && runningProject.getNumberOfAutosaves() > 0 )
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
			state = State.STOPPED;
		}
	}

	private class SummaryAttacher implements TestExecutionTask
	{
		private final ProjectItem project;
		private final Execution execution;
		private final EventFuture<BaseEvent> summaryWaiterFuture;

		public SummaryAttacher( ProjectItem project, Execution execution )
		{
			this.project = project;
			this.execution = execution;
			summaryWaiterFuture = EventFuture.forKey( project, CanvasItem.SUMMARY );
			BeanInjector.getBean( TestRunner.class ).registerTask( this, Phase.POST_STOP );
		}

		@Override
		public void invoke( TestExecution testExecution, Phase phase )
		{
			if( testExecution.getCanvas() == project )
			{
				try
				{
					summaryWaiterFuture.get();

					long totalRequests = project.getCounter( ProjectItem.SAMPLE_COUNTER ).get();
					long totalFailures = project.getCounter( ProjectItem.FAILURE_COUNTER ).get();

					execution.setAttribute( "totalRequests", String.valueOf( totalRequests ) );
					execution.setAttribute( "totalFailures", String.valueOf( totalFailures ) );

					Summary summary = project.getSummary();
					execution.setAttribute( "startTime", String.valueOf( summary.getStartTime().getTime() ) );
					execution.setAttribute( "endTime", String.valueOf( summary.getEndTime().getTime() ) );

					reportingManager.createReport( summary, execution.getSummaryReport(), "JASPER_PRINT" );
					project.fireEvent( new BaseEvent( project, ProjectItem.SUMMARY_EXPORTED ) );
				}
				catch( InterruptedException e )
				{
					log.error( "Failed waiting for summary!", e );
				}
				catch( ExecutionException e )
				{
					log.error( "Failed waiting for summary!", e );
				}

				BeanInjector.getBean( TestRunner.class ).unregisterTask( this, Phase.POST_STOP );
				summaryAttachers.remove( this );
			}
		}
	}

	private static class SummaryListener implements EventHandler<BaseEvent>
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
					Summary summary = null;
					String label = null;
					if( event.getSource() instanceof ProjectItem )
					{
						summary = project.getSummary();
						label = project.getLabel();
					}
					else
					{
						SceneItem scene = ( SceneItem )event.getSource();
						summary = scene.getSummary();
						label = project.getLabel() + "-" + scene.getLabel();
					}
					SummaryExportUtils.saveSummary( ( MutableSummary )summary, project.getReportFolder(),
							project.getReportFormat(), label );

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
