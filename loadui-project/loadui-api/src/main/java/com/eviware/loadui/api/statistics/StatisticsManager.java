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

import java.util.Collection;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.statistics.store.ExecutionManager;

/**
 * A registry of available Statistics and StatisticHolders, and the settings
 * they use. Fires a CollectionEvent when StatisticHolders are added or removed.
 * 
 * @author dain.nilsson
 */
public interface StatisticsManager extends EventFirer
{
	public static final String STATISTIC_HOLDERS = StatisticsManager.class.getName() + "@statisticHolders";
	public static final String STATISTIC_HOLDER_UPDATED = StatisticsManager.class.getName() + "@statisticHolderUpdated";

	/**
	 * Registers a StatisticHolder and all of its Statistics.
	 * 
	 * @param statisticHolder
	 */
	public void registerStatisticHolder( StatisticHolder statisticHolder );

	/**
	 * Deregisters a StatisticHolder and all of its Statistics.
	 * 
	 * @param statisticHolder
	 */
	public void deregisterStatisticHolder( StatisticHolder statisticHolder );

	/**
	 * Gets all registered StatisticHolders.
	 * 
	 * @return
	 */
	public Collection<StatisticHolder> getStatisticHolders();

	/**
	 * Get the minimum amount of time (in ms) between storing values.
	 * 
	 * @return
	 */
	public long getMinimumWriteDelay();

	/**
	 * Gets the ExecutionManager.
	 * 
	 * @return
	 */
	public ExecutionManager getExecutionManager();
}