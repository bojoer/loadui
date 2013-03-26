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

import javax.annotation.CheckForNull;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

/**
 * Writes statistics data to a Track. Each call to update allows the
 * StatisticsWriter to internally buffer data, which is aggregated and written
 * to the underlying storage periodically. A StatisticsWriter can expose several
 * Statistics which allow reading the stored statistics and current values.
 * 
 * @author dain.nilsson
 */
public interface StatisticsWriter extends Addressable
{

	/**
	 * Updates the StatisticsWriter with new data, which may trigger data to be
	 * flushed to the underlying Track, or may just buffer it in memory.
	 * 
	 * @param timestamp
	 * @param value
	 */
	public void update( long timestamp, Number value );

	/**
	 * Forces any buffered but not yet written data to be stored. This should
	 * manually be called when ending a test Execution.
	 */
	public void flush();

	/**
	 * Returns an Entry based on raw data acquired from calls to update().
	 * 
	 * @return
	 */
	public Entry output();

	/**
	 * Gets the associated StatisticVariable.
	 * 
	 * @return
	 */
	public StatisticVariable getStatisticVariable();

	/**
	 * Gets the Track for the StatisticsWriter, for the current Execution.
	 * 
	 * @return
	 */
	public TrackDescriptor getTrackDescriptor();

	/**
	 * Gets the type of the StatisticsWriter, which should be unique. This can be
	 * the same as the associated StatisticsWriterFactory.getType().
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Resets the state of the StatisticsWriter.
	 */
	public void reset();

	/**
	 * Get a description for a specific metric.
	 */
	@CheckForNull
	public String getDescriptionForMetric( String metricName );
}
