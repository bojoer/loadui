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
package com.eviware.loadui.impl.charting.line;

import java.util.Collections;
import java.util.concurrent.Callable;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public abstract class AbstractLineSegmentModel extends AbstractSegmentModel
{
	protected double scalar = 1;
	private double minY = Double.POSITIVE_INFINITY;
	private double maxY = Double.NEGATIVE_INFINITY;

	public AbstractLineSegmentModel( LineSegment segment, String name, ChartStyle chartStyle )
	{
		super( segment, name, chartStyle );
	}

	protected void doRedraw( final Statistic<?> statistic, final long xMin, final long xMax, final int level )
	{
		appendRead( new Callable<Iterable<DataPoint<?>>>()
		{
			@Override
			@SuppressWarnings( "unchecked" )
			public Iterable<DataPoint<?>> call() throws Exception
			{
				return ( Iterable<DataPoint<?>> )( execution == null ? Collections.emptyList() : statistic.getPeriod( xMin,
						xMax, level, execution ) );
			}
		}, scalar );
	}

	public double getMaxY()
	{
		return maxY;
	}

	public double getMinY()
	{
		return minY;
	}

	@Override
	public DefaultChartModel addPoint( double x, double y, boolean flush )
	{
		minY = Math.min( minY, y );
		maxY = Math.max( maxY, y );

		return super.addPoint( x, y, flush );
	}

	@Override
	public void clearPoints()
	{
		super.clearPoints();

		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
	}
}
