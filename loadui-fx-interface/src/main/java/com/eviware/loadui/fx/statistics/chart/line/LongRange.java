package com.eviware.loadui.fx.statistics.chart.line;

import com.jidesoft.range.AbstractNumericRange;
import com.jidesoft.range.Range;

public class LongRange extends AbstractNumericRange<Long> implements Range<Long>
{
	private long min, max;

	public LongRange( long min, long max )
	{
		this.min = min;
		this.max = max;
	}

	@Override
	public void adjust( Long newMin, Long newMax )
	{
		long oldMin = min;
		long oldMax = max;
		min = newMin;
		max = newMax;
		firePropertyChange( PROPERTY_MIN, oldMin, newMin );
		firePropertyChange( PROPERTY_MAX, oldMax, newMax );
	}

	@Override
	public boolean contains( Long arg0 )
	{
		return ( min <= arg0 ) && ( arg0 <= max );
	}

	@Override
	public Long lower()
	{
		return min;
	}

	@Override
	public double maximum()
	{
		return max;
	}

	@Override
	public double minimum()
	{
		return min;
	}

	@Override
	public double size()
	{
		return max - min;
	}

	@Override
	public Long upper()
	{
		return max;
	}

	public void setMin( long newMin )
	{
		long oldMin = min;
		min = newMin;
		firePropertyChange( PROPERTY_MIN, oldMin, newMin );
	}

	public void setMax( long newMax )
	{
		long oldMax = max;
		max = newMax;
		firePropertyChange( PROPERTY_MAX, oldMax, newMax );
	}
}