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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.statistics.store.Execution;

/**
 * A recording of a value which changes over time, which can be mapped into a
 * graph.
 * 
 * @author dain.nilsson
 */
public interface Statistic<T extends Number> extends Value<T>
{
	/**
	 * Gets the name of this Statistic. Names should be unique per
	 * StatisticHolder.
	 * 
	 * @return
	 */
	@Nonnull
	public String getName();

	/**
	 * Gets the StatisticVariable of this Statistic.
	 * 
	 * @return
	 */
	@Nonnull
	public StatisticVariable getStatisticVariable();

	/**
	 * Gets the source String of the Statistic.
	 * 
	 * @return
	 */
	@Nonnull
	public String getSource();

	/**
	 * Returns the stored DataPoints for the given time span, in chronological
	 * order.
	 * 
	 * @param start
	 *           The lower bound for the timestamps of the retrieved DataPoints,
	 *           as milliseconds since the start of the Execution.
	 * @param end
	 *           The upper bound for the timestamps of the retrieved DataPoints,
	 *           as milliseconds since the start of the Execution.
	 * @param interpolationLevel
	 *           The interpolationLevel defines the minimum time in-between two
	 *           datapoints. Data must be stored using the interpolation levels,
	 *           or none will be available.
	 * @param execution
	 *           The Execution to read data from.
	 * @return
	 */
	@Nonnull
	public Iterable<DataPoint<T>> getPeriod( long start, long end, int interpolationLevel, Execution execution );

	/**
	 * execution defaults to the current Execution.
	 * 
	 * @see getPeriod(int, int, int, Execution)
	 */
	@Nonnull
	public Iterable<DataPoint<T>> getPeriod( long start, long end, int interpolationLevel );

	/**
	 * interpolationLevel defaults to 0.
	 * 
	 * @see getPeriod(int, int, int, Execution)
	 */
	@Nonnull
	public Iterable<DataPoint<T>> getPeriod( long start, long end );

	/**
	 * Gets the timestamp of when the last value was recorded, expressed as the
	 * number of milliseconds since the beginning of the Execution.
	 * 
	 * @return
	 */
	public long getTimestamp();

	/**
	 * Gets the latest DataPoint stored for a particular interpolation level.
	 * 
	 * @param interpolationLevel
	 * @return
	 */
	public DataPoint<T> getLatestPoint( int interpolationLevel );
}