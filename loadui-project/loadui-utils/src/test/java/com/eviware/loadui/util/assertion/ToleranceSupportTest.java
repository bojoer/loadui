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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ToleranceSupportTest
{
	private ToleranceSupport toleranceSupport;

	@Before
	public void setup()
	{
		toleranceSupport = new ToleranceSupport();
	}

	@Test
	public void shouldAlwaysTriggerWhenNoAllowedOccurances()
	{
		toleranceSupport.setTolerance( 10, 0 );

		assertTrue( toleranceSupport.occur( 0 ) );
		assertTrue( toleranceSupport.occur( 1000 ) );
		assertTrue( toleranceSupport.occur( 100000 ) );
	}

	@Test
	public void shouldTriggerAfterAllowedOccurances()
	{
		toleranceSupport.setTolerance( 10, 5 );

		assertFalse( toleranceSupport.occur( 0 ) );
		assertFalse( toleranceSupport.occur( 0 ) );
		assertFalse( toleranceSupport.occur( 0 ) );
		assertFalse( toleranceSupport.occur( 0 ) );
		assertFalse( toleranceSupport.occur( 0 ) );

		assertTrue( toleranceSupport.occur( 0 ) );
	}
}
