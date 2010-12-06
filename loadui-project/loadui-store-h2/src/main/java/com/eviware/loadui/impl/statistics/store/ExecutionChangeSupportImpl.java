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

import java.util.ArrayList;

import com.eviware.loadui.api.statistics.store.ExecutionChangeSupport;
import com.eviware.loadui.api.statistics.store.ExecutionListener;

public class ExecutionChangeSupportImpl implements ExecutionChangeSupport
{

	private ArrayList<ExecutionListener> startedListeners = new ArrayList<ExecutionListener>();
	private ArrayList<ExecutionListener> pausedListeners = new ArrayList<ExecutionListener>();
	private ArrayList<ExecutionListener> stopedListeners = new ArrayList<ExecutionListener>();
	
	@Override
	public void addExecutionPausedListener( ExecutionListener el )
	{
		pausedListeners.add( el );
	}

	@Override
	public void addExecutionStartListener( ExecutionListener el )
	{
		startedListeners.add( el );
	}

	@Override
	public void addExecutionStopedListener( ExecutionListener el )
	{
		stopedListeners.add( el );
	}

	@Override
	public void fireExecutionPaused()
	{
		for( ExecutionListener el : pausedListeners )
			el.executionPaused();
	}

	@Override
	public void fireExecutionStarted()
	{
		for ( ExecutionListener el : startedListeners)
			el.executionStarted();
	}

	@Override
	public void fireExecutionStoped()
	{
		for( ExecutionListener el: stopedListeners)
			el.executionStoped();
	}

	@Override
	public void removeAllExecutionListeners()
	{
		stopedListeners.clear();
		startedListeners.clear();
		pausedListeners.clear();
	}

}
