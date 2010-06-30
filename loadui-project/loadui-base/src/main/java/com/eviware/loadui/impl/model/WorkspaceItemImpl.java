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
package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.discovery.RunnerDiscovery.RunnerReference;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.config.LoaduiProjectDocumentConfig;
import com.eviware.loadui.config.LoaduiWorkspaceDocumentConfig;
import com.eviware.loadui.config.ProjectReferenceConfig;
import com.eviware.loadui.config.RunnerItemConfig;
import com.eviware.loadui.config.WorkspaceItemConfig;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.impl.XmlBeansUtils;

public class WorkspaceItemImpl extends ModelItemImpl<WorkspaceItemConfig> implements WorkspaceItem
{
	public static final Logger log = LoggerFactory.getLogger( WorkspaceItemImpl.class );

	private File workspaceFile;
	private final ScheduledExecutorService executor;
	private final LoaduiWorkspaceDocumentConfig doc;
	private final Set<ProjectRefImpl> projects = new HashSet<ProjectRefImpl>();
	private final Set<RunnerItem> runners = new HashSet<RunnerItem>();
	private final ProjectListener projectListener = new ProjectListener();
	private final RunnerListener runnerListener = new RunnerListener();
	private final Property<Boolean> localMode;
	private final Property<Long> garbageCollectionInterval;

	private ScheduledFuture<?> gcTask = null;

	public static WorkspaceItemImpl loadWorkspace( File workspaceFile ) throws XmlException, IOException
	{
		WorkspaceItemImpl workspace = new WorkspaceItemImpl( workspaceFile,
				workspaceFile.exists() ? LoaduiWorkspaceDocumentConfig.Factory.parse( workspaceFile )
						: LoaduiWorkspaceDocumentConfig.Factory.newInstance() );
		workspace.init();

		return workspace;
	}

	private WorkspaceItemImpl( File workspaceFile, LoaduiWorkspaceDocumentConfig doc )
	{
		super( doc.getLoaduiWorkspace() == null ? doc.addNewLoaduiWorkspace() : doc.getLoaduiWorkspace() );

		executor = BeanInjector.getBean( ScheduledExecutorService.class );

		this.doc = doc;
		this.workspaceFile = workspaceFile;

		localMode = createProperty( LOCAL_MODE_PROPERTY, Boolean.class, false );
		createProperty( MAX_THREADS_PROPERTY, Long.class, 200 );
		createProperty( MAX_THREAD_QUEUE_PROPERTY, Long.class, 10000 );
		createProperty( IMPORT_MISSING_RUNNERS_PROPERTY, Boolean.class, false );
		createProperty( SOAPUI_PATH_PROPERTY, File.class );
		createProperty( SOAPUI_SYNC_PROPERTY, Boolean.class );
		createProperty( SOAPUI_CAJO_PORT_PROPERTY, Integer.class, 1198 );
		createProperty( LOADUI_CAJO_PORT_PROPERTY, Integer.class, 1199 );
		garbageCollectionInterval = createProperty( AUTO_GARBAGE_COLLECTION_INTERVAL, Long.class, 60 ); // using
		// seconds
	}

