/*
 * Copyright 2010 eviware software ab
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

/**
 * A Statistical Variable containing several Statistics, for several instances.
 * 
 * @author dain.nilsson
 */
public interface StatisticVariable
{
	/**
	 * Gets the name of the StatisticVariable.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Gets the StatisticHolder which this StatisticVariable belongs to.
	 * 
	 * @return
	 */
	public StatisticHolder getStatisticHolder();

	/**
	 * Gets the available sources of the StatisticVariable.
	 * 
	 * @return
	 */
	public Set<String> getSources();

	/**
	 * Gets the available Statistic names for the StatisticVariable.
	 * 
	 * @return
	 */
	public Set<String> getStatisticNames();

	/**
	 * Gets the Statistic corresponding to the given statistic name and source.
	 * 
	 * @param statisticName
	 * @param source
	 * @return
	 */
	public Statistic<?> getStatistic( String statisticName, String source );
}
