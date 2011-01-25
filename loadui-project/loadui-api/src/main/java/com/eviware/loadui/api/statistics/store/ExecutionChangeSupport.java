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
 * Support for handling execution events.
 * 
 * @author robert
 * 
 */
public interface ExecutionChangeSupport
{

	/**
	 * Add execution listener
	 * 
	 * @param el
	 */
	void addExecutionListener( ExecutionListener el );

	/**
	 * remove all listeners
	 */
	void removeAllExecutionListeners();

	/**
	 * remove particular ExecutionListener
	 * 
	 * @param el
	 */
	void removeExecutionListener( ExecutionListener el );

	/**
	 * notify all start execution listeners
	 */
	void fireExecutionStarted( State oldState );

	/**
	 * notify all paused execution listeners
	 */
	void fireExecutionPaused( State oldState );

	/**
	 * notify all stopped execution listeners.
	 */
	void fireExecutionStopped( State oldState );
	
	/**
	 * notify all that new track has been registered.
	 */
	void fireTrackRegistered( TrackDescriptor trackDescriptor );
	
	/**
	 * notify all that track has been unregistered.
	 */
	void fireTrackUnregistered( TrackDescriptor trackDescriptor );

}
