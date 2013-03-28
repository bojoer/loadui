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

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.statistics.model.Chart;

/**
 * A Holder of Statistics. Fires CollectionEvents for contained
 * StatisticVariables.
 * 
 * @author dain.nilsson
 */
public interface StatisticHolder extends EventFirer, Chart.Owner
{
	/**
	 * Collection of StatisticVariables.
	 */
	public static final String STATISTIC_VARIABLES = StatisticHolder.class.getName() + "@statisticVariables";

	/**
	 * Retrieves a particular Statistic by its name.
	 * 
	 * @param statisticName
	 * @param description
	 * @return
	 */
	public StatisticVariable getStatisticVariable( String statisticVariableName );

	/**
	 * Gets a set of the names of all the contained StatisticsVariables.
	 * 
	 * @return
	 */
	@Nonnull
	public Set<String> getStatisticVariableNames();

	/**
	 * Gets a set of all the contained StatisticsVariables.
	 * 
	 * @return
	 */
	@Nonnull
	public Collection<? extends StatisticVariable> getStatisticVariables();

	/**
	 * Gets a Set (which can be empty) of default Statistics contained under the
	 * StatisticHolder to be displayed by default.
	 * 
	 * @return
	 */
	@Nonnull
	public Set<? extends Statistic.Descriptor> getDefaultStatistics();

	/**
	 * Returns the CanvasItem associated with this StatisticHolder.
	 * 
	 * @return
	 */
	@Nonnull
	public CanvasItem getCanvas();
}
