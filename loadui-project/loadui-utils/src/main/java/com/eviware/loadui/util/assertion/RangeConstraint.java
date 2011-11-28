package com.eviware.loadui.util.assertion;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.assertion.Constraint;

public class RangeConstraint implements Constraint<Number>
{
	private static final long serialVersionUID = 7196203941990578604L;

	private final double min;
	private final double max;

	public RangeConstraint( @Nonnull Number min, @Nonnull Number max )
	{
		this.min = min.doubleValue();
		this.max = max.doubleValue();
	}

	@Override
	public boolean validate( Number value )
	{
		double doubleVal = value.doubleValue();
		return min <= doubleVal && doubleVal <= max;
	}

	@Override
	public String toString()
	{
		return String.format( "RangeConstraint(%.02f - %.02f)", min, max );
	}
}