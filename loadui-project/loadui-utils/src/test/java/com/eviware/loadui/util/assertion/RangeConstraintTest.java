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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RangeConstraintTest
{
	@Test
	public void shouldAllowInclusive()
	{
		RangeConstraint constraint = new RangeConstraint( -15, 17.1234 );

		assertTrue( constraint.validate( -15 ) );
		assertTrue( constraint.validate( 0 ) );
		assertTrue( constraint.validate( 17.1234 ) );
	}

	@Test
	public void shouldNotAllowOutside()
	{
		RangeConstraint constraint = new RangeConstraint( -15, 17.1234 );

		assertFalse( constraint.validate( -16 ) );
		assertFalse( constraint.validate( 17.12341 ) );
	}

	@Test
	public void shouldHandleExtremes()
	{
		RangeConstraint constraint = new RangeConstraint( -15, 17.1234 );

		assertFalse( constraint.validate( Double.NEGATIVE_INFINITY ) );
		assertFalse( constraint.validate( Double.POSITIVE_INFINITY ) );
	}

	@Test
	public void shouldAlwaysFailNaN()
	{
		RangeConstraint constraint = new RangeConstraint( 0, 0 );
		assertFalse( constraint.validate( Double.NaN ) );

		constraint = new RangeConstraint( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
		assertFalse( constraint.validate( Double.NaN ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void shouldBeValidRange()
	{
		RangeConstraint constraint = new RangeConstraint( 5, 4 );

		assertNotNull( constraint );
	}
}
