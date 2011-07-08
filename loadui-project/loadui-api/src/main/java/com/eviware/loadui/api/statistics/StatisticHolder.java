/*
 * Copyright 2011 eviware software ab
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

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Labeled;

/**
 * A Holder of Statistics. Fires CollectionEvents for contained
 * StatisticVariables.
 * 
 * @author dain.nilsson
 */
public interface StatisticHolder extends EventFirer, Addressable, Labeled
{
	public static final String STATISTICS = StatisticHolder.class.getName() + "@statistics";

	/**
	 * Retrieves a particular Statistic by its name.
	 * 
	 * @param statisticName
	 * @return
	 */
	public StatisticVariable getStatisticVariable( String statisticVariableName );

	/**
	 * Gets a list of the names of all the contained StatisticsVariables.
	 * 
	 * @return
	 */
	public Set<String> getStatisticVariableNames();
}
