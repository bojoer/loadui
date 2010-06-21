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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.eviware.loadui.api.deferrable.Deferrable;

import static org.mockito.Mockito.*;

public class DeferrableManagerImplTest
{
	private DeferrableManagerImpl manager;

	@Before
	public void setup()
	{
		manager = new DeferrableManagerImpl( mock( ScheduledExecutorService.class ), 1, TimeUnit.SECONDS );
	}

	@Test
	public void shouldRunDeferrablesUpFront()
	{
		Deferrable d1 = mock( Deferrable.class );
		Deferrable d2 = mock( Deferrable.class );

		when( d1.run() ).thenReturn( true );
		when( d2.run() ).thenReturn( false, true );

		manager.defer( d1 );
		manager.defer( d2 );

		verify( d1 ).run();
		verify( d2 ).run();
		verifyNoMoreInteractions( d1 );

		manager.run();

		verify( d2, times( 2 ) ).run();
		verifyNoMoreInteractions( d2 );

		manager.run();
		manager.run();
	}

	@Test
	public void shouldRunPeriodically()
	{
		Deferrable d1 = mock( Deferrable.class );
		when( d1.run() ).thenReturn( false, false, false, true );

		manager.defer( d1 );

		manager.run();
		manager.run();
		manager.run();

		verify( d1, times( 4 ) ).run();
		verifyNoMoreInteractions( d1 );

		manager.run();
	}
}
