/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.deferrable;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class AbstractDeferrableTest
{
	private int workCalls;
	private int onCompleteCalls;

	@Before
	public void setup()
	{
		workCalls = 0;
		onCompleteCalls = 0;
	}

	@Test
	public void shouldRunCallbackWhenDone()
	{

		AbstractDeferrable d = new AbstractDeferrable()
		{
			@Override
			public boolean work()
			{
				workCalls++ ;
				return workCalls == 2;
			}

			@Override
			public void onComplete()
			{
				onCompleteCalls++ ;
			}
		};

		d.run();

		assertThat( workCalls, is( 1 ) );
		assertThat( onCompleteCalls, is( 0 ) );

		d.run();

		assertThat( workCalls, is( 2 ) );
		assertThat( onCompleteCalls, is( 1 ) );
	}
}
