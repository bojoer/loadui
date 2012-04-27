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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.eviware.loadui.api.charting.line.ZoomLevel;
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
	private ZoomLevel level = ZoomLevel.SECONDS;

	public void setLevel( ZoomLevel level )
	{
		this.level = level;
	}

	public ZoomLevel getLevel()
	{
		return level;
	}

	@Override
	public Tick[] calculateTicks( Range<Long> range )
	{
		long firstTick = range.lower() / 1000;
		long end = range.upper() / 1000;
		long span = end - firstTick;
		final ZoomLevel ticksLevel = level == ZoomLevel.ALL ? ZoomLevel.forSpan( span ) : level;

		final long interval = ticksLevel.getInterval();
		if( interval < 1 )
			throw new RuntimeException( "Interval must be positive! Interval = " + interval );
		firstTick -= ( firstTick % interval );

		ArrayList<Tick> ticks = new ArrayList<Tick>();
		for( long i = firstTick; i <= end; i += interval )
			ticks.add( makeTick( i, ticksLevel ) );

		return ticks.toArray( new Tick[ticks.size()] );
	}

	private Tick makeTick( long time, ZoomLevel level )
	{
		if( ZoomLevel.ALL != level && time % level.getMajorTickInterval() != 0 )
			return new Tick( time * 1000 );
		else
			return new Tick( time * 1000, formatTime( time, level ) );
	}

	private String formatTime( long time, ZoomLevel level )
	{
		StringBuilder stringBuilder = new StringBuilder();
		if( time < 0 )
		{
			stringBuilder.append( "-" );
			time = -time;
		}

		int seconds = ( int )( time % ZoomLevel.MINUTES.getInterval() );
		time -= seconds;

		int minutes = ( int )( time % ZoomLevel.HOURS.getInterval() / ZoomLevel.MINUTES.getInterval() );
		time -= minutes * ZoomLevel.MINUTES.getInterval();

		int hours = ( int )( time % ZoomLevel.DAYS.getInterval() / ZoomLevel.HOURS.getInterval() );
		time -= hours * ZoomLevel.HOURS.getInterval();

		int days = ( int )( time % ZoomLevel.WEEKS.getInterval() / ZoomLevel.DAYS.getInterval() );
		time -= days * ZoomLevel.DAYS.getInterval();

		int weeks = ( int )( time / ZoomLevel.WEEKS.getInterval() );

		boolean started = false;

		if( weeks > 0 || level == ZoomLevel.WEEKS )
		{
			started = true;
			stringBuilder.append( weeks ).append( "w" );
			if( level == ZoomLevel.WEEKS )
				return stringBuilder.toString();
			stringBuilder.append( " " );
		}

		if( days > 0 || level == ZoomLevel.DAYS || started )
		{
			started = true;
			stringBuilder.append( days ).append( "d" );
			if( level == ZoomLevel.DAYS )
				return stringBuilder.toString();
			stringBuilder.append( " " );
		}

		if( hours > 0 || level == ZoomLevel.HOURS || started )
		{
			started = true;
			stringBuilder.append( String.format( "%02d", hours ) );
			if( level == ZoomLevel.HOURS )
				return stringBuilder.toString();
			stringBuilder.append( ":" );
		}

		if( minutes > 0 || level == ZoomLevel.MINUTES || started )
		{
			started = true;
			stringBuilder.append( String.format( "%02d", minutes ) );
			if( level == ZoomLevel.MINUTES )
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
