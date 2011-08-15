package com.eviware.loadui.api.lifecycle;

import java.util.Map;

/**
 * A task which is invoked during one or several phases of a life-cycle. A
 * life-cycle phase will not complete until all LifecycleTasks for the given
 * phase have completed. Tasks are executed in parallel.
 * 
 * @author dain.nilsson
 */
public interface LifecycleTask
{
	/**
	 * Called when the given Phase is initiated. The context given is shared
	 * between all LifecycleTasks for the entire life-cycle, and is thread safe.
	 * 
	 * @param context
	 * @param phase
	 */
	public void invoke( Map<String, Object> context, Phase phase );
}