	@Override
	public void init()
	{
		super.init();

		for( RunnerItemConfig runnerConfig : getConfig().getRunnerArray() )
		{
			RunnerItemImpl runner = new RunnerItemImpl( this, runnerConfig );
			runner.init();
			runner.addEventListener( BaseEvent.class, runnerListener );
			runners.add( runner );
		}

		for( ProjectReferenceConfig projectRefConfig : getConfig().getProjectArray() )
		{
			try
			{
				projects.add( new ProjectRefImpl( this, projectRefConfig ) );
			}
			catch( IOException e )
			{
				log.error( "Unable to load Project: " + projectRefConfig.getLabel(), e );
			}
		}

		log.info( "Workspace '{}' loaded successfully", this );

		runGCTimer();

		addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( AUTO_GARBAGE_COLLECTION_INTERVAL.equals( event.getKey() ) )
				{
					if( gcTask != null )
					{
						gcTask.cancel( true );
						gcTask = null;
					}
					runGCTimer();
				}
			}
		} );
	}

	// run internal GCT
	private void runGCTimer()
	{
		Long interval = garbageCollectionInterval.getValue();

		if( interval != null && interval > 0 )
		{
			gcTask = executor.scheduleWithFixedDelay( new Runnable()
			{
				@Override
				public void run()
				{
					System.gc();
				}
			}, interval, interval, TimeUnit.SECONDS );
		}
	}

	@Override
	public File getWorkspaceFile()
	{
		return workspaceFile;
	}

	@Override
	public void delete()
	{
		release();
		workspaceFile.delete();
		super.delete();
	}

	@Override
	public void release()
	{
		for( ProjectRef ref : projects )
			if( ref.isEnabled() )
				ref.getProject().release();

		super.release();
	}

	@Override
	public String getLoaduiVersion()
	{
		return getConfig().getLoaduiVersion();
	}

	@Override
	public ProjectItem createProject( File projectFile, String label, boolean enabled )
	{
		try
		{
			if( projectFile.createNewFile() )
			{
				LoaduiProjectDocumentConfig projectConfig = LoaduiProjectDocumentConfig.Factory.newInstance();
				projectConfig.addNewLoaduiProject().setLabel( label );
				projectConfig.save( projectFile );
				return importProject( projectFile, enabled ).getProject();
			}
			else
				throw new IllegalArgumentException( "File already exists: " + projectFile );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Could not create project file:", e );
		}
	}

	@Override
	public ProjectRef importProject( File projectFile, boolean enabled ) throws IOException
	{
		if( !projectFile.exists() )
			throw new IllegalArgumentException( "File does not exist: " + projectFile );

		ProjectReferenceConfig projectRefConfig = getConfig().addNewProject();
		projectRefConfig.setProjectFile( projectFile.getAbsolutePath() );
		projectRefConfig.setEnabled( enabled );
		ProjectRefImpl ref;
		try
		{
			ref = new ProjectRefImpl( this, projectRefConfig );
			projects.add( ref );
			fireCollectionEvent( PROJECT_REFS, CollectionEvent.Event.ADDED, ref );
			return ref;
		}
		catch( IOException e )
		{
			getConfig().removeProject( getConfig().sizeOfProjectArray() - 1 );
			throw e;
		}
	}

	@Override
	public RunnerItem createRunner( String url, String label )
	{
		if( !url.startsWith( "http" ) )
			url = "https://" + url;
		if( !url.substring( 6 ).contains( ":" ) )
			url += ":8443";
		if( !url.endsWith( "/" ) )
			url += "/";
		RunnerItemConfig runnerConfig = getConfig().addNewRunner();
		runnerConfig.setUrl( url );
		runnerConfig.setLabel( label );
		RunnerItemImpl runner = new RunnerItemImpl( this, runnerConfig );
		runner.init();
		runner.addEventListener( BaseEvent.class, runnerListener );
		runners.add( runner );
		fireCollectionEvent( RUNNERS, CollectionEvent.Event.ADDED, runner );
		return runner;
	}

	@Override
	public RunnerItem createRunner( RunnerReference ref, String label )
	{
		RunnerItemConfig runnerConfig = getConfig().addNewRunner();
		runnerConfig.setUrl( ref.getUrl() );
		runnerConfig.setId( ref.getId() );
		runnerConfig.setLabel( label );
		RunnerItemImpl runner = new RunnerItemImpl( this, runnerConfig );
		runner.init();
		runner.addEventListener( BaseEvent.class, runnerListener );
		runners.add( runner );
		fireCollectionEvent( RUNNERS, CollectionEvent.Event.ADDED, runner );
		return runner;
	}

	@Override
	public Collection<ProjectItem> getProjects()
	{
		Collection<ProjectItem> list = new ArrayList<ProjectItem>();
		for( ProjectRef ref : projects )
			if( ref.isEnabled() )
				list.add( ref.getProject() );
		return list;
	}

	@Override
	public Collection<ProjectRef> getProjectRefs()
	{
		Collection<ProjectRef> list = new ArrayList<ProjectRef>();
		for( ProjectRef ref : projects )
			list.add( ref );
		return list;
	}

	@Override
	public Collection<RunnerItem> getRunners()
	{
		return Collections.unmodifiableSet( runners );
	}

	@Override
	public void removeProject( ProjectRef projectRef )
	{
		if( !projects.contains( projectRef ) || !( projectRef instanceof ProjectRefImpl ) )
			throw new IllegalArgumentException( "Project does not belong to this Workspace" );

		if( projectRef.isEnabled() )
		{
			ProjectItem project = projectRef.getProject();
			if( project != null )
				project.release();
		}

		projects.remove( projectRef );
		for( int i = 0; i < getConfig().sizeOfProjectArray(); i++ )
		{
			if( getConfig().getProjectArray( i ) == ( ( ProjectRefImpl )projectRef ).getConfig() )
			{
				fireCollectionEvent( PROJECT_REFS, CollectionEvent.Event.REMOVED, projectRef );
				getConfig().removeProject( i );
				return;
			}
		}
	}

	@Override
	public void removeProject( ProjectItem project )
	{
		if( project == null )
			throw new IllegalArgumentException( "Project is null" );

		for( ProjectRefImpl ref : projects )
			if( project == ref.getProject() )
			{
				removeProject( ref );
				return;
			}

		throw new IllegalArgumentException( "Project does not belong to this Workspace" );
	}

	@Override
	public void removeRunner( RunnerItem runner )
	{
		if( runner == null )
			throw new IllegalArgumentException( "Runner is null" );

		if( runners.remove( runner ) )
		{
			for( int i = 0; i < getConfig().sizeOfRunnerArray(); i++ )
			{
				if( getConfig().getRunnerArray( i ) == ( ( RunnerItemImpl )runner ).getConfig() )
				{
					fireCollectionEvent( RUNNERS, CollectionEvent.Event.REMOVED, runner );
					getConfig().removeRunner( i );
					return;
				}
			}
		}
		else
			throw new IllegalArgumentException( "Runner does not belong to this Workspace" );
	}

	public void projectLoaded( ProjectItem project )
	{
		fireCollectionEvent( PROJECTS, CollectionEvent.Event.ADDED, project );
		project.addEventListener( BaseEvent.class, projectListener );
	}

	@Override
	public void save()
	{
		try
		{
			if( !workspaceFile.exists() )
				workspaceFile.createNewFile();

			log.info( "Saving Workspace to file: '{}'", workspaceFile );
			XmlBeansUtils.saveToFile( doc, workspaceFile );
		}
		catch( IOException e )
		{
			log.error( "Error saving Workspace!", e );
		}
	}

	@Override
	public boolean isLocalMode()
	{
		return localMode.getValue();
	}

	@Override
	public void setLocalMode( boolean localMode )
	{
		if( localMode != isLocalMode() )
		{
			triggerAction( CanvasItem.COMPLETE_ACTION );
			triggerAction( CounterHolder.COUNTER_RESET_ACTION );
			this.localMode.setValue( localMode );
		}
	}

	private class ProjectListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( RELEASED ) )
				fireCollectionEvent( PROJECTS, CollectionEvent.Event.REMOVED, event.getSource() );
			else if( event.getKey().equals( DELETED ) )
			{
				removeProject( ( ProjectItem )event.getSource() );
			}
		}
	}

	private class RunnerListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( DELETED ) )
			{
				removeRunner( ( RunnerItem )event.getSource() );
			}
			else if( event instanceof PropertyEvent && RunnerItem.MAX_THREADS_PROPERTY.equals( event.getKey() ) )
			{
				( ( RunnerItem )event.getSource() ).sendMessage( RunnerItem.RUNNER_CHANNEL, Collections.singletonMap(
						RunnerItem.SET_MAX_THREADS, ( ( PropertyEvent )event ).getProperty().getStringValue() ) );
			}
		}
	}
}
