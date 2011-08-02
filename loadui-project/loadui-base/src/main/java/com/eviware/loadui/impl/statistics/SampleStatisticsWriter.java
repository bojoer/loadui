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
package com.eviware.loadui.impl.statistics;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.Entry;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StatisticsWriter for calculating the average of given values.
 * 
 * @author dain.nilsson
 */
public class SampleStatisticsWriter extends AbstractStatisticsWriter
{
	public static final String TYPE = "SAMPLE";

	public static final Logger log = LoggerFactory.getLogger( SampleStatisticsWriter.class );

	public enum Stats
	{
		AVERAGE( "The average %v." ), COUNT, SUM, STD_DEV( "The standard deviation of %v." ), STD_DEV_SUM, PERCENTILE_25TH(
				"The 25th percentile of %v." ), PERCENTILE_75TH( "The 75th percentile of %v." ), PERCENTILE_90TH(
				"The 90th percentile of %v." ), MEDIAN( "The median value of %v." ), MIN( "The mininum value of %v." ), MAX(
				"The maximum value of %v." );

		private final String description;

		Stats()
		{
			this.description = this.name() + " of %v.";
		}

		Stats( String description )
		{
			this.description = description;
		}
	}

	double sum = 0.0;
	long count = 0L;

	private final PriorityQueue<Double> sortedValues = new PriorityQueue<Double>();

