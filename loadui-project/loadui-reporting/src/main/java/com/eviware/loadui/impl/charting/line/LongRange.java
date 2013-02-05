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

import com.jidesoft.range.AbstractNumericRange;

public class LongRange extends AbstractNumericRange<Long>
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