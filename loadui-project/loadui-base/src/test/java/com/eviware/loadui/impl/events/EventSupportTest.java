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
package com.eviware.loadui.impl.events;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.EventObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.util.events.EventSupport;

public class EventSupportTest
{
	private EventSupport support;
	private EventHandler<BaseEvent> mockListener;
	private BaseEvent sameType;
	private PropertyEvent subType;
	private EventObject superType;

	@Before
	@SuppressWarnings( "unchecked" )
	public void setup()
	{
		support = new EventSupport( this );
		mockListener = mock( EventHandler.class );

		sameType = mock( BaseEvent.class );
		subType = mock( PropertyEvent.class );
		superType = mock( EventObject.class );
	}

	@After
	public void teardown()
	{
		support.release();
	}

	@Test
	public void shouldHandleSameTypes() throws InterruptedException
	{
		support.addEventListener( BaseEvent.class, mockListener );

		support.fireEvent( sameType );

		Thread.sleep( 100 );

		verify( mockListener ).handleEvent( sameType );
		verifyNoMoreInteractions( mockListener );
	}

	@Test
	public void shouldHandleSubtypes() throws InterruptedException
	{
		support.addEventListener( BaseEvent.class, mockListener );

		support.fireEvent( subType );

		Thread.sleep( 100 );

		verify( mockListener ).handleEvent( subType );
		verifyNoMoreInteractions( mockListener );
	}

	@Test
	public void shouldNotHandleSupertypes() throws InterruptedException
	{
		support.addEventListener( BaseEvent.class, mockListener );

		support.fireEvent( superType );

		Thread.sleep( 100 );

		verify( mockListener, never() ).handleEvent( any( BaseEvent.class ) );
		verifyNoMoreInteractions( mockListener );
	}

	@Test
	public void shouldNotReceiveEventsAfterBeingRemoved() throws InterruptedException
	{
		support.addEventListener( BaseEvent.class, mockListener );

		support.fireEvent( subType );

		Thread.sleep( 100 );

		verify( mockListener ).handleEvent( subType );

		support.removeEventListener( BaseEvent.class, mockListener );

		support.fireEvent( subType );
		support.fireEvent( sameType );
		support.fireEvent( superType );

		Thread.sleep( 100 );

		verifyNoMoreInteractions( mockListener );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldWorkWithSeveralListeners() throws InterruptedException
	{
		support.addEventListener( BaseEvent.class, mockListener );
		EventHandler<EventObject> secondListener = mock( EventHandler.class );
		support.addEventListener( EventObject.class, secondListener );

		support.fireEvent( subType );

		Thread.sleep( 100 );

		verify( mockListener ).handleEvent( subType );
		verify( secondListener ).handleEvent( subType );

		support.fireEvent( sameType );
		support.fireEvent( sameType );

		Thread.sleep( 100 );

		verify( mockListener, times( 2 ) ).handleEvent( sameType );
		verify( secondListener, times( 2 ) ).handleEvent( sameType );

		support.fireEvent( superType );

		Thread.sleep( 100 );

		verify( secondListener ).handleEvent( superType );

		verifyNoMoreInteractions( mockListener );
		verifyNoMoreInteractions( secondListener );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldDeliverEventsInCorrectOrder() throws InterruptedException
	{
		EventHandler<EventObject> handler1 = mock( EventHandler.class );
		EventHandler<EventObject> handler2 = mock( EventHandler.class );
		EventHandler<EventObject> handler3 = mock( EventHandler.class );

		doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				support.fireEvent( sameType );
				return null;
			}
		} ).when( handler2 ).handleEvent( superType );

		support.addEventListener( EventObject.class, handler1 );
		support.addEventListener( EventObject.class, handler2 );
		support.addEventListener( EventObject.class, handler3 );

		support.fireEvent( superType );

		Thread.sleep( 100 );

		InOrder inOrder = inOrder( handler1 );

		inOrder.verify( handler1 ).handleEvent( superType );
		inOrder.verify( handler1 ).handleEvent( sameType );
		verifyNoMoreInteractions( handler1 );

		inOrder = inOrder( handler2 );

		inOrder.verify( handler2 ).handleEvent( superType );
		inOrder.verify( handler2 ).handleEvent( sameType );
		verifyNoMoreInteractions( handler2 );

		inOrder = inOrder( handler3 );

		inOrder.verify( handler3 ).handleEvent( superType );
		inOrder.verify( handler3 ).handleEvent( sameType );
		verifyNoMoreInteractions( handler3 );
	}
}