	public SampleStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
			Map<String, Class<? extends Number>> trackStructure, Map<String, Object> config )
	{
		super( statisticsManager, variable, trackStructure, config, new Aggregator() );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void update( long timestamp, Number value )
	{
		synchronized( this )
		{
			double doubleValue = value.doubleValue();
			this.sortedValues.add( doubleValue );
			sum += doubleValue;
			count++ ;
			if( lastTimeFlushed + delay <= System.currentTimeMillis() )
			{
				flush();
			}
		}
	}

	@Override
	public Entry output()
	{
		if( count == 0 )
		{
			return null;
		}
		else
		{
			double average = 0.0;
			double stdDev = 0.0;
			double sumTotalSquare = 0.0;
			double percentile25 = 0;
			double percentile50 = 0;
			double percentile75 = 0;
			double percentile90 = 0;

			average = sum / count;
			sumTotalSquare = 0;

			double previousValue = 0;
			int upperPercPos25 = 0, upperPercPos50 = 0, upperPercPos75 = 0, upperPercPos90 = 0;
			double diff25 = 0, diff50 = 0, diff75 = 0, diff90 = 0;

			// percentile precalculations
			if( sortedValues.size() != 1 )
			{
				double percentilePos25 = 0.25 * ( sortedValues.size() - 1 );
				double percentilePos50 = 0.50 * ( sortedValues.size() - 1 );
				double percentilePos75 = 0.75 * ( sortedValues.size() - 1 );
				double percentilePos90 = 0.9 * ( sortedValues.size() - 1 );
				if( percentilePos90 >= sortedValues.size() - 1 )
				{
					upperPercPos90 = sortedValues.size() - 1;
				}
				else
				{
					upperPercPos90 = ( int )Math.floor( percentilePos90 ) + 1;
				}
				upperPercPos75 = ( int )Math.floor( percentilePos75 ) + 1;
				upperPercPos50 = ( int )Math.floor( percentilePos50 ) + 1;
				upperPercPos25 = ( int )Math.floor( percentilePos25 ) + 1;
				diff25 = percentilePos25 - Math.floor( percentilePos25 );
				diff50 = percentilePos50 - Math.floor( percentilePos50 );
				diff75 = percentilePos75 - Math.floor( percentilePos75 );
				diff90 = percentilePos90 - Math.floor( percentilePos90 );
			}

			Double value;
			double min = sortedValues.peek();
			if( sortedValues.size() > 1 )
			{
				int i = 0;
				while( ( value = sortedValues.poll() ) != null )
				{
					sumTotalSquare += Math.pow( value - average, 2 );
					if( i == upperPercPos25 )
						percentile25 = previousValue + diff25 * ( value - previousValue );
					if( i == upperPercPos50 )
						percentile50 = previousValue + diff50 * ( value - previousValue );
					if( i == upperPercPos75 )
						percentile75 = previousValue + diff75 * ( value - previousValue );
					if( i == upperPercPos90 )
						percentile90 = previousValue + diff90 * ( value - previousValue );
					previousValue = value;
					i++ ;
				}
			}
			else
			{
				previousValue = percentile25 = percentile50 = percentile75 = percentile90 = min;
			}
			double max = previousValue;

			stdDev = Math.sqrt( sumTotalSquare / count );
			// percentile = perc.evaluate( pValues );

			lastTimeFlushed = System.currentTimeMillis();

			Entry e = at( lastTimeFlushed ).put( Stats.AVERAGE.name(), average ).put( Stats.COUNT.name(), count )
					.put( Stats.SUM.name(), sum ).put( Stats.STD_DEV_SUM.name(), sumTotalSquare )
					.put( Stats.STD_DEV.name(), stdDev ).put( Stats.PERCENTILE_25TH.name(), percentile25 )
					.put( Stats.PERCENTILE_75TH.name(), percentile75 ).put( Stats.PERCENTILE_90TH.name(), percentile90 )
					.put( Stats.MEDIAN.name(), percentile50 ).put( Stats.MIN.name(), min ).put( Stats.MAX.name(), max )
					.build();

			// reset counters
			sum = 0;
			count = 0;
			sortedValues.clear();
			return e;
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		sortedValues.clear();
		sum = 0.0;
		count = 0L;
	}

	private static class Aggregator implements EntryAggregator
	{
		/**
		 * Aggregates a list of Entries.
		 * 
		 * Note that we are using Population based Standard deviation, as opposed
		 * to Sample based Standard deviation.
		 * 
		 * The percentile calculation assumes that the values are normally
		 * distributed. This assumption might be wrong, but has to be done to
		 * avoid storing and iterating through the whole set of actual values
		 * provided by the loadUI components.
		 * 
		 * @author henrik.olsson
		 */
		@Override
		public Entry aggregate( Set<Entry> entries, boolean parallel )
		{
			if( entries.size() <= 1 )
				return Iterables.getFirst( entries, null );

			long timestamp = -1;
			double totalSum = 0;
			long totalCount = 0;
			double min = Double.MAX_VALUE;
			double max = 0;
			double median = 0, percentile25 = 0, percentile75 = 0, percentile90 = 0, stddev = 0;

			for( Entry e : entries )
			{
				long count = e.getValue( Stats.COUNT.name() ).longValue();
				double average = e.getValue( Stats.AVERAGE.name() ).doubleValue();

				timestamp = Math.max( timestamp, e.getTimestamp() );

				// median - not really median of all subpopulations, rather a weighted
				// average of the subpopulations' medians (performance reasons).
				median += count * e.getValue( Stats.MEDIAN.name() ).doubleValue();
				percentile25 += count * e.getValue( Stats.PERCENTILE_25TH.name() ).doubleValue();
				percentile75 += count * e.getValue( Stats.PERCENTILE_75TH.name() ).doubleValue();
				percentile90 += count * e.getValue( Stats.PERCENTILE_90TH.name() ).doubleValue();
				stddev += count * e.getValue( Stats.STD_DEV.name() ).doubleValue();

				// average
				totalSum += count * average;
				totalCount += count;

				min = Math.min( min, e.getValue( Stats.MIN.name() ).doubleValue() );
				max = Math.max( max, e.getValue( Stats.MAX.name() ).doubleValue() );
			}

			median = median / totalCount;
			percentile25 = percentile25 / totalCount;
			percentile75 = percentile75 / totalCount;
			percentile90 = percentile90 / totalCount;
			stddev = stddev / totalCount;

			double totalAverage = totalSum / totalCount;

			return at( timestamp ).put( Stats.AVERAGE.name(), totalAverage ).put( Stats.COUNT.name(), totalCount )
					.put( Stats.STD_DEV.name(), stddev ).put( Stats.PERCENTILE_90TH.name(), percentile90 )
					.put( Stats.PERCENTILE_25TH.name(), percentile25 ).put( Stats.PERCENTILE_75TH.name(), percentile75 )
					.put( Stats.MEDIAN.name(), median ).put( Stats.MIN.name(), min ).put( Stats.MAX.name(), max ).build();
		}
	}

	/**
	 * Factory for instantiating AverageStatisticWriters.
	 * 
	 * @author dain.nilsson
	 * 
	 *         Define what Statistics this writer should write in Track.
	 * 
	 */
	public static class Factory implements StatisticsWriterFactory
	{
		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
				Map<String, Object> config )
		{
			Map<String, Class<? extends Number>> trackStructure = new TreeMap<String, Class<? extends Number>>();

			// init statistics
			trackStructure.put( Stats.AVERAGE.name(), Double.class );
			trackStructure.put( Stats.MIN.name(), Double.class );
			trackStructure.put( Stats.MAX.name(), Double.class );
			// trackStructure.put( Stats.COUNT.name(), Double.class );
			// trackStructure.put( Stats.SUM.name(), Double.class );
			trackStructure.put( Stats.STD_DEV.name(), Double.class );
			// trackStructure.put( Stats.STD_DEV_SUM.name(), Double.class );
			trackStructure.put( Stats.PERCENTILE_25TH.name(), Double.class );
			trackStructure.put( Stats.PERCENTILE_75TH.name(), Double.class );
			trackStructure.put( Stats.PERCENTILE_90TH.name(), Double.class );
			trackStructure.put( Stats.MEDIAN.name(), Double.class );

			return new SampleStatisticsWriter( statisticsManager, variable, trackStructure, config );
		}
	}

	@Override
	public String getDescriptionForMetric( String metricName )
	{
		for( Stats s : Stats.values() )
		{
			if( s.name().equals( metricName ) )
				return s.description;
		}
		return null;
	}

}