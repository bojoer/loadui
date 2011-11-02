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
package com.eviware.loadui.api.testevents;

/**
 * Manager of TestEvents.
 * 
 * @author dain.nilsson
 */
public interface TestEventManager
{
	/**
	 * Logs a TestEvent to the current Execution, if any Execution is currently
	 * running. If no Execution is running, the TestEvent will be dropped.
	 * 
	 * @param testEvent
	 */
	public <T extends TestEvent> void logTestEvent( TestEvent.Source<T> source, T testEvent );

	/**
	 * Gets the label for the given TestEvent type.
	 * 
	 * @param type
	 * @return
	 */
	public String getLabelForType( Class<? extends TestEvent> type );

	/**
	 * Registers a TestEventObserver to be notified of each logged TestEvent.
	 * 
	 * @param observer
	 */
	public void registerObserver( TestEventObserver observer );

	/**
	 * Unregisters a TestEventObserver from being notified of each logged
	 * TestEvent.
	 * 
	 * @param observer
	 */
	public void unregisterObserver( TestEventObserver observer );

	/**
	 * Callback interface for observing logged TestEvents.
	 * 
	 * @author dain.nilsson
	 */
	public interface TestEventObserver
	{
		/**
		 * Called each time a new TestEvent is logged, with a TestEvent.Entry
		 * wrapping the new TestEvent.
		 * 
		 * @param eventEntry
		 */
		public void onTestEvent( TestEvent.Entry eventEntry );
	}
}
