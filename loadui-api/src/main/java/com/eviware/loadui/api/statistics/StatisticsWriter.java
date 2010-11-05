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
import java.util.Map;

/**
 * Writes statistics data to a Track. Each call to update allows the
 * StatisticsWriter to internally buffer data, which is aggregated and written
 * to the underlying storage periodically. A StatisticsWriter can expose several
 * Statistics which allow reading the stored statistics and current values.
 * 
 * @author dain.nilsson
 */
public interface StatisticsWriter
{
	/**
	 * Gets the number of values which are needed each update. This number does
	 * not have to be the same as the number of values stored per entry.
	 * 
	 * @return
	 */
	public int getValueCount();

	/**
	 * Updates the StatisticsWriter with new data, which may trigger data to be
	 * flushed to the underlying Track, or may just buffer it in memory.
	 * 
	 * @param timestamp
	 * @param values
	 */
	public void update( long timestamp, Number... values );

	/**
	 * Forces any buffered but not yet written data to be stored. This should
	 * manually be called when ending a test Execution.
	 */
	public void flush();

	/**
	 * Gets a Map of the names of the Statistics that this StatisticsWriter
	 * provides, paired with the Number subclass of the Statistic.
	 * 
	 * @return
	 */
	public Map<String, Class<? extends Number>> getStatisticsNames();

	/**
	 * Gets the current value of a particular Statistic instance that the
	 * StatisticsWriter provides.
	 * 
	 * @param <T>
	 * @param statisticName
	 * @param instance
	 * @return
	 */
	public <T extends Number> T getStatisticValue( String statisticName, String instance );

	/**
	 * Gets an Iterable of DataPoints for the given Statistic instance for the
	 * given range.
	 * 
	 * @param <T>
	 * @param statisticName
	 * @param instance
	 * @param start
	 * @param end
	 * @return
	 */
	public <T extends Number> Iterable<DataPoint<T>> getStatisticRange( String statisticName, String instance,
			long start, long end );
}
