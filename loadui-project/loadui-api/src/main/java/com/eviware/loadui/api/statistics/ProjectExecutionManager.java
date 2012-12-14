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
package com.eviware.loadui.api.statistics;

import java.util.Set;

import com.eviware.loadui.api.model.ProjectItem;
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
	 * 
	 * @param project
	 * @return
	 */
	public Set<Execution> getExecutions( ProjectItem project );

	/**
	 * Get all Executions that is executions of the given project. Options
	 * whether to include Executions marked as recent, archived or both.
	 * 
	 * @param project
	 * @return
	 */
	public Set<Execution> getExecutions( ProjectItem project, boolean includeRecent, boolean includeArchived );

	/**
	 * Get the Project ID for a given Execution.
	 * 
	 * @param execution
	 * @return
	 */
	public String getProjectId( Execution execution );
}
