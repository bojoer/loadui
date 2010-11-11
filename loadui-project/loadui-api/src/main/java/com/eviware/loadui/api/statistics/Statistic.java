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

import com.eviware.loadui.api.serialization.Value;

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
	public String getName();

	/**
	 * Gets the StatisticHolder of this Statistic.
	 * 
	 * @return
	 */
	public StatisticVariable getStatisticVariable();

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
	 * @return
	 */
	public Iterable<DataPoint<T>> getPeriod( int start, int end );
}