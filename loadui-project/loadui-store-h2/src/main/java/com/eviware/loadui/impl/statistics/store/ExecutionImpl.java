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
package com.eviware.loadui.impl.statistics.store;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.TestEventRegistry;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Factory;
import com.eviware.loadui.api.testevents.TestEventSourceDescriptor;
import com.eviware.loadui.api.testevents.TestEventTypeDescriptor;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.db.util.TypeConverter;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventData;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventEntryImpl;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventSourceConfig;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventSourceDescriptorImpl;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.testevents.UnknownTestEvent;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

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

	private final Function<TestEventData, TestEvent.Entry> createTestEvent;

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

	public ExecutionImpl( File executionDir, String id, long startTime, ExecutionManagerImpl manager,
			TestEventRegistry testEventRegistry )
	{
		this( executionDir, manager, testEventRegistry );

		attributes.put( KEY_ID, id );
		attributes.put( KEY_START_TIME, String.valueOf( startTime ) );
		storeAttributes();
	}

	public ExecutionImpl( File executionDir, ExecutionManagerImpl manager, final TestEventRegistry testEventRegistry )
	{
		createTestEvent = new Function<TestEventData, TestEvent.Entry>()
		{
			@Override
			public TestEvent.Entry apply( TestEventData input )
			{
				Factory<?> factory = testEventRegistry.lookupFactory( input.getType() );
				if( factory == null )
				{
					return new TestEventEntryImpl( new UnknownTestEvent( input.getTimestamp() ), input
							.getTestEventSourceConfig().getLabel(), "Unknown" );
				}
				else
				{
					return new TestEventEntryImpl( factory.createTestEvent( input.getTimestamp(), input
							.getTestEventSourceConfig().getData(), input.getData() ), input.getTestEventSourceConfig()
							.getLabel(), factory.getLabel() );
				}
			}
		};

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
	public Set<TestEventTypeDescriptor> getEventTypes()
	{
		return ImmutableSet.<TestEventTypeDescriptor> copyOf( manager.getTestEventTypes( getId() ) );
	}

	private Iterable<TestEventSourceConfig> getConfigsForSources( TestEventSourceDescriptor... sources )
	{
		return Iterables.concat( Iterables.transform(
				Iterables.filter( Arrays.asList( sources ), TestEventSourceDescriptorImpl.class ),
				new Function<TestEventSourceDescriptorImpl, Iterable<TestEventSourceConfig>>()
				{
					@Override
					public Iterable<TestEventSourceConfig> apply( TestEventSourceDescriptorImpl input )
					{
						return input.getConfigs();
					}
				} ) );
	}

	@Override
	public int getTestEventCount( TestEventSourceDescriptor... sources )
	{
		return manager.getTestEventCount( getId(), getConfigsForSources( sources ) );
	}

	@Override
	public Iterable<TestEvent.Entry> getTestEventRange( long startTime, long endTime,
			TestEventSourceDescriptor... sources )
	{
		return Iterables
				.transform( manager.readTestEventRange( getId(), startTime, endTime, getConfigsForSources( sources ) ),
						createTestEvent );
	}

	@Override
	public Iterable<TestEvent.Entry> getTestEvents( int index, boolean reversed, TestEventSourceDescriptor... sources )
	{
		return Iterables.transform( new TestEventBlockIterable( index, reversed, getConfigsForSources( sources ) ),
				createTestEvent );
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

	private final void loadAttributes()
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

	private class TestEventBlockIterable implements Iterable<TestEventData>
	{
		private static final int BLOCK_FETCH_SIZE = 100;

		private final Iterable<TestEventSourceConfig> configs;
		private final boolean reverse;
		private final int index;

		public TestEventBlockIterable( int index, boolean reverse, Iterable<TestEventSourceConfig> configs )
		{
			this.index = index;
			this.reverse = reverse;
			this.configs = configs;
		}

		@Override
		public Iterator<TestEventData> iterator()
		{
			return Iterators.concat( new Iterator<Iterator<TestEventData>>()
			{
				int nextIndex = index;

				@Override
				public boolean hasNext()
				{
					return reverse ? nextIndex >= 0 : nextIndex < manager.getTestEventCount( getId(), configs );
				}

				@Override
				public Iterator<TestEventData> next()
				{
					int offset = reverse ? Math.max( 0, nextIndex - BLOCK_FETCH_SIZE ) : nextIndex;
					if( reverse )
					{
						nextIndex -= BLOCK_FETCH_SIZE;
						return Lists.reverse(
								Lists.newArrayList( manager.readTestEvents( getId(), offset, BLOCK_FETCH_SIZE, configs ) ) )
								.iterator();
					}
					else
					{
						nextIndex += BLOCK_FETCH_SIZE;
						return manager.readTestEvents( getId(), offset, BLOCK_FETCH_SIZE, configs ).iterator();
					}
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			} );
		}
	}
}
