/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.execution;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.summary.Summary;

/**
 * Holds information about the end result of a TestExecution.
 * 
 * @author dain.nilsson
 */
public interface ExecutionResult
{
	/**
	 * Gets the CanvasItem which was run.
	 * 
	 * @return
	 */
	public CanvasItem getCanvas();

	/**
	 * Gets a Summary of the execution.
	 * 
	 * @return
	 */
	public Summary getSummary();

	/**
	 * Returns the total run time of the Execution in milliseconds.
	 * 
	 * @return
	 */
	public long getExecutionLength();

	/**
	 * True if the test was run in Distributed mode.
	 * 
	 * @return
	 */
	public boolean isDistributed();

	/**
	 * True if the test was aborted.
	 * 
	 * @return
	 */
	public boolean isAborted();
}
