package com.eviware.loadui.api.lifecycle;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Schedules life-cycles. The LifecycleScheduler may have a single life-cycle
 * running at any one time.
 * 
 * @author dain.nilsson
 */
public interface LifecycleScheduler
{
	/**
	 * Returns the current state of the LifecycleScheduler.
	 * 
	 * @return
	 */
	public State getState();

	/**
	 * Requests the start of a new life-cycle. The LifecycleScheduler must at
	 * this point be in a STOPPED state, and not have initiated the start of
	 * another life-cycle (otherwise an IllegalLifecycleStateException will be
	 * thrown). A Future will be returned, which can be used to wait for the
	 * completion of the start phases, and which will provide access to the
	 * context map of the life-cycle (initially copied from the method parameter
	 * and possibly modified by LifecycleTasks).
	 * 
	 * @param initialContext
	 * @return
	 * @throws IllegalLifecycleStateException
	 */
	public Future<Map<String, Object>> requestStart( final Map<String, Object> initialContext )
			throws IllegalLifecycleStateException;

	/**
	 * Requests the current life-cycle to end. The LifecycleScheduler must at
	 * this point have a life-cycle running, or an IllegalLifecycleStateException
	 * will be thrown. A Future will be provided to wait for the completion of
	 * the stop phases, and to provide access to the context map in its final
	 * state, after completion of the life-cycle.
	 * 
	 * @return
	 * @throws IllegalLifecycleStateException
	 */
	public Future<Map<String, Object>> requestStop() throws IllegalLifecycleStateException;

	/**
	 * Registers a LifecycleTask to be part of a Phase. All tasks registered for
	 * a Phase will be invoked in parallel during that phase, and the phase will
	 * not complete until all tasks have completed. The LifecycleScheduler will
	 * only keep weak references to the registered tasks to avoid memory leaks,
	 * but this means that callers need to keep at least one reference to the
	 * task for as long as it is used, or it may be garbage collected.
	 * 
	 * @param task
	 * @param phase
	 */
	public void registerTask( LifecycleTask task, Phase phase );

	/**
	 * Unregisters a LifecycleTask from a specific Phase.
	 * 
	 * @param task
	 * @param phase
	 */
	public void unregisterTask( LifecycleTask task, Phase phase );
}
