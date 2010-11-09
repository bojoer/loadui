package com.eviware.loadui.impl.statistics;

import com.eviware.loadui.api.statistics.DataPoint;

public class DataPointImpl<T extends Number> implements DataPoint<T>
{

	private long timestamp;
	private T value;

	public DataPointImpl(long timestamp, T value)
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

}
