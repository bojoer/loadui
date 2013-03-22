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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addon.Addon.Context;
import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
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
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class ProjectExecutionManagerImpl implements ProjectExecutionManager, Releasable
{
	private static Logger log = LoggerFactory.getLogger( ProjectExecutionManagerImpl.class );

	private final ExecutionManager executionManager;
	private final WorkspaceProvider workspaceProvider;
	private final ReportingManager reportingManager;
	private final Set<SummaryTask> summaryAttachers = new HashSet<>();
	private final RunningExecutionTask runningExecutionTask = new RunningExecutionTask();

	ProjectExecutionManagerImpl( final ExecutionManager executionManager, final WorkspaceProvider workspaceProvider,
			final ReportingManager reportingManager )
	{
		this.executionManager = executionManager;
		this.workspaceProvider = workspaceProvider;
		this.reportingManager = reportingManager;

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
	public Set<Execution> getExecutions( final ProjectItem project )
	{
		return Sets.newHashSet( Iterables.filter( executionManager.getExecutions(), new Predicate<Execution>()
		{

			@Override
			public boolean apply( @Nullable Execution input )
			{
				return input != null && project.getId().equals( getProjectId( input ) );
			}

		} ) );
	}

	@Override
	public Set<Execution> getExecutions( ProjectItem project, boolean includeRecent, boolean includeArchived )
	{
		HashSet<Execution> results = new HashSet<>();
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
				startExecution( runningProject );
				break;
			case POST_STOP :
				stopExecution( runningProject );
				break;
			default :
				break;
			}
		}

		private void startExecution( ProjectItem runningProject )
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

		}

		private void stopExecution( ProjectItem runningProject )
		{
			executionManager.stopExecution();
			long autoSaves = workspaceProvider.getWorkspace().getNumberOfAutosaves();

			List<Execution> executions = new ArrayList<>( getExecutions( runningProject, true, false ) );
			if( executions.size() > autoSaves )
			{
				// sorts list from oldest to newest
				Collections.sort( executions, new Comparator<Execution>()
				{
					@Override
					public int compare( Execution ex1, Execution ex2 )
					{
						return ( int )( ex1.getStartTime() - ex2.getStartTime() );
					}
				} );
				while( executions.size() > autoSaves )
				{
					Execution oldest = executions.remove( 0 );
					log.debug( "Removing oldest Execution: " + oldest.getLabel() );
					oldest.delete();
				}
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
