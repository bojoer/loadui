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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Labeled;

/**
 * A Statistical Variable containing several Statistics, for several instances.
 * 
 * @author dain.nilsson
 */
public interface StatisticVariable extends Labeled, Describable.Mutable
{
	/**
	 * When writing to the local, or main source, use this String as the source
	 * identifier.
	 */
	public static final String MAIN_SOURCE = "main";

	/**
	 * Collection of Statistics
	 */
	public static final String STATISTICS = StatisticVariable.class.getName() + "@statistics";

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

	/**
	 * A mutable version of StatisticVariable. Its values are controlled by one
	 * or more attached StatisticWriters, which receive data from the update
	 * method.
	 * 
	 * @author dain.nilsson
	 */
	public interface Mutable extends StatisticVariable
	{
		/**
		 * Updates the StatisticVariable.Mutable with new data, which will be
		 * passed to the attached StatisticsWriters.
		 * 
		 * @param timestamp
		 * @param value
		 */
		public void update( long timestamp, Number value );
	}

	/**
	 * Gets the description for this a specific statistic.
	 * 
	 * @param statisticName
	 * @return
	 */
	public String getDescriptionForStatistic( @Nonnull String statisticName );
}
