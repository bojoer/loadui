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
package com.eviware.loadui.api.statistics.store;

import com.eviware.loadui.api.statistics.store.ExecutionManager.State;

/**
 * Listener for execution events ( start, stop, paused ) 
 * 
 * @author robert
 *
 */

public interface ExecutionListener
{
	/**
	 * Execute this when execution start event catched.
	 */
	void executionStarted(State oldState);

	/**
	 * Execution paused, handle it.
	 */
	void executionPaused(State oldState);

	/**
	 * Execution stoped, handle it.
	 */
	void executionStoped(State oldState);
	
}
