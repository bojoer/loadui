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
package com.eviware.loadui.impl.statistics;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.Addon.Context;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
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
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class ProjectExecutionManagerImpl implements ProjectExecutionManager, Releasable
{
	private static Logger log = LoggerFactory.getLogger( ProjectExecutionManagerImpl.class );

	private final ExecutionManager executionManager;
	private final WorkspaceProvider workspaceProvider;
	private final ReportingManager reportingManager;
	private final SetMultimap<String, Execution> projectIdToExecutions = Multimaps.newSetMultimap(
			new HashMap<String, Collection<Execution>>(), new Supplier<Set<Execution>>()
			{
				@Override
				public HashSet<Execution> get()
				{
					return Sets.newHashSet();
				}
			} );
	private final HashSet<SummaryTask> summaryAttachers = new HashSet<SummaryTask>();
	private final CollectionListener collectionListener = new CollectionListener();
	private final RunningExecutionTask runningExecutionTask = new RunningExecutionTask();

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

		BeanInjector.getBean( TestRunner.class ).registerTask( runningExecutionTask, Phase.START, Phase.POST_STOP );
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

	@Override
	public void release()
	{
		BeanInjector.getBean( TestRunner.class ).unregisterTask( runningExecutionTask, Phase.values() );
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
			if( WorkspaceItem.PROJECTS.equals( event.getKey() ) )
			{
				ProjectItem project = ( ProjectItem )event.getElement();
				String projectId = project.getId();
				if( CollectionEvent.Event.ADDED == event.getEvent() )
				{
					// lazily get project->execution mapping from disk if needed
					for( Execution e : executionManager.getExecutions() )
					{
						if( getProjectId( e ).equals( projectId ) )
						{
							projectIdToExecutions.put( projectId, e );
						}
					}
				}
				else
				{
					projectIdToExecutions.removeAll( projectId );
				}
			}
		}
	}

	private class RunningExecutionTask implements TestExecutionTask
	{
		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			if( Boolean.getBoolean( LoadUI.DISABLE_STATISTICS ) )
				return;

			CanvasItem canvas = execution.getCanvas();
			ProjectItem runningProject = canvas.getProject();

			switch( phase )
			{
			case START :
				startExecution( canvas, runningProject );
				break;
			case POST_STOP :
				stopExecution( canvas, runningProject );
				break;
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

			summaryAttachers.add( new SummaryTask( runningProject, executionManager.getCurrentExecution() ) );

			projectIdToExecutions.put( runningProject.getId(), newExecution );
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

				projectIdToExecutions.remove( runningProject.getId(), oldestExecution );
			}
		}
	}

	private class SummaryTask implements TestExecutionTask
	{
		private final ProjectItem project;
		private final Execution execution;

		public SummaryTask( ProjectItem project, Execution execution )
		{
			this.project = project;
			this.execution = execution;
			BeanInjector.getBean( TestRunner.class ).registerTask( this, Phase.POST_STOP );
		}

		@Override
		public void invoke( final @Nonnull TestExecution testExecution, final Phase phase )
		{
			CanvasItem canvas = testExecution.getCanvas();

			canvas.generateSummary();
			Summary summary = canvas.getSummary();

			long totalRequests = canvas.getCounter( CanvasItem.SAMPLE_COUNTER ).get();
			long totalFailures = canvas.getCounter( CanvasItem.FAILURE_COUNTER ).get();

			execution.setAttribute( "totalRequests", String.valueOf( totalRequests ) );
			execution.setAttribute( "totalFailures", String.valueOf( totalFailures ) );

			execution.setAttribute( "startTime", String.valueOf( summary.getStartTime().getTime() ) );
			execution.setAttribute( "endTime", String.valueOf( summary.getEndTime().getTime() ) );

			reportingManager.createReport( summary, execution.getSummaryReport(), "JASPER_PRINT" );

			if( project.isSaveReport() )
			{
				saveSummaryAsFile( canvas, summary );
			}

			BeanInjector.getBean( TestRunner.class ).unregisterTask( this, Phase.POST_STOP );
		}

		private void saveSummaryAsFile( CanvasItem canvas, Summary summary )
		{
			log.debug( "SUMMARY EXPORTED SENT FROM SummaryListener.handleEvent" );

			String label = null;
			if( canvas instanceof ProjectItem )
			{
				label = project.getLabel();
				log.debug( "project.getSummary(): {}", summary );
			}
			else
			{
				SceneItem scene = ( SceneItem )canvas;
				label = project.getLabel() + "-" + scene.getLabel();
				log.debug( "scene.getSummary(): {}", summary );
			}
			SummaryExportUtils.saveSummary( ( MutableSummary )summary, project.getReportFolder(),
					project.getReportFormat(), label );
		}
	}

}
