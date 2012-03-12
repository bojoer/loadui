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
package com.eviware.loadui.api.execution;

import java.util.concurrent.Future;

import com.eviware.loadui.api.model.CanvasItem;

/**
 * The execution of a load test.
 * 
 * @author dain.nilsson
 */
public interface TestExecution
{
	/**
	 * Gets the current state of the TestExecution.
	 * 
	 * @return
	 */
	public TestState getState();

	/**
	 * Gets the CanvasItem which is being run, either a ProjectItem or SceneItem.
	 * When a ProjectItem is run, its contained SceneItems may be started and
	 * stopped arbitrarily.
	 * 
	 * @return
	 */
	public CanvasItem getCanvas();

	/**
	 * Checks if the given CanvasItem is contained in the Execution, that is, it
	 * is either the current CanvasItem being executed, or it is a SceneItem
	 * belonging to the ProjectItem being executed.
	 * 
	 * @param canvas
	 * @return
	 */
	public boolean contains( CanvasItem canvas );

	/**
	 * Initiates the termination of a TestExecution (if not already in progress),
	 * returning a Future which can be used to wait for the completion to finish.
	 * If the TestExecution has already completed, this method can be used to get
	 * the ExecutionResult.
	 * 
	 * @return
	 */
	public Future<ExecutionResult> complete();

	/**
	 * Same as complete(), but also sets the aborting flag to true, indicating
	 * that queued requests and requests in progress should be discarded.
	 * 
	 * @param reason
	 *           The reason for aborting the TestExecution.
	 * @return
	 */
	public Future<ExecutionResult> abort( String reason );

	/**
	 * If set to true, the test was aborted (or currently in the process of
	 * aborting).
	 * 
	 * @return
	 */
	public boolean isAborted();

	/**
	 * Returns a message describing the reason for aborting. This method is only
	 * applicable if the TestExecution was aborted, for obvious reasons.
	 * 
	 * @return
	 */
	public String getAbortedReason();
}
