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

import java.util.Collection;

/**
 * A Statistical Variable containing several Statistics, for several instances.
 * 
 * @author dain.nilsson
 */
public interface StatisticVariable
{
	/**
	 * Gets the available instances of the StatisticVariable.
	 * 
	 * @return
	 */
	public Collection<String> getInstances();

	/**
	 * Gets the available Statistic names for the StatisticVariable.
	 * 
	 * @return
	 */
	public Collection<String> getStatisticNames();

	/**
	 * Gets the Statistic corresponding to the given statistic name and instance.
	 * 
	 * @param statisticName
	 * @param instance
	 * @return
	 */
	public Statistic<?> getStatistic( String statisticName, String instance );
}
