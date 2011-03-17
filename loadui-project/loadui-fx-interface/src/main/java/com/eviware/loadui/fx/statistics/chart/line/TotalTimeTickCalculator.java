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
package com.eviware.loadui.fx.statistics.chart.line;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.jidesoft.chart.axis.Tick;
import com.jidesoft.chart.axis.TickCalculator;
import com.jidesoft.range.Range;

/**
 * Calculates ticks for use in line charts. The range should contain the time
 * since the start of the test, in ms, for the datapoints.
 * 
 * @author dain.nilsson
 */
public class TotalTimeTickCalculator implements TickCalculator<Long>
{
	public enum Level
	{
		ALL( -1, -1, 0 ), WEEKS( 604800, 100, 4 ), DAYS( 86400, 75, 3 ), HOURS( 3600, 50, 2 ), MINUTES( 60, 50, 1 ), SECONDS(
				1, 25, 0 );

		private final int interval;
		private final int unitWidth;
		private final int level;

		Level( int interval, int unitWidth, int level )
		{
			this.interval = interval;
			this.unitWidth = unitWidth;
			this.level = level;
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

		private static Level[] spanLevels = { MINUTES, HOURS, DAYS, WEEKS };

		public static Level forSpan( long seconds )
		{
			Level last = SECONDS;
			for( Level level : spanLevels )
			{
				if( seconds < level.interval )
					return last;
				last = level;
			}
			return WEEKS;
		}
	}

	private Level level = Level.SECONDS;

	public void setLevel( Level level )
	{
		this.level = level;
	}

	public Level getLevel()
	{
		return level;
	}

	@Override
	public Tick[] calculateTicks( Range<Long> range )
	{
		long firstTick = range.lower() / 1000;
		long end = range.upper() / 1000;
		long span = end - firstTick;
		Level level = this.level == Level.ALL ? Level.forSpan( span ) : this.level;

		long interval = level.interval;
		firstTick -= ( firstTick % interval );

		ArrayList<Tick> ticks = new ArrayList<Tick>();
		for( long i = firstTick; i <= end; i += interval )
			ticks.add( new Tick( i * 1000, formatTime( i, level ) ) );

		return ticks.toArray( new Tick[ticks.size()] );
	}

	private String formatTime( long time, Level level )
	{
		StringBuilder stringBuilder = new StringBuilder();
		if( time < 0 )
		{
			stringBuilder.append( "-" );
			time = -time;
		}

		int seconds = ( int )( time % Level.MINUTES.interval );
		time -= seconds;

		int minutes = ( int )( time % Level.HOURS.interval / Level.MINUTES.interval );
		time -= minutes * Level.MINUTES.interval;

		int hours = ( int )( time % Level.DAYS.interval / Level.HOURS.interval );
		time -= hours * Level.HOURS.interval;

		int days = ( int )( time % Level.WEEKS.interval / Level.DAYS.interval );
		time -= days * Level.DAYS.interval;

		int weeks = ( int )( time / Level.WEEKS.interval );

		boolean started = false;

		if( weeks > 0 || level == Level.WEEKS )
		{
			started = true;
			stringBuilder.append( weeks ).append( "w" );
			if( level == Level.WEEKS )
				return stringBuilder.toString();
			stringBuilder.append( " " );
		}

		if( days > 0 || level == Level.DAYS || started )
		{
			started = true;
			stringBuilder.append( days ).append( "d" );
			if( level == Level.DAYS )
				return stringBuilder.toString();
			stringBuilder.append( " " );
		}

		if( hours > 0 || level == Level.HOURS || started )
		{
			started = true;
			stringBuilder.append( String.format( "%02d", hours ) );
			if( level == Level.HOURS )
				return stringBuilder.toString();
			stringBuilder.append( ":" );
		}

		if( minutes > 0 || level == Level.MINUTES || started )
		{
			started = true;
			stringBuilder.append( String.format( "%02d", minutes ) );
			if( level == Level.MINUTES )
				return stringBuilder.toString();
			stringBuilder.append( ":" );
		}

		stringBuilder.append( String.format( "%02d", seconds ) );
		return stringBuilder.toString();
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener arg0 )
	{
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener arg0 )
	{
	}
}
