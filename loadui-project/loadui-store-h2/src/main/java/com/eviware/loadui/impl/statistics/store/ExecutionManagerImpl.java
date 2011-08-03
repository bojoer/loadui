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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DataSourceProvider;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.model.DataTable;
import com.eviware.loadui.impl.statistics.db.table.model.InterpolationLevelTable;
import com.eviware.loadui.impl.statistics.db.table.model.SourceMetadataTable;
import com.eviware.loadui.impl.statistics.db.table.model.TrackMetadataTable;
import com.eviware.loadui.impl.statistics.db.util.FileUtil;
import com.eviware.loadui.util.FormattingUtils;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.util.statistics.store.ExecutionChangeSupport;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

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

	private static final Map<String, String> columnNames = new MapMaker().weakKeys().makeComputingMap(
			new Function<String, String>()
			{
				@Override
				public String apply( String name )
				{
					//Replaces whitespace characters with underscore, removes all non-word characters, and places a C in front of any column name starting with a digit. 
					return name.replaceAll( "\\s", "_" ).replaceAll( "[^\\w]", "" ).replaceAll( "^(\\d)", "C$0" )
							.toUpperCase();
				}
			} );

	private static ExecutionManagerImpl instance;

	private WorkspaceItem workspace = null;

	public File baseDirectory = new File( System.getProperty( LoadUI.LOADUI_HOME ), "results" );
	public String baseDirectoryURI = baseDirectory.toURI().toString().replaceAll( "%20", " " ) + File.separator;

	/**
	 * Current execution
	 */
	private ExecutionImpl currentExecution;

	private final ExecutionChangeSupport ecs = new ExecutionChangeSupport();

	private final EventSupport eventSupport = new EventSupport();

	private final Map<String, ExecutionImpl> executionMap = new HashMap<String, ExecutionImpl>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	private final Map<String, Entry> latestEntries = new HashMap<String, Entry>();

	private final TableRegistry tableRegistry = new TableRegistry();

	private final DatabaseMetadata metadata;

	private final ConnectionRegistry connectionRegistry;

	private State executionState = State.STOPPED;

	private final ExecutionPool executionPool = new ExecutionPool();

	private final ResultPathListener resultPathListener = new ResultPathListener();

	private long pauseStartedTime = 0;

	private long totalPause = 0;

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
				log.debug( " ExecutionManagerImpl:executionStarted()" );
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
				// This is in case component is added during the test.
				if( executionState != State.STOPPED )
					createTrack( trackDescriptor );
			}

		} );

		// Load the executions.
		getExecutions();

		setInstance( this );
	}

	private static void setInstance( ExecutionManagerImpl instance )
	{
		ExecutionManagerImpl.instance = instance;
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
			totalPause += System.currentTimeMillis() - pauseStartedTime;
			ecs.fireExecutionStarted( State.PAUSED );
			log.debug( "State changed: PAUSED -> STARTED" );
			return currentExecution;
		}

		latestEntries.clear();

		if( executionMap.containsKey( id ) )
		{
			throw new IllegalArgumentException( "Execution with the specified id already exist!" );
		}

		File executionDir = null;
		SourceMetadataTable sourceMetaTable = null;
		InterpolationLevelTable levelMetaTable = null;
		TrackMetadataTable trackMetaTable = null;
		String dbName;
		try
		{
			executionDir = new File( getDBBaseDir(), FormattingUtils.formatFileName( fileName ) );
			if( !executionDir.mkdir() )
			{
				log.error( "Unable to start Execution, couldn't create directory: {}", executionDir );
				return null;
			}
			dbName = executionDir.getName();

			// create source meta table
			sourceMetaTable = new SourceMetadataTable( dbName, connectionRegistry, metadata, tableRegistry );

			// create interpolation level meta table
			levelMetaTable = new InterpolationLevelTable( dbName, connectionRegistry, metadata, tableRegistry );

			// create track meta table
			trackMetaTable = new TrackMetadataTable( dbName, connectionRegistry, metadata, tableRegistry );
		}
		catch( SQLException e )
		{
			log.debug( "    " + e.getMessage() );
			if( e.getMessage().contains( "not enough space" ) )
				signalLowDiskspace();
			else
				signalDiskProblem();
			return null;
		}

		// after all SQL operations are finished successfully, add created
		// tables into registry
		tableRegistry.put( dbName, sourceMetaTable );
		tableRegistry.put( dbName, levelMetaTable );
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

		totalPause = 0;
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

	static void signalDiskProblem()
	{
		signalDiskProblem( "genericDiskProblem" );
	}

	static void signalDiskProblem( String msg )
	{
		log.warn( "Stopping execution since loadUI was unable to record statistics to disk. Please make sure that there's enough free disk space." );
		instance.stopExecution();
		if( instance.workspace != null )
		{
			log.debug( "fff: " + msg );
			instance.workspace.getProjects().iterator().next().triggerAction( CanvasItem.COMPLETE_ACTION );
			instance.workspace.fireEvent( new BaseEvent( instance, msg ) );
		}
	}

	static void signalLowDiskspace()
	{
		log.warn( "Stopping execution due to critically low diskspace." );
		signalDiskProblem( "lowDiskspace" );
	}

	private synchronized void createTrackSourceLevelTable( String dbName, String trackId, int interpolationLevel,
			String source )
	{
		if( tableRegistry.getTable( dbName, buildDataTableName( trackId, interpolationLevel, source ) ) != null )
		{
			return;
		}
		try
		{
			// create data table
			TrackDescriptor td = trackDescriptors.get( trackId );
			ImmutableMap.Builder<String, Class<? extends Number>> builder = ImmutableMap.builder();
			for( java.util.Map.Entry<String, Class<? extends Number>> entry : td.getValueNames().entrySet() )
			{
				builder.put( columnNames.get( entry.getKey() ), entry.getValue() );
			}

			DataTable dtd = new DataTable( dbName, buildDataTableName( td.getId(), interpolationLevel, source ),
					builder.build(), connectionRegistry, metadata, tableRegistry );

			SourceMetadataTable sources = ( SourceMetadataTable )tableRegistry.getTable( dbName,
					SourceMetadataTable.SOURCE_TABLE_NAME );
			if( !sources.getInMemoryTable().contains( source ) )
			{
				Map<String, Object> data = new HashMap<String, Object>();
				data.put( SourceMetadataTable.STATIC_FIELD_SOURCE_NAME, source );
				sources.insert( data );
			}

			InterpolationLevelTable levels = ( InterpolationLevelTable )tableRegistry.getTable( dbName,
					InterpolationLevelTable.INTERPOLATION_LEVEL_TABLE_NAME );
			if( !levels.getInMemoryTable().contains( interpolationLevel ) )
			{
				Map<String, Object> data = new HashMap<String, Object>();
				data.put( InterpolationLevelTable.STATIC_FIELD_INTERPOLATION_LEVEL, interpolationLevel );
				levels.insert( data );
			}

			// all SQL operations were successful so add created table into table
			// registry
			tableRegistry.put( dbName, dtd );
		}
		catch( SQLException e )
		{
			log.error( "Exception thrown when attempting to create table for {}", trackDescriptors.get( trackId ) );
			throw new RuntimeException( "Unable to create table for source!", e );
		}
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

			// insert into meta-table
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( TrackMetadataTable.STATIC_FIELD_TRACK_NAME, td.getId() );
			TableBase trackMetadataTable = tableRegistry.getTable( dbName, TrackMetadataTable.TABLE_NAME );
			trackMetadataTable.insert( data );

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
			if( !baseDir.mkdirs() )
			{
				log.error( "Unable to create directory for executions: {}", baseDir );
				return Collections.emptyList();
			}
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
			// create source meta table
			SourceMetadataTable sourceMetaTable = new SourceMetadataTable( dbName, connectionRegistry, metadata,
					tableRegistry );
			createdTableList.add( sourceMetaTable );

			// create level table
			InterpolationLevelTable levelTable = new InterpolationLevelTable( dbName, connectionRegistry, metadata,
					tableRegistry );
			createdTableList.add( levelTable );

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
					// create data tables
					for( Integer level : levelTable.getLevels() )
					{
						for( String source : sourceMetaTable.getSourceNames() )
						{
							DataTable dtd = new DataTable( dbName, buildDataTableName( td.getId(), level, source ),
									td.getValueNames(), connectionRegistry, metadata, tableRegistry );
							createdTableList.add( dtd );
						}
					}
					tracksToCreate.add( td );
				}
			}

			// add created tables into table registry
			tableRegistry.putAll( dbName, createdTableList );

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
		catch( SQLException e )
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
	public void writeEntry( final String trackId, Entry entry, String source, int interpolationLevel )
	{
		if( currentExecution != null )
		{
			// log.debug(
			// "Trying to store entry: {} for source: {} at level: {} and trackId: {}",
			// new Object[] { entry,
			// source, interpolationLevel, trackId } );

			// Adjust timestamp:
			long timestamp = entry.getTimestamp();
			if( executionState == State.PAUSED )
			{
				// quick fix since we are going to remove pause functionality. for
				// some reason when project finishes execution it goes through PAUSE
				// state. All timestamps greater than pauseStartedTime are set to
				// pauseStartedTime which causes primary key exception.
				// timestamp = Math.min( timestamp, pauseStartedTime );
			}
			timestamp -= ( currentExecution.getStartTime() + totalPause );

			latestEntries.put( trackId + ":" + source + ":" + String.valueOf( interpolationLevel ), new AdjustedEntry(
					entry, timestamp ) );

			currentExecution.updateLength( timestamp );
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( DataTable.STATIC_FIELD_TIMESTAMP, timestamp );

			for( String name : getTrack( trackId ).getTrackDescriptor().getValueNames().keySet() )
			{
				data.put( columnNames.get( name ), entry.getValue( name ) );
			}
			try
			{
				write( trackId, source, interpolationLevel, data );
			}
			catch( SQLException e )
			{
				log.error( "Unable to write data to the database:", e );
				log.error( "Unable to store entry: {} for source: {} at level: {} and trackId: {}", new Object[] { entry,
						source, interpolationLevel, trackId } );
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

	private String buildDataTableName( String trackId, long interpolationLevel, String source )
	{
		source = source.replaceAll( "[^A-Za-z0-9]", "" );
		return trackId + "_" + interpolationLevel + "_" + source;
	}

	private void write( String trackId, String source, int interpolationLevel, Map<String, Object> data )
			throws SQLException
	{
		if( executionState == State.STOPPED )
		{
			// This will occur from time to time because sometimes a
			// StatisticHolder will get the START_ACTION event and trigger
			// StatisticWriter (which will write it's starting values) before the
			// ProjectExecutionManager have started a new Execution. One way to get
			// around this would be to allow addEventListener(... , ... , priority
			// ).
			log.debug( "Write request to STOPPED execution ignored." );
			return;
		}

		// check if source is registered, and register it and create relevant
		// tables if necessary
		String dbName = currentExecution.getExecutionDir().getName();
		createTrackSourceLevelTable( dbName, trackId, interpolationLevel, source );

		TableBase dtd = tableRegistry.getTable( currentExecution.getExecutionDir().getName(),
				buildDataTableName( trackId, interpolationLevel, source ) );

		dtd.insert( data );
	}

	public Map<String, Object> readNext( String executionId, String trackId, String source, long startTime,
			int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( getExecution( executionId ).getExecutionDir().getName(),
				buildDataTableName( trackId, interpolationLevel, source ) );

		Map<String, Object> data = new HashMap<String, Object>();
		if( dtd == null )
		{
			// if table does not exist return empty set
			return data;
		}

		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, System.currentTimeMillis() );

		HashMap<String, Object> cleanedData = Maps.newHashMap();
		Map<String, Object> rawData = dtd.selectFirst( data );
		if( !rawData.isEmpty() )
		{
			for( String name : getTrack( trackId ).getTrackDescriptor().getValueNames().keySet() )
			{
				cleanedData.put( name, rawData.remove( columnNames.get( name ) ) );
			}
			cleanedData.putAll( rawData );
		}

		return cleanedData;
	}

	public Iterable<Map<String, Object>> read( String executionId, String trackId, String source, long startTime,
			long endTime, int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( getExecution( executionId ).getExecutionDir().getName(),
				buildDataTableName( trackId, interpolationLevel, source ) );

		if( dtd == null )
		{
			// if table does not exist return empty set
			return new ArrayList<Map<String, Object>>();
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, endTime );

		final Set<String> descriptorNames = getTrack( trackId ).getTrackDescriptor().getValueNames().keySet();

		return Iterables.transform( dtd.select( data ), new Function<Map<String, Object>, Map<String, Object>>()
		{
			@Override
			public Map<String, Object> apply( Map<String, Object> rawData )
			{
				HashMap<String, Object> data = Maps.newHashMap();
				for( String name : descriptorNames )
				{
					data.put( name, rawData.remove( columnNames.get( name ) ) );
				}
				data.putAll( rawData );

				return data;
			}
		} );
	}

	public void deleteTrack( String executionId, String trackId ) throws SQLException
	{
		String dbName = getExecution( executionId ).getExecutionDir().getName();
		SourceMetadataTable sources = ( SourceMetadataTable )tableRegistry.getTable( dbName,
				SourceMetadataTable.SOURCE_TABLE_NAME );
		InterpolationLevelTable levels = ( InterpolationLevelTable )tableRegistry.getTable( dbName,
				InterpolationLevelTable.INTERPOLATION_LEVEL_TABLE_NAME );

		TableBase table;
		for( Integer level : levels.getLevels() )
		{
			for( String s : sources.getSourceNames() )
			{
				table = tableRegistry.getTable( dbName, buildDataTableName( trackId, level, s ) );
				if( table != null )
				{
					table.drop();
					// release resources and remove from registry
					tableRegistry.release( dbName, buildDataTableName( trackId, level, s ) );
				}
			}
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
			pauseStartedTime = System.currentTimeMillis();
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
		workspace = provider.getWorkspace();
		baseDirectory = new File( provider.getWorkspace().getProperty( WorkspaceItem.STATISTIC_RESULTS_PATH )
				.getStringValue() );
		baseDirectoryURI = convertToURI( baseDirectory );
		workspace.addEventListener( PropertyEvent.class, resultPathListener );
	}

	private static class AdjustedEntry implements Entry
	{
		private final Entry delegate;
		private final long timestamp;

		public AdjustedEntry( Entry delegate, long timestamp )
		{
			this.delegate = delegate;
			this.timestamp = timestamp;
		}

		@Override
		public long getTimestamp()
		{
			return timestamp;
		}

		@Override
		public Set<String> getNames()
		{
			return delegate.getNames();
		}

		@Override
		public Number getValue( String name )
		{
			return delegate.getValue( name );
		}

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
		private static final int maxSize = 10;

		private final ArrayList<ExecutionImpl> list = new ArrayList<ExecutionImpl>();

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
