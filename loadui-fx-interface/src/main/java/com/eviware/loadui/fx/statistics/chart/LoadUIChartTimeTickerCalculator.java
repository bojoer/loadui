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
package com.eviware.loadui.fx.statistics.chart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.jidesoft.chart.axis.AbstractTimeTickCalculator;
import com.jidesoft.chart.axis.Tick;
import com.jidesoft.range.Range;

public class LoadUIChartTimeTickerCalculator extends AbstractTimeTickCalculator
{
	enum Level
	{
		ALL, WEEKS, DAYS, HOURS, MINUTES, SECONDS
	}

	private Level level = Level.SECONDS;

	@Override
	public Tick[] calculateTicks( Range<Date> range )
	{
		ArrayList<Tick> result = new ArrayList<Tick>();
		int period = 1000;
		for( double start = range.minimum(); start <= range.maximum(); start += period ) {
			switch( level )
			{
			case ALL:
			case SECONDS :
				period = 1000;
				SimpleDateFormat df = new SimpleDateFormat( "ss" );
				result.add( new Tick(start, df.format( new Date(( long )start) ) ));
				break;
			case MINUTES :
				period = 1000 * 60;
				df = new SimpleDateFormat( "mm:ss" );
				result.add( new Tick(start, df.format( new Date(( long )start) ) ));
				break;
			case HOURS :
				period = 1000 * 60 * 60;
				df = new SimpleDateFormat( "HH:mm:ss" );
				result.add( new Tick(start, df.format( new Date(( long )start) ) ));
				break;
			case DAYS:
				period = 1000 * 60 * 60 * 24;
				df = new SimpleDateFormat( "E" );
				result.add( new Tick(start, df.format( new Date(( long )start) ) ));
				break;
			case WEEKS:
				period = 1000 * 60 * 60 * 24 * 365;
				df = new SimpleDateFormat( "w" );
				result.add( new Tick(start, df.format( new Date(( long )start) ) ));
				break;
			default :
				period = 1000;
				result.add( new Tick(start, String.valueOf(start/period) ) );
			}
		}
		return (Tick[])result.toArray( new Tick[0] );
	}

	public void setLevel( String level )
	{
		this.level = Level.valueOf( level.toUpperCase() );
	}

}
