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
package com.eviware.loadui.impl.statistics.store;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DataSourceProvider;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.model.DataTable;
import com.eviware.loadui.impl.statistics.db.table.model.SequenceTable;
import com.eviware.loadui.impl.statistics.db.table.model.SourceTable;
import com.eviware.loadui.impl.statistics.db.table.model.TrackMetadataTable;
import com.eviware.loadui.impl.statistics.db.util.FileUtil;
import com.eviware.loadui.util.FormattingUtils;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.util.statistics.store.ExecutionChangeSupport;

/**
 * Implementation of execution manager. Basically main class for data handling.
 * Handles tables, database data sources etc.
 * 
 * @author predrag.vucetic
 * 
 */
public abstract class ExecutionManagerImpl implements ExecutionManager, DataSourceProvider, Releasable
{
	private static Logger log = LoggerFactory.getLogger( ExecutionManagerImpl.class );

	/**
	 * Postfix added to data table name when creating source table
	 */
	private static final String SOURCE_TABLE_NAME_POSTFIX = "_sources";

	public File baseDirectory = new File( System.getProperty( LoadUI.LOADUI_HOME ), "executions" );
	public String baseDirectoryURI = baseDirectory.toURI().toString().replaceAll( "%20", " " ) + File.separator;

	/**
	 * Current execution
	 */
	private ExecutionImpl currentExecution;

	private ExecutionChangeSupport ecs = new ExecutionChangeSupport();

	private final EventSupport eventSupport = new EventSupport();

	private Map<String, ExecutionImpl> executionMap = new HashMap<String, ExecutionImpl>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	private final Map<String, Entry> latestEntries = new HashMap<String, Entry>();

	private TableRegistry tableRegistry = new TableRegistry();

	private DatabaseMetadata metadata;

	private ConnectionRegistry connectionRegistry;

	private State executionState = State.STOPPED;

	private ExecutionPool executionPool = new ExecutionPool();

	private ResultPathListener resultPathListener = new ResultPathListener();

	public ExecutionManagerImpl()
	{
		connectionRegistry = new ConnectionRegistry( this );

		metadata = new DatabaseMetadata();
		initializeDatabaseMetadata( metadata );

		addExecutionListener( new ExecutionListenerAdapter()
		{
			@Override
			public void executionStarted( ExecutionManager.State oldState )
			{
				// if new execution was just started create track from all track
				// descriptors
				if( oldState == ExecutionManagerImpl.State.STOPPED )
				{
					for( TrackDescriptor td : trackDescriptors.values() )
					{
						createTrack( td );
					}
				}
			}

			@Override
			public void trackRegistered( TrackDescriptor trackDescriptor )
			{
				// this is in case component is added during the test
				if( executionState != State.STOPPED )
					createTrack( trackDescriptor );
			}

			@Override
			public void trackUnregistered( TrackDescriptor trackDescriptor )
			{
				// don't delete tracks
			}

		} );

		// Load the executions.
		getExecutions();
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		return startExecution( id, timestamp, "DefaultExecutionLabel" );
	}

	@Override
	public Execution startExecution( String id, long timestamp, String label )
	{
		return startExecution( id, timestamp, label, "Execution_" + String.valueOf( timestamp ) );
	}

