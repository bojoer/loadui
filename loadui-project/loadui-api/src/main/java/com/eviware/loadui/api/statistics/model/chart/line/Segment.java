package com.eviware.loadui.api.statistics.model.chart.line;

import com.eviware.loadui.api.model.AttributeHolder;

public interface Segment extends AttributeHolder
{
	boolean isRemoved();

	public interface Removable extends Segment
	{
		public void remove();
	}
}