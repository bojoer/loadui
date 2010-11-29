package com.eviware.loadui.impl.statistics.store;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public class ExecutionImplTest
{

	H2ExecutionManager h2;

	Track track;

	@Before
	public void initialize()
	{
		System.setProperty( "loadui.home", "target" );
		h2 = new H2ExecutionManager();
		h2.clearMetaDatabase();

		h2.startExecution( "executionTestExecution", 10 );

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
	public void testDelete()
	{
		h2.getCurrentExecution().delete();
	}

}