	@Override
	public Execution startExecution( String id, long timestamp, String label, String fileName )
	{
		// unpause if paused otherwise try to create new
		if( executionState == State.PAUSED )
		{
			executionState = State.STARTED;
			ecs.fireExecutionStarted( State.PAUSED );
			log.debug( "State changed: PAUSED -> STARTED" );
			return currentExecution;
		}

		latestEntries.clear();

		if( executionMap.containsKey( id ) )
		{
			throw new IllegalArgumentException( "Execution with the specified id already exist!" );
		}

		File executionDir = new File( getDBBaseDir(), FormattingUtils.formatFileName( fileName ) );
		executionDir.mkdir();
		String dbName = executionDir.getName();

		// create sequence table
		SequenceTable sequenceTable = new SequenceTable( dbName, connectionRegistry, metadata, tableRegistry );

		// create track meta table
		TrackMetadataTable trackMetaTable = new TrackMetadataTable( dbName, connectionRegistry, metadata, tableRegistry );

		// after all SQL operations are finished successfully, add created
		// tables into registry
		tableRegistry.put( dbName, sequenceTable );
		tableRegistry.put( dbName, trackMetaTable );

		currentExecution = new ExecutionImpl( executionDir, id, timestamp, this );
		currentExecution.setLabel( label );

		executionMap.put( id, currentExecution );
		fireEvent( new CollectionEvent( this, EXECUTIONS, CollectionEvent.Event.ADDED, currentExecution ) );

		executionState = State.STARTED;
		ecs.fireExecutionStarted( State.STOPPED );
		log.debug( "State changed: STOPPED -> STARTED" );

		currentExecution.setLoaded( true );
		executionPool.setCurrentExecution( currentExecution );
		return currentExecution;
	}

	@Override
	public Execution getCurrentExecution()
	{
		return currentExecution;
	}

	@Override
	public Track getTrack( String trackId )
	{
		if( currentExecution == null )
		{
			throw new IllegalArgumentException( "There is no running execution!" );
		}
		Track track = currentExecution.getTrack( trackId );
		if( track == null )
		{
			throw new IllegalArgumentException( "No track found for specified trackId!" );
		}
		return track;
	}

	private synchronized void createTrack( TrackDescriptor td )
	{
		// synchronized to make sure that all tracks are created one by one, and
		// if one track is in process of creation to stop other threads to try to
		// create the same track. If one thread creates a track, others will have
		// to wait and when synchronized block is exited new track will already be
		// placed into currentExecution so another threads threads will know that
		// this track was created and will exit this method.
		if( currentExecution.getTrack( td.getId() ) != null )
		{
			return;
		}
		try
		{
			String dbName = currentExecution.getExecutionDir().getName();

			// create data table
			DataTable dtd = new DataTable( dbName, td.getId(), td.getValueNames(), connectionRegistry, metadata,
					tableRegistry );

			// create sources table
			SourceTable std = new SourceTable( dbName, td.getId() + SOURCE_TABLE_NAME_POSTFIX, connectionRegistry,
					metadata, tableRegistry );
			dtd.setParentTable( std );

			// insert into meta-table
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( TrackMetadataTable.STATIC_FIELD_TRACK_NAME, td.getId() );
			TableBase trackMetadataTable = tableRegistry.getTable( dbName, TrackMetadataTable.TABLE_NAME );
			trackMetadataTable.insert( data );

			// all tables are created properly and meta data inserted, so all SQL
			// operations have finished properly. Create track instance and add it
			// to current execution and put created tables into table registry
			tableRegistry.put( dbName, dtd );
			tableRegistry.put( dbName, std );

			Track track = new TrackImpl( currentExecution, td, this );
			currentExecution.addTrack( track );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to create track!", e );
		}
	}

	@Override
	public Collection<Execution> getExecutions()
	{
		File baseDir = new File( getDBBaseDir() );
		if( !baseDir.exists() )
		{
			baseDir.mkdirs();
		}

		File[] executionDirs = baseDir.listFiles( new FileFilter()
		{
			@Override
			public boolean accept( File pathname )
			{
				return pathname.isDirectory();
			}
		} );

		for( File executionDir : executionDirs )
		{
			ExecutionImpl execution = new ExecutionImpl( executionDir, this );
			if( !executionMap.containsKey( execution.getId() ) )
			{
				executionMap.put( execution.getId(), execution );
				fireEvent( new CollectionEvent( this, EXECUTIONS, CollectionEvent.Event.ADDED, execution ) );
			}
		}

		ArrayList<Execution> executions = new ArrayList<Execution>();
		executions.addAll( executionMap.values() );

		return executions;
	}

