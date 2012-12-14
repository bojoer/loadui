package com.eviware.loadui.impl.statistics.store;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.serialization.ListenableValue.ValueListener;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.api.testevents.TestEvent.Source;

public class DummyExecutionManager implements ExecutionManager
{
	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
	}

	@Override
	public void clearEventListeners()
	{
	}

	@Override
	public void fireEvent( EventObject event )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution getCurrentExecution()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution startExecution( String executionId, long startTime, String label, String fileName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution startExecution( String executionId, long startTime, String label )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution startExecution( String executionId, long startTime )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopExecution()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void unregisterTrackDescriptor( String trackId )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Track getTrack( String trackId )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getTrackIds()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entry getLastEntry( String trackId, String source )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entry getLastEntry( String trackId, String source, int interpolationLevel )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addEntryListener( String trackId, String source, int interpolationLevel,
			ValueListener<? super Entry> listener )
	{
	}

	@Override
	public void removeEntryListener( String trackId, String source, int interpolationLevel,
			ValueListener<? super Entry> listener )
	{
	}

	@Override
	public void writeTestEvent( String typeLabel, Source<?> source, long timestamp, byte[] testEventData,
			int interpolationLevel )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Execution> getExecutions()
	{
		return Collections.emptySet();
	}

	@Override
	public Execution getExecution( String executionId )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDBBaseDir()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addExecutionListener( ExecutionListener el )
	{
	}

	@Override
	public void removeAllExecutionListeners()
	{
	}

	@Override
	public void removeExecutionListener( ExecutionListener el )
	{
	}

	@Override
	public State getState()
	{
		throw new UnsupportedOperationException();
	}
}
