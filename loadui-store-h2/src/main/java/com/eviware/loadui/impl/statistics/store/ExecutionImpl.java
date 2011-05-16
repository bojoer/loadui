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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.impl.statistics.db.util.TypeConverter;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Execution implementation
 * 
 * @author predrag.vucetic
 */
public class ExecutionImpl implements Execution, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( ExecutionImpl.class );

	public static final String KEY_ID = "ID";
	public static final String KEY_START_TIME = "START_TIME";
	public static final String KEY_ARCHIVED = "ARCHIVED";
	public static final String KEY_LABEL = "LABEL";
	public static final String KEY_LENGTH = "LENGTH";
	public static final String KEY_ICON = "ICON";

	/**
	 * Execution directory
	 */
	private final File executionDir;

	/**
	 * Reference to execution manager implementation
	 */
	private final ExecutionManagerImpl manager;

	private final EventSupport eventSupport = new EventSupport();

	private final Properties attributes = new Properties();
	private final File propertiesFile;

	private final Object loadingLock = new Object();
	private boolean isLoading = false;

	/**
	 * Map that holds references to all tracks that belongs to this execution
	 */
	private final Map<String, Track> trackMap;

	/**
	 * Execution length
	 */
	private long length = 0;

	private long lastFlushedLength = 0;

	private boolean loaded = false;

	private Image icon;

	public ExecutionImpl( File executionDir, String id, long startTime, ExecutionManagerImpl manager )
	{
		this.executionDir = executionDir;
		this.manager = manager;
		trackMap = new HashMap<String, Track>();

		propertiesFile = new File( executionDir, "execution.properties" );

		if( propertiesFile.exists() )
			loadAttributes();

		attributes.put( KEY_ID, id );
		attributes.put( KEY_START_TIME, String.valueOf( startTime ) );
		storeAttributes();
	}

	public ExecutionImpl( File executionDir, ExecutionManagerImpl manager )
	{
		this.executionDir = executionDir;
		this.manager = manager;
		trackMap = new HashMap<String, Track>();

		propertiesFile = new File( executionDir, "execution.properties" );

		if( propertiesFile.exists() )
			loadAttributes();
	}

	@Override
	public String getId()
	{
		return getAttribute( KEY_ID, "" );
	}

	@Override
	public long getStartTime()
	{
		return Long.parseLong( getAttribute( KEY_START_TIME, "0" ) );
	}

	private void awaitLoaded()
	{
		if( !isLoaded() )
		{
			synchronized( loadingLock )
			{
				if( isLoading )
				{
					try
					{
						loadingLock.wait();
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					isLoading = true;
					manager.loadExecution( getId() );
				}
			}
		}
	}

	@Override
	public Track getTrack( String trackId )
	{
		awaitLoaded();
		return trackMap.get( trackId );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		awaitLoaded();
		return trackMap.keySet();
	}

	@Override
	public void delete()
	{
		manager.delete( getId() );
		fireEvent( new BaseEvent( this, DELETED ) );
	}

	/**
	 * Adds track to track map after it was created in execution manager
	 * 
	 * @param track
	 *           Track to add to track map
	 */
	public void addTrack( Track track )
	{
		trackMap.put( track.getId(), track );
	}

	@Override
	public boolean isArchived()
	{
		return Boolean.valueOf( getAttribute( KEY_ARCHIVED, "false" ) );
	}

	@Override
	public void archive()
	{
		if( !isArchived() )
		{
			setAttribute( KEY_ARCHIVED, Boolean.TRUE.toString() );
			fireEvent( new BaseEvent( this, ARCHIVED ) );
		}
	}

	@Override
	public String getLabel()
	{
		return getAttribute( KEY_LABEL, "<label missing>" );
	}

	@Override
	public void setLabel( String label )
	{
		setAttribute( KEY_LABEL, label );
		fireEvent( new BaseEvent( this, LABEL ) );
	}

	@Override
	public long getLength()
	{
		return length;
	}

	void updateLength( long timestamp )
	{
		length = Math.max( length, timestamp );
		if( length > lastFlushedLength + 5000 )
			flushLength();
	}

	void flushLength()
	{
		lastFlushedLength = length;
		setAttribute( KEY_LENGTH, String.valueOf( length ) );
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
	public File getSummaryReport()
	{
		return new File( executionDir, "summary.jp" );
	}

	@Override
	public void release()
	{
		manager.release( getId() );
	}

	public void setLoaded( boolean loaded )
	{
		synchronized( loadingLock )
		{
			this.loaded = loaded;
			if( isLoading )
			{
				isLoading = false;
				loadingLock.notifyAll();
			}
		}
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	@Override
	public Image getIcon()
	{
		return icon;
	}

	@Override
	public void setIcon( Image icon )
	{
		setAttribute( KEY_ICON, TypeConverter.objectToString( icon ) );
		this.icon = icon;
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributes.setProperty( key, value );
		storeAttributes();
	}

	public File getExecutionDir()
	{
		return executionDir;
	}

	private void loadAttributes()
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream( propertiesFile );
			attributes.load( fis );
			length = Long.parseLong( attributes.getProperty( KEY_LENGTH, "0" ) );
			icon = ( BufferedImage )TypeConverter.stringToObject( getAttribute( KEY_ICON, null ), BufferedImage.class );
		}
		catch( FileNotFoundException e )
		{
			log.error( "Could not load execution properties file!", e );
		}
		catch( IOException e )
		{
			log.error( "Could not load execution properties file!", e );
		}
		finally
		{
			try
			{
				if( fis != null )
					fis.close();
			}
			catch( IOException e )
			{
			}
		}
	}

	private void storeAttributes()
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream( propertiesFile );
			attributes.store( fos, "" );
		}
		catch( FileNotFoundException e )
		{
			log.error( "Could not store execution properties file!", e );
		}
		catch( IOException e )
		{
			log.error( "Could not store execution properties file!", e );
		}
		finally
		{
			try
			{
				if( fos != null )
					fos.close();
			}
			catch( IOException e )
			{
			}
		}
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributes.getProperty( key, defaultValue );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributes.remove( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributes.stringPropertyNames();
	}
}