	@Override
	public ExecutionImpl getExecution( String executionId )
	{
		// If not loaded, force a re-read of the executions directory.
		if( !executionMap.containsKey( executionId ) )
			getExecutions();

		if( executionMap.containsKey( executionId ) )
		{
			return executionMap.get( executionId );
		}
		else
		{
			throw new IllegalArgumentException( "Execution with the specified id does not exist!" );
		}
	}

	/**
	 * Loads an existing execution. Creates all necessary table objects
	 * (ExecutionMetadatatable, TrackMetadataTable, SequenceTable, DataTable(s)
	 * SourceTable(s)), creates Execution objects instance, adds it to
	 * executionsMap, reads tracks from TrackMetadataTable, creates Track object
	 * instances and them to newly created execution. Tracks and tables are
	 * created just for existing tracks (registered track descriptors)
	 * 
	 * @param executionId
	 *           ID of execution that has to be loaded
	 * @return Loaded execution
	 */
	public Execution loadExecution( String executionId )
	{
		ExecutionImpl execution = executionMap.get( executionId );
		String dbName = execution.getExecutionDir().getName();
		// keep everything in temporary lists, until all SQL operations
		// are finished successfully and then create objects and add
		// tables to table registry
		List<TrackDescriptor> tracksToCreate = new ArrayList<TrackDescriptor>();
		List<TableBase> createdTableList = new ArrayList<TableBase>();
		try
		{
			// create sequence table
			SequenceTable sequenceTable = new SequenceTable( dbName, connectionRegistry, metadata, tableRegistry );
			createdTableList.add( sequenceTable );

			// create track meta data table
			TrackMetadataTable trackMetaTable = new TrackMetadataTable( dbName, connectionRegistry, metadata,
					tableRegistry );
			createdTableList.add( trackMetaTable );

			// go through all tracks in track meta table and for those that
			// have track descriptor registered, create data and sources
			// table instance. If table descriptor do not exist it means that
			// corresponding component has been deleted, so its data won't be
			// shown anyway.
			List<String> trackList = trackMetaTable.listAllTracks();
			for( int i = 0; i < trackList.size(); i++ )
			{
				String trackId = trackList.get( i );
				TrackDescriptor td = trackDescriptors.get( trackId );
				if( td != null )
				{
					// create data table
					DataTable dtd = new DataTable( dbName, td.getId(), td.getValueNames(), connectionRegistry, metadata,
							tableRegistry );
					// create sources table
					SourceTable std = new SourceTable( dbName, td.getId() + SOURCE_TABLE_NAME_POSTFIX, connectionRegistry,
							metadata, tableRegistry );
					dtd.setParentTable( std );

					createdTableList.add( dtd );
					createdTableList.add( std );
					tracksToCreate.add( td );
				}
			}

			// add created tables into table registry
			for( int i = 0; i < createdTableList.size(); i++ )
			{
				tableRegistry.put( dbName, createdTableList.get( i ) );
			}

			// create tracks and add them to execution
			for( int i = 0; i < tracksToCreate.size(); i++ )
			{
				Track track = new TrackImpl( execution, tracksToCreate.get( i ), this );
				execution.addTrack( track );
			}

			execution.setLoaded( true );
			executionPool.put( execution );
			return execution;
		}
		catch( Exception e )
		{
			throw new RuntimeException( "Execution " + executionId + " is corrupted and can't be loaded", e );
		}
		finally
		{
			createdTableList.clear();
			tracksToCreate.clear();
		}
	}

