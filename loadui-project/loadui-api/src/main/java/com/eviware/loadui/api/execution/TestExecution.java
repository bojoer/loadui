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
	 * @return
	 */
	public Future<ExecutionResult> abort();

	/**
	 * If set to true, the test was aborted (or currently in the process of
	 * aborting).
	 * 
	 * @return
	 */
	public boolean isAborted();
}
