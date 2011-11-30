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
package com.eviware.loadui.util.serialization;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import com.eviware.loadui.api.serialization.ListenableValue.ValueListener;

public class ListenableValueSupportTest
{
	private ListenableValueSupport<Number> listenableValueSupport;

	@Before
	public void setup()
	{
		listenableValueSupport = new ListenableValueSupport<Number>();
	}

	@Test
	public void shouldNotifyListeners()
	{
		@SuppressWarnings( "unchecked" )
		ValueListener<Number> listener = mock( ValueListener.class );

		listenableValueSupport.addListener( listener );

		listenableValueSupport.update( 0 );
		listenableValueSupport.update( 7 );
		listenableValueSupport.update( 4 );

		InOrder inOrder = inOrder( listener );

		inOrder.verify( listener ).update( 0 );
		inOrder.verify( listener ).update( 7 );
		inOrder.verify( listener ).update( 4 );
	}

	@Test
	public void shouldNotNotifyListeners()
	{
		@SuppressWarnings( "unchecked" )
		ValueListener<Number> listener = mock( ValueListener.class );

		listenableValueSupport.addListener( listener );
		listenableValueSupport.removeListener( listener );

		listenableValueSupport.update( 0 );
		listenableValueSupport.update( 7 );
		listenableValueSupport.update( 4 );

		verify( listener, never() ).update( Matchers.<Number> any() );
	}

	@Test
	public void shouldNotHoldStrongReferences() throws Exception
	{
		listenableValueSupport.addListener( new ValueListener<Number>()
		{
			@Override
			public void update( Number value )
			{
				throw new UnsupportedOperationException();
			}
		} );

		System.gc();
		Thread.sleep( 50 );

		listenableValueSupport.update( 0 );

		assertThat( listenableValueSupport.getListenerCount(), is( 0 ) );
	}

	@Test
	public void shouldRetainLatestValue()
	{
		assertThat( listenableValueSupport.getLastValue(), nullValue() );

		listenableValueSupport.update( 15.7 );
		assertThat( ( Double )listenableValueSupport.getLastValue(), is( 15.7 ) );

		listenableValueSupport.update( 0.345 );
		assertThat( ( Double )listenableValueSupport.getLastValue(), is( 0.345 ) );
	}
}
