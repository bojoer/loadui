package com.eviware.loadui.impl.statistics.store;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public class TrackImplTest
{
	Track track;

	@Before
	public void initialize()
	{
		H2ExecutionManager h2 = new H2ExecutionManager();
		h2.clearMetaDatabase();

		ExecutionManagerImpl.getInstance().startExecution( "trackTestExecution", 10 );

		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( "a", Long.class );
		types.put( "b", Long.class );
		types.put( "c", Integer.class );
		types.put( "d", Double.class );

		TrackDescriptorImpl td = new TrackDescriptorImpl( "testTrack", types );
		ExecutionManagerImpl.getInstance().registerTrackDescriptor( td );
		track = ExecutionManagerImpl.getInstance().getTrack( "testTrack" );
	}

	@Test
	public void testWrite()
	{
		Map<String, Number> values = new HashMap<String, Number>();
		values.put( "a", 1 );
		values.put( "b", 2 );
		values.put( "c", 3 );
		values.put( "d", 4 );

		EntryImpl entry = new EntryImpl( ( int )( System.currentTimeMillis() / 10000 ), values );
		track.write( entry, "local1" );

		entry = new EntryImpl( ( int )( System.currentTimeMillis() / 10000 + 100 ), values );
		track.write( entry, "local2" );
	}

	@Test
	public void testDelete()
	{
		track.delete();
	}
}
