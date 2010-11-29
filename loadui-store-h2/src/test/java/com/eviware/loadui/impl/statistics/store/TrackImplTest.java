package com.eviware.loadui.impl.statistics.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public class TrackImplTest
{

	H2ExecutionManager h2;

	Track track;

	@Before
	public void initialize()
	{
		h2 = new H2ExecutionManager();
		h2.clearMetaDatabase();

		h2.startExecution( "trackTestExecution", 10 );

		Map<String, Class<? extends Number>> types = new HashMap<String, Class<? extends Number>>();
		types.put( "a", Long.class );
		types.put( "b", Long.class );
		types.put( "c", Integer.class );
		types.put( "d", Double.class );

		TrackDescriptorImpl td = new TrackDescriptorImpl( "testTrack", types );
		h2.registerTrackDescriptor( td );
		track = h2.getTrack( "testTrack" );
	}

	@Test
	public void testWrite()
	{
		Map<String, Number> values = new HashMap<String, Number>();
		values.put( "a", 1 );
		values.put( "b", 2 );
		values.put( "c", 3 );
		values.put( "d", 4 );

		EntryImpl entry = new EntryImpl( ( int )( 10 ), values );
		h2.writeEntry( track.getId(), entry, "local1" );

		entry = new EntryImpl( ( int )( 20 ), values );
		h2.writeEntry( track.getId(), entry, "local2" );

		entry = new EntryImpl( ( int )( 30 ), values );
		h2.writeEntry( track.getId(), entry, "local2" );
	}

	@Test
	public void testGetNextEntry()
	{
		Entry e = track.getNextEntry( "local1", 15 );
		assertNull( e );

		e = track.getNextEntry( "local1", 5 );
		assertEquals( e.getTimestamp(), 10 );

		e = track.getNextEntry( "local2", 40 );
		assertNull( e );

		e = track.getNextEntry( "local2", 25 );
		assertEquals( e.getTimestamp(), 30 );

		e = track.getNextEntry( "local2", 15 );
		assertEquals( e.getTimestamp(), 20 );
	}

	@Test
	public void testGetRange()
	{
		List<Entry> e = ( List<Entry> )track.getRange( "local1", 15, 20 );
		assertNull( e );

		e = ( List<Entry> )track.getRange( "local1", 8, 9 );
		assertNull( e );
		
		e = ( List<Entry> )track.getRange( "local1", 20, 0 );
		assertNull( e );

		e = ( List<Entry> )track.getRange( "local1", 10, 20 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local1", 5, 10 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local1", 10, 10 );
		assertEquals( e.size(), 1 );
		
		e = ( List<Entry> )track.getRange( "local1", 5, 15 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local2", 20, 30 );
		assertEquals( e.size(), 2 );
		
		e = ( List<Entry> )track.getRange( "local2", 15, 35 );
		assertEquals( e.size(), 2 );
		
		e = ( List<Entry> )track.getRange( "local2", 25, 35 );
		assertEquals( e.size(), 1 );
		
		e = ( List<Entry> )track.getRange( "local2", 15, 25 );
		assertEquals( e.size(), 1 );
	}

	@Test
	public void testDelete()
	{
		track.delete();
	}

}