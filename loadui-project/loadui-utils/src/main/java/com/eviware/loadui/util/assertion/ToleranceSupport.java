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
package com.eviware.loadui.util.assertion;

import java.util.LinkedList;

import com.google.common.collect.Lists;

/**
 * Support for adding tolerance to some occurrence, in the form of
 * "allow x occurrences within y seconds".
 * 
 * @author dain.nilsson
 */
public class ToleranceSupport
{
	private final LinkedList<Long> occurrences = Lists.newLinkedList();

	private int period;
	private int allowedOccurrences;

	public ToleranceSupport()
	{
	}

	public ToleranceSupport( int period, int allowedOccurances )
	{
		setTolerance( period, allowedOccurances );
	}

	/**
	 * Sets the tolerance to allow allowedOccurrences occurrences within period
	 * milliseconds without triggering.
	 * 
	 * @param period
	 * @param allowedOccurrences
	 */
	public void setTolerance( int period, int allowedOccurrences )
	{
		this.period = period;
		this.allowedOccurrences = allowedOccurrences;
	}

	public int getPeriod()
	{
		return period;
	}

	public int getAllowedOccurrences()
	{
		return allowedOccurrences;
	}

	/**
	 * Clears the buffered occurrences, resetting the state.
	 */
	public void clear()
	{
		occurrences.clear();
	}

	/**
	 * Signal an occurrence at the given time, returns false if this is within
	 * the tolerated parameters, true if not.
	 * 
	 * @param timestamp
	 * @return
	 */
	public boolean occur( long timestamp )
	{
		if( allowedOccurrences == 0 )
		{
			return true;
		}

		occurrences.add( timestamp );
		if( occurrences.size() > allowedOccurrences )
		{
			if( period == 0 )
			{
				occurrences.clear();
				return true;
			}

			long expiry = timestamp - period;
			while( !occurrences.isEmpty() && occurrences.getFirst() < expiry )
			{
				occurrences.removeFirst();
			}

			if( occurrences.size() > allowedOccurrences )
			{
				occurrences.clear();
				return true;
			}
		}

		return false;
	}
}