	@Override
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor )
	{
		trackDescriptors.put( trackDescriptor.getId(), trackDescriptor );
		ecs.fireTrackRegistered( trackDescriptor );
	}

	@Override
	public void unregisterTrackDescriptor( String trackId )
	{
		ecs.fireTrackUnregistered( trackDescriptors.remove( trackId ) );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		return Collections.unmodifiableSet( trackDescriptors.keySet() );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source )
	{
		writeEntry( trackId, entry, source, 0 );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel )
	{
		latestEntries.put( trackId + ":" + source + ":" + String.valueOf( interpolationLevel ), entry );

		if( currentExecution != null )
		{
			// log.debug(
			// "Trying to store entry: {} for source: {} at level: {} and trackId: {}",
			// new Object[] { entry,
			// source, interpolationLevel, trackId } );

			currentExecution.updateLength( entry.getTimestamp() );
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( DataTable.STATIC_FIELD_TIMESTAMP, entry.getTimestamp() );
			data.put( DataTable.STATIC_FIELD_INTERPOLATIONLEVEL, interpolationLevel );
			Collection<String> nameCollection = entry.getNames();
			for( Iterator<String> iterator = nameCollection.iterator(); iterator.hasNext(); )
			{
				String name = iterator.next();
				data.put( name, entry.getValue( name ) );
			}
			try
			{
				write( trackId, source, data );
			}
			catch( SQLException e )
			{
				log.error( "Unable to write data to the database:", e );
				log.error( "Unable to store entry: {} for source: {} at level: {} and trackId: {}", new Object[] { entry,
						source, interpolationLevel, trackId } );
				throw new RuntimeException( "Unable to write data to the database!", e );
			}
		}
	}

	@Override
	public Entry getLastEntry( String trackId, String source )
	{
		return getLastEntry( trackId, source, 0 );
	}

	@Override
	public Entry getLastEntry( String trackId, String source, int interpolationLevel )
	{
		return latestEntries.get( trackId + ":" + source + ":" + String.valueOf( interpolationLevel ) );
	}

	private void write( String trackId, String source, Map<String, Object> data ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( currentExecution.getExecutionDir().getName(), trackId );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.STATIC_FIELD_SOURCEID, sourceId );

		dtd.insert( data );
	}

	public Map<String, Object> readNext( String executionId, String trackId, String source, int startTime,
			int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( getExecution( executionId ).getExecutionDir().getName(), trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, System.currentTimeMillis() );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );
		data.put( DataTable.SELECT_ARG_INTERPOLATIONLEVEL_EQ, interpolationLevel );

		return dtd.selectFirst( data );
	}

	public List<Map<String, Object>> read( String executionId, String trackId, String source, int startTime,
			int endTime, int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( getExecution( executionId ).getExecutionDir().getName(), trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, endTime );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );
		data.put( DataTable.SELECT_ARG_INTERPOLATIONLEVEL_EQ, interpolationLevel );

		return dtd.select( data );
	}

	public void deleteTrack( String executionId, String trackId ) throws SQLException
	{
		String dbName = getExecution( executionId ).getExecutionDir().getName();
		TableBase table = tableRegistry.getTable( dbName, trackId );
		if( table != null )
		{
			table.drop();

			// drop source table
			table = tableRegistry.getTable( dbName, trackId + SOURCE_TABLE_NAME_POSTFIX );
			if( table != null )
			{
				table.drop();
			}

			// release resources and remove from registry
			tableRegistry.release( dbName, trackId );
		}
	}

	public void delete( String executionId )
	{
		ExecutionImpl execution = getExecution( executionId );
		if( execution == null )
			return;

		executionMap.remove( executionId );
		release( execution );
		if( execution.getExecutionDir().exists() )
		{
			FileUtil.deleteDirectory( execution.getExecutionDir() );
		}
		fireEvent( new CollectionEvent( this, EXECUTIONS, CollectionEvent.Event.REMOVED, execution ) );
	}

	public void release( String executionId )
	{
		release( executionMap.get( executionId ) );
	}

	public void release( ExecutionImpl execution )
	{
		if( execution != null )
		{
			String dbName = execution.getExecutionDir().getName();
			tableRegistry.release( dbName );
			connectionRegistry.release( dbName );
			execution.setLoaded( false );
		}

		fireEvent( new BaseEvent( this, Releasable.RELEASED ) );
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( tableRegistry, connectionRegistry, eventSupport );
		executionMap.clear();
		ecs.removeAllExecutionListeners();
	}

	/**
	 * Initialization of database meta-data. This must be implemented by concrete
	 * database execution manager implementation
	 */
	protected abstract void initializeDatabaseMetadata( DatabaseMetadata metadata );

	@Override
	public String getDBBaseDir()
	{
		return baseDirectory.getAbsolutePath();
	}

	@Override
	public void pauseExecution()
	{
		// if started and not paused (can not pause something that is not started)
		if( executionState == State.STARTED )
		{
			executionState = State.PAUSED;
			ecs.fireExecutionPaused( State.STARTED );
			log.debug( "State changed: START -> PAUSED" );
		}
	}

	@Override
	public void stopExecution()
	{
		// execution can be stopped only if started or paused previously
		if( executionState == State.STARTED || executionState == State.PAUSED )
		{
			State oldState = executionState;
			executionState = State.STOPPED;
			currentExecution.flushLength();
			latestEntries.clear();
			ecs.fireExecutionStopped( oldState );
			log.debug( "State changed: " + oldState.name() + " -> STOPPED " );
		}
	}

	@Override
	public void removeAllExecutionListeners()
	{
		ecs.removeAllExecutionListeners();
	}

	@Override
	public void addExecutionListener( ExecutionListener el )
	{
		ecs.addExecutionListener( el );
	}

	@Override
	public void removeExecutionListener( ExecutionListener el )
	{
		ecs.removeExecutionListener( el );
	}

	@Override
	public State getState()
	{
		return executionState;
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

	public void setWorkspaceProvider( WorkspaceProvider provider )
	{
		provider.addEventListener( BaseEvent.class, new WorkspaceListener() );

		if( provider.isWorkspaceLoaded() )
		{
			updateWorkspace( provider );
		}
	}

	private String convertToURI( File baseDirectory )
	{
		return baseDirectory.toURI().toString().replaceAll( "%20", " " ) + File.separator;
	}

	private void updateWorkspace( WorkspaceProvider provider )
	{
		WorkspaceItem workspace = provider.getWorkspace();
		baseDirectory = new File( provider.getWorkspace().getProperty( WorkspaceItem.STATISTIC_RESULTS_PATH )
				.getStringValue() );
		baseDirectoryURI = convertToURI( baseDirectory );
		workspace.addEventListener( PropertyEvent.class, resultPathListener );
	}

	private class WorkspaceListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( WorkspaceProvider.WORKSPACE_LOADED ) )
			{
				WorkspaceProvider provider = ( ( WorkspaceProvider )event.getSource() );
				updateWorkspace( provider );
			}
		}
	}

	private class ResultPathListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getProperty().getKey().equals( WorkspaceItem.STATISTIC_RESULTS_PATH )
					&& event.getEvent() == PropertyEvent.Event.VALUE )
			{
				baseDirectory = new File( event.getProperty().getStringValue() );
				baseDirectoryURI = convertToURI( baseDirectory );
				log.debug( "Results base directory changed to " + event.getProperty().getStringValue() );
			}
		}
	}

	private class ExecutionPool
	{
		private int maxSize = 10;

		private ArrayList<ExecutionImpl> list = new ArrayList<ExecutionImpl>();

		private ExecutionImpl currentExecution;

		public void setCurrentExecution( ExecutionImpl execution )
		{
			// put previous current execution in a pool
			if( currentExecution != null )
			{
				put( currentExecution );
			}
			// replace old current execution with the new one
			currentExecution = execution;
		}

		public void put( ExecutionImpl execution )
		{
			if( list.size() == maxSize )
			{
				release( list.remove( 0 ).getId() );
			}
			list.add( execution );
		}
	}
}
