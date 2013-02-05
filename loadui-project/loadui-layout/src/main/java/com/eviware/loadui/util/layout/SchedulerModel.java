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
package com.eviware.loadui.util.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * @author predrag.vucetic
 */
public class SchedulerModel extends Observable
{

	private List<Day> days = new ArrayList<Day>();

	private final List<Integer> seconds = new ArrayList<Integer>();
	private final List<Integer> minutes = new ArrayList<Integer>();
	private final List<Integer> hours = new ArrayList<Integer>();

	private long duration = 0;
	private long maxDuration = 0;
	private int runsLimit = 0;
	private int runsCounter = 0;

	public void incrementRunsCounter()
	{
		runsCounter++ ;
		setChanged();
		notifyObservers();
	}

	public void resetRunsCounter()
	{
		runsCounter = 0;
		setChanged();
	}

	public List<Integer> getSeconds()
	{
		return seconds;
	}

	public void setSeconds( Set<Integer> seconds )
	{
		this.seconds.clear();
		Iterator<Integer> i = seconds.iterator();
		while( i.hasNext() )
		{
			Integer second = i.next();
			if( second >= 0 && second <= 59 )
			{
				this.seconds.add( second );
			}
		}
		setChanged();
	}

	public List<Integer> getMinutes()
	{
		return minutes;
	}

	public void setMinutes( Set<Integer> minutes )
	{
		this.minutes.clear();
		Iterator<Integer> i = minutes.iterator();
		while( i.hasNext() )
		{
			Integer minute = i.next();
			if( minute >= 0 && minute <= 59 )
			{
				this.minutes.add( minute );
			}
		}
		setChanged();
	}

	public List<Integer> getHours()
	{
		return hours;
	}

	public void setHours( Set<Integer> hours )
	{
		this.hours.clear();
		Iterator<Integer> i = hours.iterator();
		while( i.hasNext() )
		{
			Integer hour = i.next();
			if( hour >= 0 && hour <= 59 )
			{
				this.hours.add( hour );
			}
		}
		setChanged();
	}

	public List<Day> getDays()
	{
		return days;
	}

	public Boolean[] getDaysAsBoolean()
	{
		Boolean[] d = new Boolean[7];
		d[0] = days.contains( Day.MON );
		d[1] = days.contains( Day.TUE );
		d[2] = days.contains( Day.WED );
		d[3] = days.contains( Day.THU );
		d[4] = days.contains( Day.FRI );
		d[5] = days.contains( Day.SAT );
		d[6] = days.contains( Day.SUN );
		return d;
	}

	public void setDays( Set<Integer> daysInWeek )
	{
		days.clear();
		Iterator<Integer> dayIterator = daysInWeek.iterator();
		while( dayIterator.hasNext() )
		{
			switch( dayIterator.next() )
			{
			case 1 :
				days.add( Day.SUN );
				break;
			case 2 :
				days.add( Day.MON );
				break;
			case 3 :
				days.add( Day.TUE );
				break;
			case 4 :
				days.add( Day.WED );
				break;
			case 5 :
				days.add( Day.THU );
				break;
			case 6 :
				days.add( Day.FRI );
				break;
			case 7 :
				days.add( Day.SAT );
				break;
			default :
				break;
			}
		}
		setChanged();
	}

	public void setAllDays()
	{
		days.clear();
		days.add( Day.MON );
		days.add( Day.TUE );
		days.add( Day.WED );
		days.add( Day.THU );
		days.add( Day.FRI );
		days.add( Day.SAT );
		days.add( Day.SUN );
		setChanged();
	}

	public void setDays( List<Day> days )
	{
		this.days = days;
		setChanged();
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration( long duration )
	{
		this.duration = duration;
		setChanged();
	}

	public int getRunsLimit()
	{
		return runsLimit;
	}

	public void setRunsLimit( int runsCount )
	{
		this.runsLimit = runsCount;
		setChanged();
	}

	public long getMaxDuration()
	{
		return maxDuration;
	}

	public void setMaxDuration( long maxDuration )
	{
		this.maxDuration = maxDuration;
		setChanged();
	}

	public enum Day
	{
		SUN, MON, TUE, WED, THU, FRI, SAT
	}

	public int getTotalCountPerDay()
	{
		return hours.size() * minutes.size() * seconds.size();
	}

	public Map<Integer, List<ExecutionTime>> getExecutionTimeMap()
	{
		List<ExecutionTime> prevList = new ArrayList<ExecutionTime>();
		List<ExecutionTime> nextList = new ArrayList<ExecutionTime>();

		int current = new ExecutionTime().getTime();
		//move 10 seconds in future to be sure 
		//that last executed job won't be included 
		//in nextList. 
		current += 10000;

		Boolean[] daysAsBoolean = getDaysAsBoolean();
		for( int i = 0; i < daysAsBoolean.length; i++ )
		{
			int dayIndex = i + 1;
			if( daysAsBoolean[i] )
			{
				for( Integer h : hours )
				{
					for( Integer m : minutes )
					{
						for( Integer s : seconds )
						{
							int time = ( ( ( dayIndex * 24 + h ) * 60 + m ) * 60 + s ) * 1000;
							if( time + duration < current )
							{
								prevList.add( new ExecutionTime( dayIndex, h, m, s ) );
							}
							else
							{
								nextList.add( new ExecutionTime( dayIndex, h, m, s ) );
							}
						}
					}
				}
			}
		}
		nextList.addAll( prevList );

		while( runsLimit > 0 && nextList.size() > ( runsLimit - runsCounter ) )
		{
			nextList.remove( nextList.size() - 1 );
		}

		HashMap<Integer, List<ExecutionTime>> result = new HashMap<Integer, List<ExecutionTime>>();
		for( int i = 1; i < 8; i++ )
		{
			result.put( i, new ArrayList<ExecutionTime>() );
		}
		for( ExecutionTime et : nextList )
		{
			result.get( et.getDay() ).add( et );
		}
		return result;
	}
}