/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;

public class BaseCategoryTest
{
	private ComponentItem component;
	private BaseCategory baseCategory;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		baseCategory = new BaseCategory( component.getContext() )
		{
			@Override
			public String getColor()
			{
				return null;
			}

			@Override
			public String getCategory()
			{
				return null;
			}
		};
		ComponentTestUtils.setComponentBehavior( component, baseCategory );
	}

	@Test
	public void shouldCreateTotals() throws Exception
	{
		@SuppressWarnings( "unchecked" )
		Callable<Number> callable = mock( Callable.class );
		when( callable.call() ).thenReturn( 7 );

		Value<Number> total = baseCategory.createTotal( "total", callable );
		assertThat( total.getValue().intValue(), is( 7 ) );

		when( callable.call() ).thenReturn( 11 );
		assertThat( total.getValue().intValue(), is( 11 ) );

		baseCategory.removeTotal( "total" );
		assertThat( total.getValue().intValue(), is( 0 ) );
	}

	@Test
	public void shouldAggregateTotals() throws Exception
	{
		@SuppressWarnings( "unchecked" )
		Callable<Number> callable = mock( Callable.class );
		when( callable.call() ).thenReturn( 7 );

		BaseCategory baseCategorySpy = spy( baseCategory );
		ComponentTestUtils.setComponentBehavior( component, baseCategorySpy );

		final CountDownLatch messageAwaiter = new CountDownLatch( 1 );

		doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				TerminalMessage message = ( TerminalMessage )invocation.getArguments()[2];
				assertThat( message.get( "total" ), is( ( Object )7 ) );
				messageAwaiter.countDown();

				return null;
			}
		} ).when( baseCategorySpy ).onTerminalMessage( any( OutputTerminal.class ), any( InputTerminal.class ),
				any( TerminalMessage.class ) );

		try
		{
			System.setProperty( LoadUI.INSTANCE, LoadUI.AGENT );
			baseCategory.createTotal( "total", callable );
		}
		finally
		{
			System.setProperty( LoadUI.INSTANCE, LoadUI.CONTROLLER );
		}

		assertThat( messageAwaiter.await( 5, TimeUnit.SECONDS ), is( true ) );
	}
}
