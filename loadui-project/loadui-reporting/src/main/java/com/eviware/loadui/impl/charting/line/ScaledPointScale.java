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

package com.eviware.loadui.impl.charting.line;

import java.util.WeakHashMap;

import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.RealPosition;

/**
 * Provides a scale and creates ChartPoints that are scaled to this.
 * 
 * @author dain.nilsson
 */
public class ScaledPointScale
{
	private final WeakHashMap<ScaledChartPoint, Object> points = new WeakHashMap<ScaledChartPoint, Object>();
	private double scale = 1.0;

	public void setScale( double scale )
	{
		this.scale = scale;
		for( ScaledChartPoint point : points.keySet() )
			point.setY( new RealPosition( point.realY * scale ) );
	}

	public double getScale()
	{
		return scale;
	}

	public ScaledChartPoint createPoint( double x, double y )
	{
		return new ScaledChartPoint( x, y );
	}

	public class ScaledChartPoint extends ChartPoint
	{
		private double realY;

		public ScaledChartPoint( double x, double y )
		{
			super( x, y * scale );
			realY = y;
			points.put( this, null );
		}
	}
}
