package com.eviware.loadui.api.statistics;

/**
 * Mutable version of a StatisticVariable which is used to provide data to its
 * writers.
 * 
 * @author dain.nilsson
 */
public interface MutableStatisticVariable extends StatisticVariable
{
	/**
	 * Updates the MutableStatisticVariable with new data, which will be passed
	 * to the attached StatisticsWriters.
	 * 
	 * @param timestamp
	 * @param value
	 */
	public void update( long timestamp, Number value );
}
