package com.eviware.loadui.impl.statistics.store;

import java.util.Collection;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;

/**
 * The bundle will not build if empty, so remove this once actual classes have
 * been added.
 * 
 * @author dain.nilsson
 */
public class ExecutorManagerDummy implements ExecutionManager
{
	public Execution getCurrentExecution()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Execution startExecution( String executionId, long startTime )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Track createTrack( String trackId, Map trackStructure )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getExecutionNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Execution getExecution( String executionId )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
