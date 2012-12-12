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

import java.util.Observable;

/**
 * Used to display a time interval, and a position within it. Has a start time,
 * a stop time, an end time (of the timeframe) and a position. It may be in one
 * of two states, running or not running.
 * 
 * @author dain.nilsson
 */
public class IntervalModel extends Observable
{
	public static final long INFINITE = 20000000000L;

	private long start = 0;
	private long stop = 0;
	private long end = 0;

	private long startTime = 0;
	private long startPosition = 0;
	private boolean running = false;

	public long getStart()
	{
		return start;
	}

	public void setStart( long start )
	{
		this.start = start;
		setChanged();
	}

	public long getStop()
	{
		return Math.max( start, stop );
	}

	public void setStop( long stop )
	{
		this.stop = stop;
		setChanged();
	}

	public long getEnd()
	{
		return Math.max( getStop(), end );
	}

	public void setEnd( long end )
	{
		this.end = end;
		setChanged();
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRunning( boolean running )
	{
		if( this.running != running )
		{
			if( running )
				startTime = System.currentTimeMillis();
			else
				startPosition = getPosition();

			this.running = running;
			setChanged();
		}
	}

	public long getPosition()
	{
		return Math.min( running ? startPosition + System.currentTimeMillis() - startTime : startPosition, getEnd() );
	}

	public void setPosition( long position )
	{
		startTime = System.currentTimeMillis();
		startPosition = position;
		setChanged();
	}

	public boolean isInfinite()
	{
		return stop == INFINITE;
	}
}