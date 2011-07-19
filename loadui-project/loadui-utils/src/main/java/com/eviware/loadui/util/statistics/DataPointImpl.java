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
package com.eviware.loadui.util.statistics;

import com.eviware.loadui.api.statistics.DataPoint;

public class DataPointImpl<T extends Number> implements DataPoint<T>
{
	private long timestamp;
	private T value;

	public DataPointImpl( long timestamp, T value )
	{
		this.timestamp = timestamp;
		this.value = value;
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "DataPoint(time: " + timestamp + ", value: " + value + ")";
	}
}