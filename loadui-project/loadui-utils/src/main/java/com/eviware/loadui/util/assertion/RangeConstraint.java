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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.assertion.Constraint;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Constraint that checks if a given value is within a specific range,
 * inclusive.
 * 
 * @author dain.nilsson
 */
public class RangeConstraint implements Constraint<Number>
{
	private static final long serialVersionUID = 7196203941990578604L;

	private final double min;
	private final double max;

	public RangeConstraint( @Nonnull Number min, @Nonnull Number max )
	{
		this.min = min.doubleValue();
		this.max = max.doubleValue();

		Preconditions.checkArgument( this.min <= this.max, "min must be less than or equal to max!" );
	}

	@Override
	public boolean validate( Number value )
	{
		double doubleVal = value.doubleValue();
		return min <= doubleVal && doubleVal <= max;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode( min, max );
	}

	@Override
	public boolean equals( Object obj )
	{

		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		RangeConstraint other = ( RangeConstraint )obj;
		if( Double.doubleToLongBits( max ) != Double.doubleToLongBits( other.max ) )
			return false;
		if( Double.doubleToLongBits( min ) != Double.doubleToLongBits( other.min ) )
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return String.format( "range %.02f - %.02f", min, max );
	}
}