package com.eviware.loadui.util.assertion;

import java.util.LinkedList;

import com.google.common.collect.Lists;

public class ToleranceSupport
{
	private final LinkedList<Long> occurrences = Lists.newLinkedList();

	private int period;
	private int allowedOccurrences;

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

	public void clear()
	{
		occurrences.clear();
	}

	public boolean occur( long timestamp )
	{
		if( allowedOccurrences == 0 )
		{
			return true;
		}

		occurrences.add( timestamp );
		if( occurrences.size() >= allowedOccurrences )
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

			if( occurrences.size() >= allowedOccurrences )
			{
				occurrences.clear();
				return true;
			}
		}

		return false;
	}
}
