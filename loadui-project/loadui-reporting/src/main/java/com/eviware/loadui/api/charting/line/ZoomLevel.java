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
package com.eviware.loadui.api.charting.line;

/**
 * Defines the zoom level of a chart.
 * 
 * @author dain.nilsson
 */
public enum ZoomLevel
{
	ALL( 1, -1, 0, 1 ), WEEKS( 604800, 100, 4, 1 ), DAYS( 86400, 75, 3, 1 ), HOURS( 3600, 50, 2, 1 ), MINUTES( 60, 50,
			1, 1 ), SECONDS( 1, 12, 0, 5 );

	private final int interval;
	private final int unitWidth;
	private final int level;
	private final int majorTickInterval;

	ZoomLevel( int interval, int unitWidth, int level, int majorTickInterval )
	{
		this.interval = interval;
		this.unitWidth = unitWidth;
		this.level = level;
		this.majorTickInterval = majorTickInterval * interval;
	}

	public int getInterval()
	{
		return interval;
	}

	public int getUnitWidth()
	{
		return unitWidth;
	}

	public int getLevel()
	{
		return level;
	}

	public int getMajorTickInterval()
	{
		return majorTickInterval;
	}

	private static ZoomLevel[] spanLevels = { MINUTES, HOURS, DAYS, WEEKS };

	public static ZoomLevel forSpan( long seconds )
	{
		ZoomLevel last = SECONDS;
		for( ZoomLevel level : spanLevels )
		{
			if( seconds < level.interval )
				return last;
			last = level;
		}
		return WEEKS;
	}
}
