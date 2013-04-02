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
package com.eviware.loadui.api.statistics;

import java.util.Set;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.statistics.store.Execution;

/**
 * Adds Executions to ProjectItems.
 * 
 * @author dain.nilsson
 */
public interface ExecutionAddon extends Addon
{
	/**
	 * Gets all Executions for the ProjectItem.
	 * 
	 * @return
	 */
	public Set<Execution> getExecutions();

	/**
	 * Gets Executions for the ProjectItem, filtered using the given arguments.
	 * 
	 * @param includeRecent
	 * @param includeArchived
	 * @return
	 */
	public Set<Execution> getExecutions( boolean includeRecent, boolean includeArchived );
}
