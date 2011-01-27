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
package com.eviware.loadui.impl.statistics.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.LoadUI;
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
		System.setProperty( LoadUI.LOADUI_HOME, "target" );

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
	public void testWriteEntry()
	{
		Map<String, Number> values = new HashMap<String, Number>();
		values.put( "a", 1 );
		values.put( "b", 2 );
		values.put( "c", 3 );
		values.put( "d", 4 );

		EntryImpl entry = new EntryImpl( ( 10 ), values );
		h2.writeEntry( track.getId(), entry, "local1", 0 );

		entry = new EntryImpl( ( 20 ), values );
		h2.writeEntry( track.getId(), entry, "local2", 0 );

		entry = new EntryImpl( ( 30 ), values );
		h2.writeEntry( track.getId(), entry, "local2", 0 );
		
		entry = new EntryImpl( ( 30 ), values );
		h2.writeEntry( track.getId(), entry, "local3", 1 );
		
		entry = new EntryImpl( ( 25 ), values );
		h2.writeEntry( track.getId(), entry, "local3", 0 );
	}

	@Test
	public void testGetNextEntry()
	{
		Entry e = track.getNextEntry( "local1", 15, 0 );
		assertNull( e );

		e = track.getNextEntry( "local1", 5, 0 );
		assertEquals( e.getTimestamp(), 10 );

		e = track.getNextEntry( "local2", 40, 0 );
		assertNull( e );

		e = track.getNextEntry( "local2", 25, 0 );
		assertEquals( e.getTimestamp(), 30 );

		e = track.getNextEntry( "local2", 15, 0 );
		assertEquals( e.getTimestamp(), 20 );

		e = track.getNextEntry( "local3", 10, 1 );
		assertEquals( e.getTimestamp(), 30 );
	}

	@Test
	public void testGetRange()
	{
		List<Entry> e = ( List<Entry> )track.getRange( "local1", 15, 20, 0 );
		assertEquals( e.size(), 0 );

		e = ( List<Entry> )track.getRange( "local1", 8, 9, 0 );
		assertEquals( e.size(), 0 );

		e = ( List<Entry> )track.getRange( "local1", 20, 0, 0 );
		assertEquals( e.size(), 0 );

		e = ( List<Entry> )track.getRange( "local1", 10, 20, 0 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local1", 5, 10, 0 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local1", 10, 10, 0 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local1", 5, 15, 0 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local2", 20, 30, 0 );
		assertEquals( e.size(), 2 );

		e = ( List<Entry> )track.getRange( "local2", 15, 35, 0 );
		assertEquals( e.size(), 2 );

		e = ( List<Entry> )track.getRange( "local2", 25, 35, 0 );
		assertEquals( e.size(), 1 );

		e = ( List<Entry> )track.getRange( "local2", 15, 25, 0 );
		assertEquals( e.size(), 1 );
		
		e = ( List<Entry> )track.getRange( "local3", 0, 50, 1 );
		assertEquals( e.size(), 1 );
	}

	@Test
	public void testDelete()
	{
		track.delete();
	}

}
