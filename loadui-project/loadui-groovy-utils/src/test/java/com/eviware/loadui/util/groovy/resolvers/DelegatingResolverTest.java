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
package com.eviware.loadui.util.groovy.resolvers;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import org.junit.*;
import org.mockito.InOrder;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.groovy.GroovyResolver;

import static org.mockito.Mockito.*;

public class DelegatingResolverTest
{
	private static final String PROPERTY_NAME = "propertyName";
	private static final String METHOD_NAME = "methodName";

	@Test
	public void shouldDelegateToDelegates()
	{
		GroovyResolver.Methods methodResolverMock = mock( GroovyResolver.Methods.class,
				withSettings().extraInterfaces( Releasable.class ) );
		GroovyResolver.Properties propertyResolverMock = mock( GroovyResolver.Properties.class );

		DelegatingResolver resolver = new DelegatingResolver( methodResolverMock, propertyResolverMock );

		resolver.invokeMethod( METHOD_NAME, "arg1", 5 );
		verify( methodResolverMock ).invokeMethod( METHOD_NAME, "arg1", 5 );

		resolver.getProperty( PROPERTY_NAME );
		verify( propertyResolverMock ).getProperty( PROPERTY_NAME );
	}

	@Test
	public void shouldDelegateInOrder()
	{
		GroovyResolver.Methods methodResolverMock1 = mock( GroovyResolver.Methods.class );
		when( methodResolverMock1.invokeMethod( METHOD_NAME ) ).thenThrow(
				new MissingMethodException( METHOD_NAME, getClass(), new Object[0] ) );
		GroovyResolver.Methods methodResolverMock2 = mock( GroovyResolver.Methods.class );
		when( methodResolverMock2.invokeMethod( METHOD_NAME ) ).thenThrow(
				new MissingMethodException( METHOD_NAME, getClass(), new Object[0] ) );
		GroovyResolver.Methods methodResolverMock3 = mock( GroovyResolver.Methods.class );

		DelegatingResolver resolver = new DelegatingResolver( methodResolverMock1, methodResolverMock2 );
		resolver.addResolver( methodResolverMock3 );

		resolver.invokeMethod( METHOD_NAME );

		InOrder inOrder = inOrder( methodResolverMock1, methodResolverMock2, methodResolverMock3 );

		inOrder.verify( methodResolverMock1 ).invokeMethod( METHOD_NAME );
		inOrder.verify( methodResolverMock2 ).invokeMethod( METHOD_NAME );
		inOrder.verify( methodResolverMock3 ).invokeMethod( METHOD_NAME );
	}

	@Test
	public void shouldNotReleaseReleasablesWrappedWithNoRelease()
	{
		GroovyResolver.Methods methodResolverMock = mock( GroovyResolver.Methods.class,
				withSettings().extraInterfaces( Releasable.class ) );
		GroovyResolver.Properties propertyResolverMock = mock( GroovyResolver.Properties.class, withSettings()
				.extraInterfaces( Releasable.class ) );

		DelegatingResolver resolver = new DelegatingResolver( methodResolverMock,
				DelegatingResolver.noRelease( propertyResolverMock ) );

		resolver.release();

		verify( ( Releasable )methodResolverMock ).release();
		verify( ( Releasable )propertyResolverMock, never() ).release();
	}

	@Test( expected = MissingMethodException.class )
	public void methodShouldFailIfAllDelegatesFail()
	{
		GroovyResolver.Methods methodResolverMock = mock( GroovyResolver.Methods.class );
		when( methodResolverMock.invokeMethod( METHOD_NAME ) ).thenThrow(
				new MissingMethodException( METHOD_NAME, getClass(), new Object[0] ) );

		DelegatingResolver resolver = new DelegatingResolver( methodResolverMock );

		resolver.invokeMethod( METHOD_NAME );
	}

	@Test( expected = MissingPropertyException.class )
	public void propertyShouldFailIfAllDelegatesFail()
	{
		GroovyResolver.Properties propertyResolverMock = mock( GroovyResolver.Properties.class );
		when( propertyResolverMock.getProperty( PROPERTY_NAME ) )
				.thenThrow( new MissingPropertyException( PROPERTY_NAME ) );

		DelegatingResolver resolver = new DelegatingResolver( propertyResolverMock );

		resolver.getProperty( PROPERTY_NAME );
	}
}
