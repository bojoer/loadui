package com.eviware.loadui.api.statistics;

import java.util.Set;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.statistics.store.Execution;

/**
 * 
 * Responsible for mapping Executions to ProjectItems. 
 * 
 * @author henrik.olsson
 *
 */

public interface ProjectExecutionManager
{
	/**
	 * Get all Executions that is executions of the given project.
	 * @param project
	 * @return
	 */
	public Set<Execution> getExecutions( ProjectItem project );
	
	/**
	 * Get all Executions that is executions of the given project.
	 * Options whether to include Executions marked as recent, archived or both.
	 * @param project
	 * @return
	 */
	public Set<Execution> getExecutions( ProjectItem project, boolean includeRecent, boolean includeArchived );
	
	/**
	 * Get the ProjectRef for a given Execution.
	 * @param execution
	 * @return
	 */
	public ProjectRef getProjectRef( Execution execution );
}
