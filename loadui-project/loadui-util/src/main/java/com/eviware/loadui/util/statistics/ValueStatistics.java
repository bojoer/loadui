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
package com.eviware.loadui.util.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.util.MapUtils;
import org.apache.commons.math.stat.descriptive.rank.Percentile;


public class ValueStatistics
{
	private final List<DataPoint> dataPoints = new ArrayList<DataPoint>();
	private long period;
	private int snapshotLength = 1000;

	public ValueStatistics( long period )
	{
		this.period = period;
	}

	public synchronized void addValue( long timestamp, long value )
	{
		dataPoints.add( new DataPoint( timestamp, value ) );
	}

	public void setPeriod( long period )
	{
		this.period = period;
	}

	public synchronized long size()
	{
		return dataPoints.size();
	}

	public synchronized DataPoint getValueAt( int index )
	{
		if( index < 0 )
			index += dataPoints.size();
		return dataPoints.get( index );
	}

	public long getPeriod()
	{
		return period;
	}

	public synchronized Map<String, Number> getData( long timestamp )
	{
		long max = 0;
		long min = Long.MAX_VALUE;
		long sum = 0;

		for( Iterator<DataPoint> it = dataPoints.iterator(); it.hasNext(); )
		{
			DataPoint dataPoint = it.next();
			if( dataPoint.timestamp < timestamp - period && period > 0 )
				it.remove();
			else
			{
				sum += dataPoint.value;
				max = Math.max( max, dataPoint.value );
				min = Math.min( min, dataPoint.value );
			}
		}

		int count = dataPoints.size();
		double avg = count > 0 ? ( double )sum / count : 0;

		double stdDev = 0;
		if( count > 0 )
		{
			for( DataPoint dataPoint : dataPoints )
				stdDev += Math.pow( dataPoint.value - avg, 2 );
			stdDev = Math.sqrt( stdDev / count );
		}

		double tps = 0;
		long vps = 0;
		if( count >= 2 )
		{
			int samples = 0;
			long earliest = timestamp - snapshotLength;
			DataPoint point = null;
			while( ++samples < count )
			{
				point = dataPoints.get( count - samples );
				vps += point.value;
				if( point.timestamp < earliest )
					break;
			}

			long timeDelta = timestamp - point.timestamp;
			
			timeDelta = timeDelta==0?1000:timeDelta;

			vps = vps * 1000 / timeDelta;
			tps = ( samples - 1 ) * 1000.0 / timeDelta;
		}

		long duration = dataPoints.get( dataPoints.size() - 1 ).timestamp - dataPoints.get( 0 ).timestamp;
		
		Percentile perc = new Percentile(90);
		
		double [] dataSet = new double [dataPoints.size()];
		int i = 0;
		for (DataPoint point:dataPoints ) {
			dataSet[i] = point.value;
			i++;
		}
		
		double percentile = perc.evaluate(dataSet, 90);

		return MapUtils.build( String.class, Number.class ) //
				.put( "Max", max ) //
				.put( "Min", min == Long.MAX_VALUE ? 0 : min ) //
				.put( "Avg", avg ) //
				.put( "Sum", sum ) //
				.put( "Std-Dev", stdDev ) //
				.put( "Tps", tps ) //
				.put( "Avg-Tps", duration > 0 ? 1000 * count / duration : 0 ) //
				.put( "Vps", vps ) //
				.put( "Avg-Vps", duration > 0 ? 1000 * sum / duration : 0 ) //
				.put( "Percentile", percentile ) //
				.put( "AvgResponseSize", 1000 * sum / dataPoints.size() ) //
				.get();
	}

	public void reset()
	{
		dataPoints.clear();
	}

	public class DataPoint
	{
		private final long timestamp;
		private final long value;

		public DataPoint( long timestamp, long value )
		{
			this.timestamp = timestamp;
			this.value = value;
		}

		public long getValue()
		{
			return value;
		}

		public long getTimestamp()
		{
			return timestamp;
		}
	}
}
